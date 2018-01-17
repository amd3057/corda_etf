package net.corda.hackathon.ralcog01.etf;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.corda.core.flows.*;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import java.security.PublicKey;
import java.time.Duration;
import java.util.List;
import net.corda.hackathon.ralcog01.etf.OrderBaseFlow.SignTxFlowNoChecking;

public class ReturnBasketFlow {
    @InitiatingFlow
    @StartableByRPC
    public static class Initiator extends OrderBaseFlow {
        private final Basket basket;
        private final Party ec;

        private final ProgressTracker.Step INITIALISING = new ProgressTracker.Step("Performing initial steps.");
        private final ProgressTracker.Step BUILDING = new ProgressTracker.Step("Performing initial steps.");
        private final ProgressTracker.Step SIGNING = new ProgressTracker.Step("Signing transaction.");
        private final ProgressTracker.Step COLLECTING = new ProgressTracker.Step("Collecting counterparty signature.") {
            @Override
            public ProgressTracker childProgressTracker() {
                return CollectSignaturesFlow.Companion.tracker();
            }
        };
        private final ProgressTracker.Step FINALISING = new ProgressTracker.Step("Finalising transaction.") {
            @Override
            public ProgressTracker childProgressTracker() {
                return FinalityFlow.Companion.tracker();
            }
        };

        private final ProgressTracker progressTracker = new ProgressTracker(
                INITIALISING, BUILDING, SIGNING, COLLECTING, FINALISING
        );

        public Initiator(Basket basket, Party ec) {
            this.basket = basket;
            this.ec = ec;
        }

        @Override
        public ProgressTracker getProgressTracker() {
            return progressTracker;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            // Step 1. Initialisation.
            progressTracker.setCurrentStep(INITIALISING);

            final PublicKey ourSigningKey = basket.getOwner().getOwningKey();

            // Step 2. Building.
            progressTracker.setCurrentStep(BUILDING);
            Party aa = getServiceHub().getNetworkMapCache().getPeerByLegalName(new CordaX500Name("AA", "New York", "US"));
            Party ap = getServiceHub().getNetworkMapCache().getPeerByLegalName(new CordaX500Name("AP", "New York", "US"));
            final List<PublicKey> requiredSigners = ImmutableList.of(ec.getOwningKey(), aa.getOwningKey(), ap.getOwningKey());

            Basket newBasket = new Basket(ec, ap, this.basket.getProducts(), this.basket.getLinearId());

            final TransactionBuilder utx = new TransactionBuilder(getFirstNotary())
                    .addOutputState(newBasket, NullReedemContract.REDEEM_CONTRACT_ID)
                    .addCommand(new NullReedemContract.Create(), requiredSigners)
                    .setTimeWindow(getServiceHub().getClock().instant(), Duration.ofSeconds(30));

            // Step 3. Sign the transaction.
            progressTracker.setCurrentStep(SIGNING);
            final SignedTransaction ptx = getServiceHub().signInitialTransaction(utx, ourSigningKey);

            // Step 4. Get the counter-party signature.
            progressTracker.setCurrentStep(COLLECTING);
            final FlowSession requesterFlow = initiateFlow(ap);
            final SignedTransaction stx = subFlow(new CollectSignaturesFlow(
                    ptx,
                    ImmutableSet.of(requesterFlow),
                    ImmutableList.of(ourSigningKey),
                    COLLECTING.childProgressTracker())
            );

            // Step 5. Finalise the transaction.
            progressTracker.setCurrentStep(FINALISING);
            subFlow(new FinalityFlow(stx, FINALISING.childProgressTracker()));

            return null;

        }

      }
        @InitiatedBy(ReturnBasketFlow.Initiator.class)
        public static class Responder extends FlowLogic<SignedTransaction> {
            private final FlowSession otherFlow;

            public Responder(FlowSession otherFlow) {
                this.otherFlow = otherFlow;
            }

            @Suspendable
            @Override
            public SignedTransaction call() throws FlowException {
                final SignedTransaction stx = subFlow(new SignTxFlowNoChecking(otherFlow, SignTransactionFlow.Companion.tracker()));
                return waitForLedgerCommit(stx.getId());
            }
        }

}
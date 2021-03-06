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

public class IssueRedeemFlow {
    @InitiatingFlow
    @StartableByRPC
    public static class Initiator extends OrderBaseFlow {
        private final Product product;
        //private final Party ap;
        private final Party pa;
        //private final Boolean anonymous;

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

        /**
        public Initiator(Product product, Party ap) {
            this.product = product;
            this.ap = ap;
            //this.anonymous = anonymous;
        }
         **/
        public Initiator(Product product, Party pa) {
            this.product = product;
            this.pa = pa;
            //this.anonymous = anonymous;
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

            final PublicKey ourSigningKey = product.getOwner().getOwningKey();

            // Step 2. Building.
            progressTracker.setCurrentStep(BUILDING);
            //Party pa = getServiceHub().getNetworkMapCache().getPeerByLegalName(new CordaX500Name("PA", "New York", "US"));
            final List<PublicKey> requiredSigners = ImmutableList.of(getOurIdentity().getOwningKey(), pa.getOwningKey());

            final TransactionBuilder utx = new TransactionBuilder(getFirstNotary())
                    .addOutputState(product, NullReedemContract.REDEEM_CONTRACT_ID)
                    .addCommand(new NullReedemContract.Create(), requiredSigners)
                    .setTimeWindow(getServiceHub().getClock().instant(), Duration.ofSeconds(30));

            // Step 3. Sign the transaction.
            progressTracker.setCurrentStep(SIGNING);
            final SignedTransaction ptx = getServiceHub().signInitialTransaction(utx, ourSigningKey);

            // Step 4. Get the counter-party signature.
            progressTracker.setCurrentStep(COLLECTING);
            //final FlowSession requesterFlow = initiateFlow(ap);
            final FlowSession requesterFlow = initiateFlow(pa);
            final SignedTransaction stx = subFlow(new CollectSignaturesFlow(
                    ptx,
                    ImmutableSet.of(requesterFlow),
                    ImmutableList.of(ourSigningKey),
                    COLLECTING.childProgressTracker())
            );

            // Step 5. Finalise the transaction.
            progressTracker.setCurrentStep(FINALISING);
            //return subFlow(new FinalityFlow(stx, FINALISING.childProgressTracker()));
            subFlow(new FinalityFlow(stx, FINALISING.childProgressTracker()));
            return subFlow(new UnbundleAndNotifyFlow.Initiator(product, pa));
        }

     }

        @InitiatedBy(IssueRedeemFlow.Initiator.class)
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
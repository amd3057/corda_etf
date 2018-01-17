package net.corda.hackathon.ralcog01.etf;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.StateAndContract;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;
import net.corda.hackathon.ralcog01.etf.OrderBaseFlow.SignTxFlowNoChecking;
import org.apache.logging.slf4j.Log4jLogger;

import java.security.PublicKey;
import java.time.Duration;
import java.util.List;

public class IssueOrderFlow {

    @InitiatingFlow
    @StartableByRPC
    public static class Initiator extends OrderBaseFlow {
        private final Basket basket;
        private final Party requester;
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

        public Initiator(Basket basket, Party requester) {
            this.basket = basket;
            this.requester = requester;
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
            // We retrieve the notary identity from the network map.
            final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);

            // Step 3. Sign the transaction.
            progressTracker.setCurrentStep(SIGNING);
            // We create a transaction builder and add the components.

            StateAndContract outputContractAndState = new StateAndContract(basket, IssueOrderContract.ISSUE_ORDER_CONTRACT_ID);
            List<PublicKey> requiredSigners = ImmutableList.of(getOurIdentity().getOwningKey(), requester.getOwningKey());
            Command cmd = new Command<>(new IssueOrderContract.Commands.Issue(), requiredSigners);
            final TransactionBuilder txBuilder = new TransactionBuilder();
            txBuilder.setNotary(notary);
            //We add the items to the builder.
            txBuilder.withItems(outputContractAndState, cmd);

            // Verifying the transaction.
            txBuilder.verify(getServiceHub());
            progressTracker.setCurrentStep(COLLECTING);
            // Signing the transaction.
            // Signing the transaction.
            final SignedTransaction signedTx = getServiceHub().signInitialTransaction(txBuilder);

        // Creating a session with the other party.
            FlowSession otherpartySession = initiateFlow(requester);

        // Obtaining the counterparty's signature.
            SignedTransaction fullySignedTx = subFlow(new CollectSignaturesFlow(
                    signedTx, ImmutableList.of(otherpartySession), CollectSignaturesFlow.tracker()));
        // Step 5. Finalise the transaction.
            progressTracker.setCurrentStep(FINALISING);
            // Finalising the transaction.
            // Finalising the transaction.
            subFlow(new FinalityFlow(fullySignedTx));
            //progressTracker.setCurrentStep();
            Party etfSponsor = getServiceHub().getNetworkMapCache().getPeerByLegalName(new CordaX500Name("PartyC", "Paris", "FR"));
            Party participatingAccount = getServiceHub().getNetworkMapCache().getPeerByLegalName(new CordaX500Name("PartyA", "London", "GB"));
            subFlow(new ValidateAndNotifySponsorFlow(basket, requester, etfSponsor, participatingAccount));
            return null;
        }

        @Suspendable
        private Basket enrichBasket(Basket basket, Party requester) throws FlowException {
/*            if (anonymous) {
                final HashMap<Party, AnonymousParty> txKeys = subFlow(new SwapIdentitiesFlow(lender));

                if (txKeys.size() != 2) {
                    throw new IllegalStateException("Something went wrong when generating confidential identities.");
                } else if (!txKeys.containsKey(getOurIdentity())) {
                    throw new FlowException("Couldn't create our conf. identity.");
                } else if (!txKeys.containsKey(lender)) {
                    throw new FlowException("Couldn't create lender's conf. identity.");
                }

                final AnonymousParty anonymousMe = txKeys.get(getOurIdentity());
                final AnonymousParty anonymousLender = txKeys.get(lender);

                return new Obligation(amount, anonymousLender, anonymousMe);
            } else {
                return new Obligation(amount, lender, getOurIdentity());
            }*/

            return new Basket(basket.getIssuer(), basket.getOwner(), basket.getProducts(), basket.getReqProduct(), new UniqueIdentifier());
        }
    }
//
//    @InitiatedBy(IssueOrderFlow.Initiator.class)
//    public static class Responder extends FlowLogic<SignedTransaction> {
//        private final FlowSession otherFlow;
//
//        public Responder(FlowSession otherFlow) {
//            this.otherFlow = otherFlow;
//        }
//
//        @Suspendable
//        @Override
//        public SignedTransaction call() throws FlowException {
//            final SignedTransaction stx = subFlow(new SignTxFlowNoChecking(otherFlow, SignTransactionFlow.Companion.tracker()));
//            return waitForLedgerCommit(stx.getId());
//        }
//    }
}
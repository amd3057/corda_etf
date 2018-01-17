package net.corda.hackathon.ralcog01.etf;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.StateAndContract;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;
import net.corda.examples.obligation.Obligation;

import java.security.PublicKey;
import java.util.List;

@InitiatingFlow
@StartableByRPC
public class ValidateAndNotifySponsorFlow extends OrderBaseFlow {

    //private final Basket basket;
    //private final Party etfCustodian;
    //private final Party etfSponsor;
    private final Party participantAccount;
    private final UniqueIdentifier linearId;

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
    public ValidateAndNotifySponsorFlow(Basket basket, Party etfCustodian, Party etfSponsor, Party participantAccount) {
        this.basket = basket;
        this.etfCustodian = etfCustodian;
        this.etfSponsor = etfSponsor;
        this.participantAccount = participantAccount;
    }**/
    public ValidateAndNotifySponsorFlow(UniqueIdentifier linearId, Party participantAccount) {
        this.linearId = linearId;
        this.participantAccount = participantAccount;
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
        // We create a transaction builder.
        final TransactionBuilder txBuilder = new TransactionBuilder();
        txBuilder.setNotary(notary);

        final StateAndRef<Basket> obligationToTransfer = getBasketsByLinearId(linearId);
        final Basket basket = obligationToTransfer.getState().getData();

        // We create the transaction components.
        //Basket newBasket = new Basket(this.etfCustodian, this.participantAccount, this.basket.getProducts(), this.basket.getReqProduct(), this.basket.getLinearId());
        Basket newBasket = new Basket(getOurIdentity(), this.participantAccount, basket.getProducts(), basket.getReqProduct(), basket.getLinearId());

        Product product = new Product(basket.getReqProduct(),participantAccount);
        StateAndContract outputContractAndState = new StateAndContract(product, ValidateAndNotifySponsorContract.VALIDATE_AND_NOTIFY_BASKET_CONTRACT_ID);


        //List<PublicKey> requiredSigners = ImmutableList.of(etfCustodian.getOwningKey(), etfSponsor.getOwningKey(), participantAccount.getOwningKey());
        List<PublicKey> requiredSigners = ImmutableList.of(getOurIdentity().getOwningKey(), participantAccount.getOwningKey());
        Command cmd = new Command<>(new ValidateAndNotifySponsorContract.Create(), requiredSigners);

// We add the items to the builder.
        txBuilder.withItems(obligationToTransfer,outputContractAndState, cmd);

// Verifying the transaction.
        txBuilder.verify(getServiceHub());
        progressTracker.setCurrentStep(COLLECTING);

// Signing the transaction.
        final SignedTransaction signedTx = getServiceHub().signInitialTransaction(txBuilder);

// Creating a session with the other party.
        //FlowSession otherpartySession = initiateFlow(etfCustodian);
        FlowSession otherpartySession = initiateFlow(participantAccount);

// Obtaining the counterparty's signature.
        SignedTransaction fullySignedTx = subFlow(new CollectSignaturesFlow(
                signedTx, ImmutableList.of(otherpartySession), CollectSignaturesFlow.tracker()));
        progressTracker.setCurrentStep(FINALISING);
// Finalising the transaction.
        subFlow(new FinalityFlow(fullySignedTx));

// Call the deposit to AP flow
//        Party ap = getServiceHub().getNetworkMapCache().getPeerByLegalName(new CordaX500Name("PartyA", "New York", "US"));
//        subFlow(new DepositToAPFlow(basket, ap, participantAccount));
        return null;
    }

    @Override
    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }


}

package net.corda.hackathon.ralcog01.etf;

import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.StateAndContract;
import net.corda.core.flows.*;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import java.security.PublicKey;
import java.util.List;

@InitiatingFlow
public class ValidateAndNotifySponsorFlow extends OrderBaseFlow {

    private final Basket basket;
    private final Party etfCustodian;
    private final Party etfSponsor;
    private final Party participantAccount;

    /**
     * The progress tracker provides checkpoints indicating the progress of the flow to observers.
     */
    private final ProgressTracker progressTracker = new ProgressTracker();

    public ValidateAndNotifySponsorFlow(Basket basket, Party etfCustodian, Party etfSponsor, Party participantAccount) {
        this.basket = basket;
        this.etfCustodian = etfCustodian;
        this.etfSponsor = etfSponsor;
        this.participantAccount = participantAccount;
    }

    @Override
    public SignedTransaction call() throws FlowException {
        // We retrieve the notary identity from the network map.
        final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);
        // We create a transaction builder.
        final TransactionBuilder txBuilder = new TransactionBuilder();
        txBuilder.setNotary(notary);

        // We create the transaction components.
        Basket newBasket = new Basket(this.etfCustodian, this.participantAccount, this.basket.getProducts(), this.basket.getReqProduct(), this.basket.getLinearId());
        StateAndContract outputContractAndState = new StateAndContract(newBasket, ValidateAndNotifySponsorContract.VALIDATE_AND_NOTIFY_BASKET_CONTRACT_ID);
        List<PublicKey> requiredSigners = ImmutableList.of(etfCustodian.getOwningKey(), etfSponsor.getOwningKey(), participantAccount.getOwningKey());
        Command cmd = new Command<>(new ValidateAndNotifySponsorContract.Create(), requiredSigners);


// We add the items to the builder.
        txBuilder.withItems(outputContractAndState, cmd);

// Verifying the transaction.
        txBuilder.verify(getServiceHub());

// Signing the transaction.
        final SignedTransaction signedTx = getServiceHub().signInitialTransaction(txBuilder);

// Creating a session with the other party.
        FlowSession otherpartySession = initiateFlow(etfCustodian);

// Obtaining the counterparty's signature.
        SignedTransaction fullySignedTx = subFlow(new CollectSignaturesFlow(
                signedTx, ImmutableList.of(otherpartySession), CollectSignaturesFlow.tracker()));

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

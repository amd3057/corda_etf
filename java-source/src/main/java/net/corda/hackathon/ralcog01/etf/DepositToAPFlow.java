package net.corda.hackathon.ralcog01.etf;

import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.StateAndContract;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import java.security.PublicKey;
import java.util.List;

public class DepositToAPFlow extends OrderBaseFlow {

    private final Basket basket;
    private final Party ap;
    private final Party participantAccount;

    /**
     * The progress tracker provides checkpoints indicating the progress of the flow to observers.
     */
    private final ProgressTracker progressTracker = new ProgressTracker();

    public DepositToAPFlow(Basket basket, Party ap, Party participantAccount) {
        this.basket = basket;
        this.ap = ap;
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
        StateAndContract outputContractAndState = new StateAndContract(basket, DepositToAPContract.DEPOSIT_TO_AP_CONTRACT_ID);
        StateAndContract outputContractAndState1 = new StateAndContract(basket.getReqProduct(), DepositToAPContract.DEPOSIT_TO_AP_CONTRACT_ID);

        List<PublicKey> requiredSigners = ImmutableList.of(participantAccount.getOwningKey(), ap.getOwningKey());
        Command cmd = new Command<>(new DepositToAPContract.Create(), requiredSigners);


// We add the items to the builder.
        txBuilder.withItems(outputContractAndState, outputContractAndState1, cmd);

// Verifying the transaction.
        txBuilder.verify(getServiceHub());

// Signing the transaction.
        final SignedTransaction signedTx = getServiceHub().signInitialTransaction(txBuilder);

// Creating a session with the other party.
        FlowSession otherpartySession = initiateFlow(ap);

// Obtaining the counterparty's signature.
        SignedTransaction fullySignedTx = subFlow(new CollectSignaturesFlow(
                signedTx, ImmutableList.of(otherpartySession), CollectSignaturesFlow.tracker()));

// Finalising the transaction.
        subFlow(new FinalityFlow(fullySignedTx));

        return null;
    }

    @Override
    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }
}

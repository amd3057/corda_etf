package net.corda.hackathon.ralcog01.etf;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.StateAndContract;
import net.corda.core.flows.CollectSignaturesFlow;
import net.corda.core.flows.FinalityFlow;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowSession;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import java.security.PublicKey;
import java.util.List;

public class DeliverBasketFlow extends OrderBaseFlow {

    private final Basket basket;
    private final Party etfCustodian;
    private final Party apAgent;

    /**
     * The progress tracker provides checkpoints indicating the progress of the flow to observers.
     */
    private final ProgressTracker progressTracker = new ProgressTracker();

    public DeliverBasketFlow(Basket basket, Party etfCustodian, Party apAgent) {
        this.basket = basket;
        this.etfCustodian = etfCustodian;
        this.apAgent = apAgent;
    }

    @Override
    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }

    @Override
    @Suspendable
    public SignedTransaction call() throws FlowException {
        // We retrieve the notary identity from the network map.
        final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);
        // We create a transaction builder.
        final TransactionBuilder txBuilder = new TransactionBuilder();
        txBuilder.setNotary(notary);

        // We create the transaction components.
        Basket newBasket = new Basket(this.apAgent, this.etfCustodian, this.basket.getProducts(), this.basket.getReqProduct(), this.basket.getLinearId());
        StateAndContract outputContractAndState = new StateAndContract(newBasket, DeliverBasketContract.DELIVER_BASKET_CONTRACT_ID);
        List<PublicKey> requiredSigners = ImmutableList.of(etfCustodian.getOwningKey(), apAgent.getOwningKey());
        Command cmd = new Command<>(new DeliverBasketContract.Create(), requiredSigners);


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

        return null;
    }
}

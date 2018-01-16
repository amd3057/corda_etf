package net.corda.hackathon.ralcog01.etf;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.ImmutableList;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;
import net.corda.core.contracts.StateAndContract;
import net.corda.core.contracts.Command;

import java.security.PublicKey;
import java.util.List;

// Replace TemplateFlow's definition with:
@InitiatingFlow
@StartableByRPC
public class ProductFlow extends FlowLogic<Void> {
    private final String ticker;
    private final double price;
    private final Integer quantity;
    private final Party otherParty;

    /**
     * The progress tracker provides checkpoints indicating the progress of the flow to observers.
     */
    private final ProgressTracker progressTracker = new ProgressTracker();

    public ProductFlow(String ticker, double price, Integer quantity, Party otherParty) {
        this.ticker = ticker;
        this.price = price;
        this.quantity = quantity;
        this.otherParty = otherParty;
    }

    @Override
    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }

    /**
     * The flow logic is encapsulated within the call() method.
     */
    @Suspendable
    @Override
    public Void call() throws FlowException {

        // We retrieve the notary identity from the network map.
        final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);
        // We create a transaction builder.
        final TransactionBuilder txBuilder = new TransactionBuilder();
        txBuilder.setNotary(notary);

        // We create the transaction components.
        Product product = Product.createProduct(ticker, getOurIdentity(), price, quantity);
        StateAndContract outputContractAndState = new StateAndContract(product, ProductContract.PRODUCT_CONTRACT_ID);
        List<PublicKey> requiredSigners = ImmutableList.of(getOurIdentity().getOwningKey());
        Command cmd = new Command<>(new ProductContract.Create(), requiredSigners);


// We add the items to the builder.
        txBuilder.withItems(outputContractAndState, cmd);

// Verifying the transaction.
        txBuilder.verify(getServiceHub());

// Signing the transaction.
        final SignedTransaction signedTx = getServiceHub().signInitialTransaction(txBuilder);

// Creating a session with the other party.
        FlowSession otherpartySession = initiateFlow(otherParty);

// Obtaining the counterparty's signature.
        SignedTransaction fullySignedTx = subFlow(new CollectSignaturesFlow(
                signedTx, ImmutableList.of(otherpartySession), CollectSignaturesFlow.tracker()));

// Finalising the transaction.
        subFlow(new FinalityFlow(fullySignedTx));

        return null;
    }
}

package net.corda.hackathon.ralcog01.etf;


import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.core.flows.FlowSession;
import net.corda.core.flows.SignTransactionFlow;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.utilities.ProgressTracker;

import java.util.List;

/**
 * An abstract FlowLogic class that is subclassed by the product flows to
 * provide helper methods and classes.
 */
abstract class OrderBaseFlow extends FlowLogic<SignedTransaction> {

    Party getFirstNotary() throws FlowException {
        List<Party> notaries = getServiceHub().getNetworkMapCache().getNotaryIdentities();
        if (notaries.isEmpty()) {
            throw new FlowException("No available notary.");
        }
        return notaries.get(0);
    }

    StateAndRef<Product> getProductsByLinearId(UniqueIdentifier linearId) throws FlowException {
        QueryCriteria queryCriteria = new QueryCriteria.LinearStateQueryCriteria(
                null,
                ImmutableList.of(linearId),
                Vault.StateStatus.UNCONSUMED,
                null);

        List<StateAndRef<Product>> products = getServiceHub().getVaultService().queryBy(Product.class, queryCriteria).getStates();
        if (products.size() != 1) {
            throw new FlowException(String.format("Obligation with id %s not found.", linearId));
        }
        return products.get(0);
    }

    Party resolveIdentity(AbstractParty abstractParty) {
        return getServiceHub().getIdentityService().requireWellKnownPartyFromAnonymous(abstractParty);
    }

    static class SignTxFlowNoChecking extends SignTransactionFlow {
        SignTxFlowNoChecking(FlowSession otherFlow, ProgressTracker progressTracker) {
            super(otherFlow, progressTracker);
        }

        @Override
        protected void checkTransaction(SignedTransaction tx) {
            // TODO: Add checking here.
        }
    }
}

package net.corda.hackathon.ralcog01.etf;

import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.CommandWithParties;
import net.corda.core.contracts.Contract;
import net.corda.core.identity.AbstractParty;
import net.corda.core.transactions.LedgerTransaction;

import java.security.PublicKey;
import java.util.List;

import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;
import static net.corda.core.contracts.ContractsDSL.requireThat;

// Replace TemplateContract's definition with:
public class ValidateAndNotifySponsorContract implements Contract {

    public static final String VALIDATE_AND_NOTIFY_BASKET_CONTRACT_ID = "net.corda.hackathon.ralcog01.etf.ValidateAndNotifySponsorContract";

    // Our Create command.
    public static class Create implements CommandData {
    }

    @Override
    public void verify(LedgerTransaction tx) {
        final CommandWithParties<Create> command = requireSingleCommand(tx.getCommands(), Create.class);

        requireThat(check -> {
            // Constraints on the shape of the transaction.
//            check.using("Input state of type basket should be consumed when delivering a basket.", !tx.getInputs().isEmpty());
            check.using("There should be one output state of type basket.", tx.getOutputs().size() == 1);

            // IOU-specific constraints.
            final Basket out = tx.outputsOfType(Basket.class).get(0);
            final AbstractParty issuer = out.getIssuer();
            final AbstractParty owner = out.getOwner();
            check.using("The lender and the borrower cannot be the same entity.", issuer != owner);
            // Constraints on the signers.
            final List<PublicKey> signers = command.getSigners();
            check.using("There must be three signers.", signers.size()==3);
//            check.using("The issuer and owner must be signers.", signers.containsAll(
//                    ImmutableList.of(owner.getOwningKey(), issuer.getOwningKey())));

            return null;
        });

    }
}
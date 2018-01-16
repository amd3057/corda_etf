package net.corda.hackathon.ralcog01.etf;

import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.CommandWithParties;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;

import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;

// Replace TemplateContract's definition with:
public class ProductContract implements Contract {

    public static final String PRODUCT_CONTRACT_ID = "net.corda.hackathon.ralcog01.etf.state.ProductContract";

    // Our Create command.
    public static class Create implements CommandData {
    }

    @Override
    public void verify(LedgerTransaction tx) {
        final CommandWithParties<Create> command = requireSingleCommand(tx.getCommands(), Create.class);
        /**
        requireThat(check -> {
            // Constraints on the shape of the transaction.
            check.using("No inputs should be consumed when issuing an IOU.", tx.getInputs().isEmpty());
            check.using("There should be one output state of type IOUState.", tx.getOutputs().size() == 1);

            // IOU-specific constraints.
            final IOUState out = tx.outputsOfType(IOUState.class).get(0);
            final Party lender = out.getLender();
            final Party borrower = out.getBorrower();
            check.using("The IOU's value must be non-negative.", out.getValue() > 0);
            check.using("The lender and the borrower cannot be the same entity.", lender != borrower);

            // Constraints on the signers.
            final List<PublicKey> signers = command.getSigners();
            check.using("There must be two signers.", signers.size() == 2);
            check.using("The borrower and lender must be signers.", signers.containsAll(
                    ImmutableList.of(borrower.getOwningKey(), lender.getOwningKey())));

            return null;
        });
         **/
    }
}
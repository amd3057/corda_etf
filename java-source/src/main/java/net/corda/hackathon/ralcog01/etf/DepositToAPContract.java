package net.corda.hackathon.ralcog01.etf;

import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.CommandWithParties;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;

import java.util.Map;

import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;
import static net.corda.core.contracts.ContractsDSL.requireThat;

// Replace TemplateContract's definition with:
public class DepositToAPContract implements Contract {

    public static final String DEPOSIT_TO_AP_CONTRACT_ID = "net.corda.hackathon.ralcog01.etf.DepositToAPContract";

    // Our Create command.
    public static class Create implements CommandData {
    }

    @Override
    public void verify(LedgerTransaction tx) {
        final CommandWithParties<Create> command = requireSingleCommand(tx.getCommands(), Create.class);

        requireThat(check -> {
            // Constraints on the shape of the transaction.
            check.using("Input state of type basket should be consumed when depositing ETF to AP.", !tx.getInputs().isEmpty());
            check.using("There should be one output state of type Product.", tx.getOutputs().size() == 1);

            // IOU-specific constraints.
            final Product out = tx.outputsOfType(Product.class).get(0);
            final int etfQuantity = out.getQuantity();
            final Map<String, Integer> etfProductMap = out.getProductMap();

            final Basket outBasket = tx.outputsOfType(Basket.class).get(0);
            final int reqEtf = outBasket.getReqProduct().getQuantity();
            final Map<String, Integer> reqProductMap = outBasket.getReqProduct().getProductMap();

            check.using("Requested and created quantities are not matching.", etfQuantity == reqEtf);
            check.using("Underlying ETF composition is niot matching", etfProductMap.equals(reqProductMap));
            return null;
        });

    }
}
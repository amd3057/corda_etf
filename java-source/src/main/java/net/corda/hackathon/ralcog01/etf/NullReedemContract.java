package net.corda.hackathon.ralcog01.etf;

import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.CommandWithParties;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;

import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;

// Replace TemplateContract's definition with:
public class NullReedemContract implements Contract {

    public static final String REDEEM_CONTRACT_ID = "net.corda.hackathon.ralcog01.etf.NullReedemContract";

    // Our Create command.
    public static class Create implements CommandData {
    }

    @Override
    public void verify(LedgerTransaction tx) {

    }
}
package net.corda.hackathon.ralcog01.etf;

import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.CommandWithParties;
import net.corda.core.contracts.Contract;
import net.corda.core.contracts.TypeOnlyCommandData;
import net.corda.core.identity.AbstractParty;
import net.corda.core.transactions.LedgerTransaction;

import java.security.PublicKey;
import java.util.HashSet;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;
import static net.corda.core.contracts.ContractsDSL.requireThat;

public class IssueOrderContract implements Contract {
    public static final String ISSUE_ORDER_CONTRACT_ID = "net.corda.hackathon.ralcog01.etf.IssueOrderContract";

    public interface Commands extends CommandData {
        class Issue extends TypeOnlyCommandData implements net.corda.hackathon.ralcog01.etf.IssueOrderContract.Commands {
        }

        class Transfer extends TypeOnlyCommandData implements net.corda.hackathon.ralcog01.etf.IssueOrderContract.Commands {
        }

        class Settle extends TypeOnlyCommandData implements net.corda.hackathon.ralcog01.etf.IssueOrderContract.Commands {
        }
    }

    @Override
    public void verify(LedgerTransaction tx) {
        final CommandWithParties<net.corda.hackathon.ralcog01.etf.IssueOrderContract.Commands> command = requireSingleCommand(tx.getCommands(), net.corda.hackathon.ralcog01.etf.IssueOrderContract.Commands.class);
        final net.corda.hackathon.ralcog01.etf.IssueOrderContract.Commands commandData = command.getValue();
        final Set<PublicKey> setOfSigners = new HashSet<>(command.getSigners());
        verifyIssue(tx, setOfSigners);
    }

    private Set<PublicKey> keysFromParticipants(Basket basket) {
        return basket
                .getParticipants().stream()
                .map(AbstractParty::getOwningKey)
                .collect(toSet());
    }

    // This only allows one basket issuance per transaction.
    private void verifyIssue(LedgerTransaction tx, Set<PublicKey> signers) {
        requireThat(req -> {
            req.using("No inputs should be consumed when issuing an order.",
                    tx.getInputStates().isEmpty());
            req.using("Only one order state should be created when issuing an order.", tx.getOutputStates().size() == 1);
            Basket basket = (Basket) tx.getOutputStates().get(0);
            req.using("A newly issued basket must have a positive number of products.", basket.getProducts().size() > 0);
            req.using("The issuer and owner cannot be the same identity.", !basket.getIssuer().equals(basket.getOwner()));
//            req.using("Both lender and borrower together only may sign issue order transaction.",
//                    signers.equals(keysFromParticipants(basket)));
            return null;
        });
    }
}

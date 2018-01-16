package net.corda.hackathon.ralcog01.etf.api;



import com.google.common.collect.ImmutableMap;
import net.corda.core.contracts.Amount;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.messaging.FlowHandle;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.utilities.OpaqueBytes;
import net.corda.examples.obligation.Obligation;
import net.corda.examples.obligation.flows.IssueObligation;
import net.corda.examples.obligation.flows.SettleObligation;
import net.corda.examples.obligation.flows.TransferObligation;
import net.corda.finance.contracts.asset.Cash;
import net.corda.finance.flows.AbstractCashFlow;
import net.corda.finance.flows.CashIssueFlow;
import net.corda.hackathon.ralcog01.etf.Basket;
import net.corda.hackathon.ralcog01.etf.Product;
import net.corda.hackathon.ralcog01.etf.ProductBuilder;
import net.corda.hackathon.ralcog01.etf.ProductState;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.CREATED;
import static net.corda.finance.contracts.GetBalances.getCashBalances;

@Path("order")
public class IssueOrder {
    private final CordaRPCOps rpcOps;
    private final Party myIdentity;

    public IssueOrder(CordaRPCOps rpcOps) {
        this.rpcOps = rpcOps;
        this.myIdentity = rpcOps.nodeInfo().getLegalIdentities().get(0);
    }

    @GET
    @Path("me")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Party> me() {
        return ImmutableMap.of("me", myIdentity);
    }

    @GET
    @Path("peers")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, List<String>> peers() {
        return ImmutableMap.of("peers", rpcOps.networkMapSnapshot()
                .stream()
                .filter(nodeInfo -> nodeInfo.getLegalIdentities().get(0) != myIdentity)
                .map(it -> it.getLegalIdentities().get(0).getName().getOrganisation())
                .collect(toList()));
    }


    @GET
    @Path("products")
    @Produces(MediaType.APPLICATION_JSON)
    public List<StateAndRef<Product>> products() {
        return rpcOps.vaultQuery(Product.class).getStates();
    }

    @GET
    @Path("baskets")
    @Produces(MediaType.APPLICATION_JSON)
    public List<StateAndRef<Basket>> baskets() {
        return rpcOps.vaultQuery(Basket.class).getStates();
    }


    @GET
    @Path("create")
    public Response createOrder(
            @QueryParam(value = "tickers") List<String> basketproducts,
            @QueryParam(value = "ticker") String ticker,
            @QueryParam(value = "quantity") String requestedQty) {


        //Prepare basket

        Stream<StateAndRef<Product>> productsStream = rpcOps.vaultQuery(Product.class).getStates().stream();
        Set<Product> products =productsStream.filter((p)->basketproducts.contains((p.getState().getData().getTicker()))).map((p)->p.getState().getData())
                .collect(Collectors.toSet());

        // 1. Get party objects for the counterparty.
        final Set<Party> lenderIdentities = rpcOps.partiesFromName("CC", false);
        if (lenderIdentities.size() != 1) {
            final String errMsg = String.format("Found %d identities for the lender.", lenderIdentities.size());
            throw new IllegalStateException(errMsg);
        }
        final Party lenderIdentity = lenderIdentities.iterator().next();

        Product etf= createETF(ticker,Integer.parseInt(requestedQty),products,myIdentity);
        Basket basket = new Basket(myIdentity,lenderIdentity,products,etf);
        return Response.status(CREATED).entity("Test").build();

    }
    private   Product createETF(final String ticker, final Integer quantity, Set<Product> underlying,AbstractParty owner) {


        return new Product("SNY5","SNY5Sd", "Equity", "ETF",
                "SNP5", "Information Technology", "NYSE", ProductState.ACTIVE.name(),
                10.00, 10.00, underlying.stream().collect(Collectors.toMap(Product::getTicker, Product::getQuantity)), owner, "SNY5.X", 10.00, quantity);

    }

}
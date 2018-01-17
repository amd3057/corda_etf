package net.corda.hackathon.ralcog01.etf.api;


import com.google.common.collect.ImmutableMap;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.messaging.FlowHandle;
import net.corda.core.transactions.SignedTransaction;
import net.corda.hackathon.ralcog01.etf.*;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.CREATED;

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
    public List<Product> products() {

        return rpcOps.vaultQuery(Product.class).getStates().stream().map((p) -> p.getState().getData()).collect(Collectors.toList());
    }


    @GET
    @Path("etfs")
    @Produces(MediaType.APPLICATION_JSON)
    public Product etfs() {

        //return rpcOps.vaultQuery(Product.class).getStates().stream().map((p) -> p.getState().getData()).collect(Collectors.toList());
        List<Product> underlying = new ArrayList<>();

        underlying.add(ProductBuilder.setAndGetProductStaticData("AAPL",175.00,100,myIdentity));
        underlying.add(ProductBuilder.setAndGetProductStaticData("MSFT",475.00,100,myIdentity));
        underlying.add(ProductBuilder.setAndGetProductStaticData("AMZN",189.00,100,myIdentity));
        underlying.add(ProductBuilder.setAndGetProductStaticData("FB",115.00,100,myIdentity));
        underlying.add(ProductBuilder.setAndGetProductStaticData("BRK.B",96.00,100,myIdentity));

        List<ProductQty> lists = new ArrayList<>();
        for (Product p : underlying) {
            lists.add(new ProductQty(p.getTicker(), p.getQuantity()));
        }

        // 1. Get party objects for the counterparty.
        final Set<Party> lenderIdentities = rpcOps.partiesFromName("PartyA", false);
        if (lenderIdentities.size() != 1) {
            final String errMsg = String.format("Found %d identities for the lender.", lenderIdentities.size());
            throw new IllegalStateException(errMsg);
        }
        final Party lenderIdentity = lenderIdentities.iterator().next();

        return new Product("SNY5", "SNY5Sd", "Equity", "ETF",
                "SNP5", "Information Technology", "NYSE", ProductState.ACTIVE.name(),
                10.00, 10.00, lists, lenderIdentity, "SNY5.X", 10.00, 50000);
    }

    @GET
    @Path("create")
    public Response createOrder(
            @QueryParam(value = "ticker") String ticker,
            @QueryParam(value = "quantity") String requestedQty) {



        try{

            List<Product> products = new ArrayList<>();

            products.add(ProductBuilder.setAndGetProductStaticData("AAPL",175.00,100,myIdentity));
            products.add(ProductBuilder.setAndGetProductStaticData("MSFT",475.00,100,myIdentity));
            products.add(ProductBuilder.setAndGetProductStaticData("AMZN",189.00,100,myIdentity));
            products.add(ProductBuilder.setAndGetProductStaticData("FB",115.00,100,myIdentity));
            products.add(ProductBuilder.setAndGetProductStaticData("BRK.B",96.00,100,myIdentity));
            if(products.size()==0)
            {
                final String msg = String.format("Product Size is sent %s from valut \n%s",
                        "", products);
                return Response.status(CREATED).entity(msg).build();
            }

//        List<Product> products = productsStream.filter((p) -> basketproducts.contains((p.getState().getData().getTicker()))).map((p) -> p.getState().getData())
//                .collect(Collectors.toList());


            // 1. Get party objects for the counterparty.
            final Set<Party> lenderIdentities = rpcOps.partiesFromName("PartyB", false);
            if (lenderIdentities.size() != 1) {
                final String errMsg = String.format("Found %d identities for the lender.", lenderIdentities.size());
                throw new IllegalStateException(errMsg);
            }
            final Party lenderIdentity = lenderIdentities.iterator().next();

            Product reqEtf = createETF(ticker, Integer.parseInt(requestedQty), products, myIdentity);
            Basket basket = new Basket(myIdentity, lenderIdentity, products, reqEtf);
            final FlowHandle<SignedTransaction> flowHandle = rpcOps.startFlowDynamic(IssueOrderFlow.Initiator.class, basket, lenderIdentity);
            flowHandle.getReturnValue().get();

            return Response.status(CREATED).entity("Success").build();
        } catch (Exception ex) {
            StringWriter errors = new StringWriter();
            ex.printStackTrace(new PrintWriter(errors));
            return Response.status(BAD_REQUEST).entity(errors.toString()).build();
        }

    }

    @GET
    @Path("confirm")
    public Response createOrder(
            @QueryParam(value = "linerId") String id) {
        try{

            final UniqueIdentifier linearId = UniqueIdentifier.Companion.fromString(id);
            // 1. Get party objects for the counterparty.
            final Set<Party> lenderIdentities = rpcOps.partiesFromName("PartyC", false);
            if (lenderIdentities.size() != 1) {
                final String errMsg = String.format("Found %d identities for the lender.", lenderIdentities.size());
                throw new IllegalStateException(errMsg);
            }
            final Party lenderIdentity = lenderIdentities.iterator().next();

            final FlowHandle<SignedTransaction> flowHandle = rpcOps.startFlowDynamic(ValidateAndNotifySponsorFlow.class, linearId, lenderIdentity);
            flowHandle.getReturnValue().get();

            return Response.status(CREATED).entity("Success").build();
        } catch (Exception ex) {
            StringWriter errors = new StringWriter();
            ex.printStackTrace(new PrintWriter(errors));
            return Response.status(BAD_REQUEST).entity(errors.toString()).build();
        }

    }

    private Product createETF(final String ticker, final Integer quantity, List<Product> underlying, AbstractParty owner) {

        List<ProductQty> lists = new ArrayList<>();
        for (Product p : underlying) {
            lists.add(new ProductQty(p.getTicker(), p.getQuantity()));
        }

        return new Product("SNY5", "SNY5Sd", "Equity", "ETF",
                "SNP5", "Information Technology", "NYSE", ProductState.ACTIVE.name(),
                10.00, 10.00, lists, owner, "SNY5.X", 10.00, quantity);

    }

}
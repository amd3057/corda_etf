package net.corda.hackathon.ralcog01.etf;

import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.CommandAndState;
import net.corda.core.contracts.OwnableState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.serialization.CordaSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Map;

public class Product implements OwnableState {

    private final String ticker;
    private final String sedol;
    private final String assetClass;
    private final String productType;
    private final String name;
    private final String sector;
    private final String exchange;
    private final String state;
    private final double marketValue;
    private final double notionalValue;
    private final OurMap<String, Integer> productMap;
    private final AbstractParty owner;

    // dynamic - to be sent from the client
    private final String isin;
    private final double price;
    private final Integer quantity;

    @NotNull
    @Override
    public AbstractParty getOwner() {
        return owner;
    }

    @NotNull
    @Override
    public CommandAndState withNewOwner(AbstractParty newOwner) {
        return new CommandAndState(new ProductContract.Create(), this);
    }

    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return ImmutableList.of(owner);
    }

     public Product(String ticker, String sedol, String assetClass, String productType, String name, String sector,
             String exchange, String state, double marketValue, double notionalValue,
                    OurMap<String, Integer> productMap, AbstractParty owner, String isin, double price, Integer quantity) {

        this.ticker = ticker;
        this.sedol = sedol;
        this.assetClass = assetClass;
        this.productType = productType;
        this.name = name;
        this.sector = sector;
        this.exchange = exchange;
        this.state = state;
        this.marketValue = marketValue;
        this.notionalValue = notionalValue;
        this.productMap = productMap;
        this.owner = owner;
        this.isin = isin;
        this.price = price;
        this.quantity = quantity;
    }

    static Product createProduct (String ticker, AbstractParty owner, final double price, final Integer quantity) {
        return ProductBuilder.setAndGetProductStaticData(ticker, price, quantity, owner);
    }

    public String getTicker() {
        return ticker;
    }

    public String getSedol() {
        return sedol;
    }

    public String getAssetClass() {
        return assetClass;
    }

    public String getProductType() {
        return productType;
    }

    public String getName() {
        return name;
    }

    public String getSector() {
        return sector;
    }

    public String getExchange() {
        return exchange;
    }

    public String getState() {
        return state;
    }

    public double getMarketValue() {
        return marketValue;
    }

    public double getNotionalValue() {
        return notionalValue;
    }

    public Map<String, Integer> getProductMap() {
        return productMap;
    }

    public String getIsin() {
        return isin;
    }

    public double getPrice() {
        return price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product)) return false;
        Product product = (Product) o;
        return Objects.equals(getTicker(), product.getTicker());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTicker());
    }

    @Override
    public String toString() {
        return "Product{" +
                "ticker='" + ticker + '\'' +
                ", quantity=" + quantity +
                '}';
    }


    @CordaSerializable
    public static class OurMap<String, Integer>  extends HashMap<String, Integer> {
    }
}

package net.corda.hackathon.ralcog01.etf;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import net.corda.core.contracts.Amount;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.crypto.NullKeys;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;

import java.security.PublicKey;
import java.util.*;
import java.util.stream.Collectors;

import static net.corda.core.utilities.EncodingUtils.toBase58String;

public class Product implements LinearState {
    private final String ticker;
    private final String name;
    private final String assetClass;
    private final Amount<Currency> price;
    private final Integer quantity;
    private final Amount<Currency> marketValue;
    private final Amount<Currency> notionalValue;
    private final String sector;
    private final String sedol;
    private final String isin;
    private final String exchange;
    private final String state;
    private final ProductType productType;
    private final Set<Product> products;
    private final AbstractParty lender;
    private final AbstractParty borrower;
    private final UniqueIdentifier linearId;

    public AbstractParty getLender() {
        return lender;
    }

    public AbstractParty getBorrower() {
        return borrower;
    }

    public String getTicker() {
        return ticker;
    }

    public String getName() {
        return name;
    }

    public String getAssetClass() {
        return assetClass;
    }

    public Amount<Currency> getPrice() {
        return price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Amount<Currency> getMarketValue() {
        return marketValue;
    }

    public Amount<Currency> getNotionalValue() {
        return notionalValue;
    }

    public String getSector() {
        return sector;
    }

    public String getSedol() {
        return sedol;
    }

    public String getIsin() {
        return isin;
    }

    public String getExchange() {
        return exchange;
    }

    public String getState() {
        return state;
    }

    public ProductType getProductType() {
        return productType;
    }

    public Set<Product> getProducts() {
        return products;
    }

    @Override
    public UniqueIdentifier getLinearId() {
        return linearId;
    }

    @Override
    public List<AbstractParty> getParticipants() {
        return ImmutableList.of(lender, borrower);
    }

    public Product pay(Amount<Currency> amountToPay) {
        return new Product(
                this.amount,
                this.lender,
                this.borrower,
                this.paid.plus(amountToPay),
                this.linearId
        );
    }

    public Product(String ticker, String name, String assetClass, Amount<Currency> price, Integer quantity, Amount<Currency> marketValue, Amount<Currency> notionalValue, String sector, String sedol, String isin, String exchange, String state, ProductType productType, Set<Product> products, AbstractParty lender, AbstractParty borrower, UniqueIdentifier linearId) {
        this.ticker = ticker;
        this.name = name;
        this.assetClass = assetClass;
        this.price = price;
        this.quantity = quantity;
        this.marketValue = marketValue;
        this.notionalValue = notionalValue;
        this.sector = sector;
        this.sedol = sedol;
        this.isin = isin;
        this.exchange = exchange;
        this.state = state;
        this.productType = productType;
        this.products = products;
        this.lender = lender;
        this.borrower = borrower;
        this.linearId = linearId;
    }

    public Product(String ticker, String name, String assetClass, Amount<Currency> price, Integer quantity, Amount<Currency> marketValue, Amount<Currency> notionalValue, String sector, String sedol, String isin, String exchange, String state, ProductType productType, Set<Product> products, AbstractParty lender, AbstractParty borrower) {
        this.ticker = ticker;
        this.name = name;
        this.assetClass = assetClass;
        this.price = price;
        this.quantity = quantity;
        this.marketValue = marketValue;
        this.notionalValue = notionalValue;
        this.sector = sector;
        this.sedol = sedol;
        this.isin = isin;
        this.exchange = exchange;
        this.state = state;
        this.productType = productType;
        this.products = products;
        this.lender = lender;
        this.borrower = borrower;
        this.linearId = new UniqueIdentifier();
    }

    public Product(String ticker, String name, String assetClass, Amount<Currency> price, Integer quantity, Amount<Currency> marketValue, Amount<Currency> notionalValue, String sector, String sedol, String isin, String exchange, String state, ProductType productType, AbstractParty lender, AbstractParty borrower) {
        this.ticker = ticker;
        this.name = name;
        this.assetClass = assetClass;
        this.price = price;
        this.quantity = quantity;
        this.marketValue = marketValue;
        this.notionalValue = notionalValue;
        this.sector = sector;
        this.sedol = sedol;
        this.isin = isin;
        this.exchange = exchange;
        this.state = state;
        this.productType = productType;
        this.products = Collections.EMPTY_SET;
        this.lender = lender;
        this.borrower = borrower;
        this.linearId = new UniqueIdentifier();
    }

    public Product withNewLender(AbstractParty newLender) {
        return new Product(this.ticker, this.name, this.assetClass, this.price, this.quantity, this.marketValue, this.notionalValue, this.sector, this.sedol,
                this.isin, this.exchange, this.state, this.productType, this.products, newLender, this.borrower, this.linearId);
    }

    public Product withoutLender() {
        return new Product(this.ticker, this.name, this.assetClass, this.price, this.quantity, this.marketValue, this.notionalValue, this.sector, this.sedol,
                this.isin, this.exchange, this.state, this.productType, this.products, NullKeys.INSTANCE.getNULL_PARTY(), this.borrower, this.linearId);
    }

    public List<PublicKey> getParticipantKeys() {
        return getParticipants().stream().map(AbstractParty::getOwningKey).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        String lenderString;
        if (this.lender instanceof Party) {
            lenderString = ((Party) lender).getName().getOrganisation();
        } else {
            PublicKey lenderKey = this.lender.getOwningKey();
            lenderString = toBase58String(lenderKey);
        }

        String borrowerString;
        if (this.borrower instanceof Party) {
            borrowerString = ((Party) borrower).getName().getOrganisation();
        } else {
            PublicKey borrowerKey = this.borrower.getOwningKey();
            borrowerString = toBase58String(borrowerKey);
        }

        return String.format("Obligation(%s): %s owes %s %s and has paid %s so far.",
                this.linearId, borrowerString, lenderString, this.amount, this.paid);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product)) return false;
        Product product = (Product) o;
        return Objects.equals(getTicker(), product.getTicker()) &&
                Objects.equals(getName(), product.getName()) &&
                Objects.equals(getAssetClass(), product.getAssetClass()) &&
                Objects.equals(getPrice(), product.getPrice()) &&
                Objects.equals(getQuantity(), product.getQuantity()) &&
                Objects.equals(getMarketValue(), product.getMarketValue()) &&
                Objects.equals(getNotionalValue(), product.getNotionalValue()) &&
                Objects.equals(getSector(), product.getSector()) &&
                Objects.equals(getSedol(), product.getSedol()) &&
                Objects.equals(getIsin(), product.getIsin()) &&
                Objects.equals(getExchange(), product.getExchange()) &&
                Objects.equals(getState(), product.getState()) &&
                getProductType() == product.getProductType() &&
                Objects.equals(getProducts(), product.getProducts()) &&
                Objects.equals(getLender(), product.getLender()) &&
                Objects.equals(getBorrower(), product.getBorrower()) &&
                Objects.equals(getLinearId(), product.getLinearId());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getTicker(), getName(), getAssetClass(), getPrice(), getQuantity(), getMarketValue(), getNotionalValue(), getSector(), getSedol(), getIsin(), getExchange(), getState(), getProductType(), getProducts(), getLender(), getBorrower(), getLinearId());
    }
}
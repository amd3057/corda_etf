package net.corda.hackathon.ralcog01.etf;

import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import org.jetbrains.annotations.NotNull;

import java.security.PublicKey;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class Basket implements LinearState {

    private final AbstractParty issuer;
    private final AbstractParty owner;
    private final Set<Product> products;
    private final Product reqProduct;
    private final UniqueIdentifier linearId;

    public Basket(AbstractParty issuer, AbstractParty owner, Set<Product> products, Product reqProduct, UniqueIdentifier linearId) {
        this.issuer = issuer;
        this.owner = owner;
        this.products = products;
        this.reqProduct = reqProduct;
        this.linearId = linearId;
    }

    public Basket(AbstractParty issuer, AbstractParty owner, Set<Product> products, Product reqProduct) {
        this.issuer = issuer;
        this.owner = owner;
        this.products = products;
        this.reqProduct = reqProduct;
        this.linearId = new UniqueIdentifier();
    }

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return this.linearId;
    }

    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return ImmutableList.of(issuer, owner);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Basket)) return false;
        Basket basket = (Basket) o;
        return Objects.equals(reqProduct, basket.reqProduct);
    }

    @Override
    public int hashCode() {

        return Objects.hash(reqProduct);
    }

    @Override
    public String toString() {
        return "Basket{" +
                "reqProduct=" + reqProduct.toString() +
                '}';
    }

    public AbstractParty getIssuer() {
        return issuer;
    }

    public AbstractParty getOwner() {
        return owner;
    }

    public Set<Product> getProducts() {
        return products;
    }

    public Product getReqProduct() {
        return reqProduct;
    }

    public List<PublicKey> getParticipantKeys() {
        return getParticipants().stream().map(AbstractParty::getOwningKey).collect(Collectors.toList());
    }
}

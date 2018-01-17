package net.corda.hackathon.ralcog01.etf;

import net.corda.core.serialization.CordaSerializable;

@CordaSerializable
public class ProductQty {
    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public ProductQty( String ticker,int quantity) {
        this.quantity = quantity;
        this.ticker = ticker;
    }

    private int quantity;
    private String ticker;

}

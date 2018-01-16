package net.corda.hackathon.ralcog01.etf;

import net.corda.core.contracts.Amount;
import net.corda.core.identity.AbstractParty;

import java.util.Collections;
import java.util.Currency;

public enum ProductBuilder {

    instance;

    static Product setAndGetProductStaticData(final String ticker, final double price, final Integer quantity, AbstractParty owner) {
         if ("AAPL".equals(ticker)) {
             return new Product("AAPL","AAPL123", "Equity", "STOCK",
                      "Apple", "Information Technology", "NYSE", ProductState.ACTIVE.name(),
                     10000.00, 10000.00, Collections.EMPTY_MAP, owner, "APPL.X", price, quantity);
         }
         return null;
    }
}

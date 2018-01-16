package net.corda.hackathon.ralcog01.etf;

import net.corda.core.contracts.Amount;
import net.corda.core.identity.AbstractParty;

import java.util.Collections;
import java.util.Currency;

public enum EtfUtil {

    instance;

    static Product setAndGetProductStaticData(final String ticker, final double price, final Integer quantity, AbstractParty owner) {
         if ("AAPL".equals(ticker)) {
             return new Product("AAPL","", "Equity", ProductType.STOCK, "Apple", "", "NYSE", ProductState.ACTIVE,
                     10000.00, 10000.00, Collections.EMPTY_SET, owner, "", price, quantity);
         }
         return null;
    }
}

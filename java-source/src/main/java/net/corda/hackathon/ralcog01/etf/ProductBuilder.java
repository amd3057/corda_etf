package net.corda.hackathon.ralcog01.etf;

import net.corda.core.contracts.Amount;
import net.corda.core.identity.AbstractParty;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public enum ProductBuilder {

    instance;

    static Product setAndGetProductStaticData(final String ticker, final double price, final Integer quantity, AbstractParty owner) {
         if ("AAPL".equals(ticker)) {
             return new Product("AAPL","AAPL123", "Equity", "STOCK",
                      "Apple", "Information Technology", "NYSE", ProductState.ACTIVE.name(),
                     10000.00, 10000.00, new ArrayList<>(), owner, "APPL.X", price, quantity);
         }
        if ("ETF".equals(ticker)) {
            List<ProductQty> list = new ArrayList<>();
            list.add(new ProductQty("AAPL",100);
            list.add(new ProductQty("MSFT",100);
            list.add(new ProductQty("AMZN",100);
            list.add(new ProductQty("FB",100);
            list.add(new ProductQty("BRK.B",100);

            return new Product("SNY5","SNY5123", "Equity", "ETF",
                    "SNY5", "SNP 5", "NYSE", ProductState.ACTIVE.name(),
                    10000.00, 10000.00, list, owner, "SNY5.X", price, quantity);
        }
         return null;
    }

}

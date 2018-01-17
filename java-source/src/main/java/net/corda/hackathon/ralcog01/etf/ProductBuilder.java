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


        if ("MSFT".equals(ticker)) {
            return new Product("MSFT","MSFT123", "Equity", "STOCK",
                    "Microsoft", "Information Technology", "NYSE", ProductState.ACTIVE.name(),
                    5000.00, 5000.00, new ArrayList<>(), owner, "MSFT.X", price, quantity);
        }

        if ("AMZN".equals(ticker)) {
            return new Product("AMZN","AMZNT123", "Equity", "STOCK",
                    "Amazon", "Information Technology", "NYSE", ProductState.ACTIVE.name(),
                    5000.00, 5000.00, new ArrayList<>(), owner, "AMZN.X", price, quantity);
        }

        if ("FB".equals(ticker)) {
            return new Product("FB","FB123", "Equity", "STOCK",
                    "Facebook", "Social Media", "NYSE", ProductState.ACTIVE.name(),
                    2500.00, 2500.00, new ArrayList<>(), owner, "FB.X", price, quantity);
        }

        if ("BRK.B".equals(ticker)) {
            return new Product("BRK.B","BRK.B123", "Equity", "STOCK",
                    "Blackrock", "Financial Services", "NYSE", ProductState.ACTIVE.name(),
                    2000.00, 2000.00, new ArrayList<>(), owner, "BRK.B", price, quantity);
        }


        if ("ETF".equals(ticker)) {
            List<ProductQty> list = new ArrayList<>();
            list.add(new ProductQty("AAPL",100));
            list.add(new ProductQty("MSFT",100));
            list.add(new ProductQty("AMZN",100));
            list.add(new ProductQty("FB",100));
            list.add(new ProductQty("BRK.B",100));

            return new Product("SNY5","SNY5123", "Equity", "ETF",
                    "SNY5", "SNP 5", "NYSE", ProductState.ACTIVE.name(),
                    10000.00, 10000.00, list, owner, "SNY5.X", price, quantity);
        }
         return null;
    }

}

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
                     10000.00, 10000.00, Collections.EMPTY_MAP, owner, "APPL.X", price, quantity);
         }
        if ("ETF".equals(ticker)) {
            Map<String,Integer> maps = new HashMap<>();
            maps.put("AAPL",100);
            maps.put("MSFT",100);
            maps.put("AMZN",100);
            maps.put("FB",100);
            maps.put("BRK.B",100);

            return new Product("SNY5","SNY5123", "Equity", "ETF",
                    "SNY5", "SNP 5", "NYSE", ProductState.ACTIVE.name(),
                    10000.00, 10000.00, maps, owner, "SNY5.X", price, quantity);
        }
         return null;
    }

   public static Product createETF(final String ticker, final Integer quantity, List<Product> underlying,AbstractParty owner) {


            return new Product("SNY5","SNY5Sd", "Equity", "ETF",
                    "SNP5", "Information Technology", "NYSE", ProductState.ACTIVE.name(),
                    10.00, 10.00, underlying.stream().collect(Collectors.toMap(Product::getTicker, Product::getQuantity)), owner, "SNY5.X", 10.00, quantity);

    }
}

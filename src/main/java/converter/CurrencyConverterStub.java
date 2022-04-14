package converter;

import java.util.AbstractMap;
import java.util.Map;
import models.Currency;

public class CurrencyConverterStub implements CurrencyConverter {

    private final Map<CurrencyPair, Double> exchangeRate;

    public CurrencyConverterStub(Map<CurrencyPair, Double> exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    @Override
    public double convert(double amount, Currency from, Currency to) {
        return amount * exchangeRate.get(new CurrencyPair(from ,to));
    }

    public static record CurrencyPair(Currency from, Currency to) {}
}

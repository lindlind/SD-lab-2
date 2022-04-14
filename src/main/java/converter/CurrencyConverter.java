package converter;

import models.Currency;

public interface CurrencyConverter {

    double convert(double amount, Currency from, Currency to);

}

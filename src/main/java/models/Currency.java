package models;

public enum Currency {
    RUB,
    USD,
    EUR,
    ;

    public static Currency fromString(String str) {
        for (Currency value : Currency.values()) {
            if (value.name().equalsIgnoreCase(str)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unexpected currency: " + str);
    }
}

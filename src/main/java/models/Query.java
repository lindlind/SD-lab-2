package models;

public enum Query {
    ADD_USER,
    DELETE_USER,

    ADD_PRODUCT,
    DELETE_PRODUCT,
    SHOW_PRODUCTS,
    ;

    public static Query fromString(String str) {
        for (Query value : Query.values()) {
            if (value.name().equalsIgnoreCase(str)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unexpected query: " + str);
    }
}

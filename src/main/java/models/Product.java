package models;

public record Product(
    String name,
    double price,
    Currency currency
) { }

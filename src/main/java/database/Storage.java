package database;

import models.Product;
import models.User;
import rx.Observable;

public interface Storage {
    Observable<Boolean> addUser(User user);
    Observable<Boolean> deleteUser(String id);
    Observable<User> getUser(String id);

    Observable<Boolean> addProduct(Product product);
    Observable<Boolean> deleteProduct(String name);
    Observable<Product> getProduct(String name);
    Observable<Product> getProducts(User user);
}

package database;

import com.mongodb.client.model.Filters;
import com.mongodb.rx.client.MongoDatabase;
import converter.CurrencyConverter;
import java.util.Map;
import models.Currency;
import models.Product;
import models.User;
import org.bson.Document;
import rx.Observable;
import rx.Scheduler;
import rx.schedulers.Schedulers;

public class MongoDbStorage implements Storage {

    private final MongoDatabase db;
    private final CurrencyConverter converter;
    private final Scheduler scheduler;

    public MongoDbStorage(MongoDatabase db, CurrencyConverter converter) {
        this.db = db;
        this.converter = converter;
        this.scheduler = Schedulers.io();
    }

    @Override
    public Observable<Boolean> addUser(User user) {
        return getUser(user.id())
            .count()
            .flatMap(existed -> {
                if (existed > 0) {
                    return Observable.just(false);
                } else {
                    return db.getCollection("users")
                        .insertOne(toDoc(user))
                        .count()
                        .map(inserted -> inserted > 0);
                }
            })
            .subscribeOn(scheduler);
    }

    @Override
    public Observable<Boolean> deleteUser(String id) {
        return db.getCollection("users")
            .deleteOne(Filters.eq("id", id))
            .count()
            .map(removed -> removed > 0)
            .subscribeOn(scheduler);
    }

    @Override
    public Observable<User> getUser(String id) {
        return db.getCollection("users")
            .find(Filters.eq("id", id))
            .toObservable()
            .map(this::parseUser)
            .subscribeOn(scheduler);
    }

    @Override
    public Observable<Boolean> addProduct(Product product) {
        return getProduct(product.name())
            .count()
            .flatMap(existed -> {
                if (existed > 0) {
                    return Observable.just(false);
                } else {
                    return db.getCollection("products")
                        .insertOne(toDoc(product))
                        .isEmpty()
                        .map(x -> !x);
                }
            })
            .subscribeOn(scheduler);
    }

    @Override
    public Observable<Boolean> deleteProduct(String name) {
        return db.getCollection("products")
            .deleteOne(Filters.eq("name", name))
            .count()
            .map(removed -> removed > 0)
            .subscribeOn(scheduler);
    }

    @Override
    public Observable<Product> getProduct(String name) {
        return db.getCollection("products")
            .find(Filters.eq("name", name))
            .toObservable()
            .map(this::parseProduct)
            .subscribeOn(scheduler);
    }

    @Override
    public Observable<Product> getProducts(User user) {
        return db.getCollection("products")
            .find()
            .toObservable()
            .map(this::parseProduct)
            .map(product -> new Product(
                product.name(),
                converter.convert(product.price(), product.currency(), user.currency()),
                user.currency()
            ))
            .subscribeOn(scheduler);
    }

    private User parseUser(Document docUser) {
        return new User(
            docUser.getString("id"),
            Currency.fromString(docUser.getString("currency"))
        );
    }

    private Document toDoc(User user) {
        return new Document(Map.of(
            "id", user.id(),
            "currency", user.currency().name()
        ));
    }

    private Product parseProduct(Document docProduct) {
        return new Product(
            docProduct.getString("name"),
            docProduct.getDouble("price"),
            Currency.fromString(docProduct.getString("currency"))
        );
    }

    private Document toDoc(Product product) {
        return new Document(Map.of(
            "name", product.name(),
            "price", product.price(),
            "currency", product.currency().name()
        ));
    }
}

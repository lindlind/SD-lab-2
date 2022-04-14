import com.mongodb.rx.client.MongoClients;
import converter.CurrencyConverterStub;
import database.MongoDbStorage;
import database.Storage;
import io.reactivex.netty.protocol.http.server.HttpServer;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import models.Currency;
import models.Product;
import models.Query;
import models.User;
import rx.Observable;

public class Main {

    public static void main(String[] args) {

        Storage storage = new MongoDbStorage(
            MongoClients.create("mongodb://127.0.0.1:27017").getDatabase("lab2"),
            new CurrencyConverterStub(Map.of(
                new CurrencyConverterStub.CurrencyPair(Currency.RUB, Currency.RUB), 1.,
                new CurrencyConverterStub.CurrencyPair(Currency.RUB, Currency.USD), 1. / 78,
                new CurrencyConverterStub.CurrencyPair(Currency.RUB, Currency.EUR), 1. / 88.,
                new CurrencyConverterStub.CurrencyPair(Currency.USD, Currency.RUB), 72.,
                new CurrencyConverterStub.CurrencyPair(Currency.USD, Currency.USD), 1.,
                new CurrencyConverterStub.CurrencyPair(Currency.USD, Currency.EUR), 72. / 88,
                new CurrencyConverterStub.CurrencyPair(Currency.EUR, Currency.RUB), 82.,
                new CurrencyConverterStub.CurrencyPair(Currency.EUR, Currency.USD), 82. / 78,
                new CurrencyConverterStub.CurrencyPair(Currency.EUR, Currency.EUR), 1.
            ))
        );
        HttpServer.newServer(8080)
            .start((request, response) -> {
                try {
                    Query query = Query.fromString(parseQuery(request));
                    switch (query) {
                        case ADD_USER:
                            final User user = new User(
                                parseParam(request, "id"),
                                Currency.fromString(parseParam(request, "currency"))
                            );
                            return response.writeString(Observable.just(user)
                                .flatMap(storage::addUser)
                                .map(Object::toString)
                            );
                        case DELETE_USER:
                            return response.writeString(Observable.just(parseParam(request, "id"))
                                .flatMap(storage::deleteUser)
                                .map(Object::toString)
                            );
                        case ADD_PRODUCT:
                            final Product product = new Product(
                                parseParam(request, "name"),
                                Double.parseDouble(parseParam(request, "price")),
                                Currency.fromString(parseParam(request, "currency"))
                            );
                            return response.writeString(Observable.just(product)
                                .flatMap(storage::addProduct)
                                .map(Object::toString)
                            );
                        case DELETE_PRODUCT:
                            return response.writeString(Observable.just(parseParam(request, "name"))
                                .flatMap(storage::deleteProduct)
                                .map(Object::toString)
                            );
                        case SHOW_PRODUCTS:
                            return response.writeString(Observable.just(parseParam(request, "user_id"))
                                .flatMap(storage::getUser)
                                .flatMap(storage::getProducts)
                                .map(p -> p.toString() + "\n")
                                .switchIfEmpty(Observable.just("No results"))
                            );
                        default:
                            return response.writeString(Observable.just("Illegal request: " + query.name()));
                    }
                } catch (IllegalArgumentException e) {
                    return response.writeString(Observable.just("Illegal request: " + e.getMessage()));
                }
            })
            .awaitShutdown();
    }

    private static <T> String parseQuery(HttpServerRequest<T> request) {
        final String path = request.getDecodedPath();
        if (path.length() < 2) {
            throw new IllegalArgumentException("Empty query");
        }
        return path.substring(1);
    }

    private static <T> String parseParam(HttpServerRequest<T> request, String key) {
        final List<String> values = request.getQueryParameters().get(key);
        if (values == null) {
            throw new IllegalArgumentException("Expected but not found query param " + key);
        }
        return request.getQueryParameters().get(key).get(0);
    }
}

package myratpackexamples.webpoll;

import com.mongodb.async.client.MongoCollection;
import io.vavr.control.Validation;
import lombok.Value;
import org.bson.Document;
import org.bson.conversions.Bson;
import ratpack.exec.Promise;

import java.util.ArrayList;

import static io.vavr.control.Validation.invalid;
import static io.vavr.control.Validation.valid;
import static ratpack.exec.Promise.async;

public class RatpackMongoClient {

    public static Promise<Void> insertOne(MongoCollection<Document> collection, Document document) {
        return async(downstream -> collection.insertOne(document, (result, throwable) -> {
            if (throwable != null) {
                downstream.error(throwable);
            } else {
                downstream.success(result);
            }
        }));
    }

    public abstract static class FindOneError {
    }

    @Value
    public static class ExactlyOneElementExpected extends FindOneError {
    }


    @Value
    public static class InvalidIdString extends FindOneError {
        String idString;
    }

    @Value
    public static class MongoError extends FindOneError {
        Throwable throwable;
    }

    public static Promise<Validation<FindOneError, Document>> findOne(MongoCollection<Document> collection, Bson filter) {
        return async(downstream -> collection.find(filter).into(new ArrayList<>(), (result, throwable) -> {
            if (throwable != null) {
                downstream.success(invalid(new MongoError(throwable)));
            } else {
                if (result.size() != 1) {
                    downstream.success(invalid(new ExactlyOneElementExpected()));
                } else {
                    downstream.success(valid(result.get(0)));
                }
            }
        }));
    }
}

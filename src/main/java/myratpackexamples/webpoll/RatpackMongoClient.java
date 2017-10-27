package myratpackexamples.webpoll;

import com.mongodb.async.client.MongoCollection;
import com.mongodb.client.model.Filters;
import io.vavr.control.Validation;
import lombok.Value;
import myratpackexamples.promises.ValidationUtil;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import ratpack.exec.Promise;

import java.util.ArrayList;

import static io.vavr.control.Validation.invalid;
import static io.vavr.control.Validation.valid;
import static ratpack.exec.Promise.async;

public class RatpackMongoClient {

    public abstract static class InsertOneError {
    }

    @Value
    public static class InsertOneMongoError extends InsertOneError {
        Throwable throwable;
    }

    public static Promise<Validation<InsertOneError, Void>> insertOne(
            MongoCollection<Document> collection, Document document
    ) {
        return async(downstream -> collection.insertOne(document, (result, throwable) -> {
            if (throwable != null) {
                downstream.success(invalid(new InsertOneMongoError(throwable)));
            } else {
                downstream.success(valid(result));
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
    public static class FindOneMongoError extends FindOneError {
        Throwable throwable;
    }

    public static Promise<Validation<FindOneError, Document>> findOneById(
            MongoCollection<Document> collection, String hexIdString
    ) {
        Validation<FindOneError, ObjectId> mongoObjectId = createMongoObjectId(hexIdString);

        return ValidationUtil.flatMapPromise(mongoObjectId, objectId ->
                findOne(collection, Filters.eq("_id", objectId)));
    }

    private static Validation<FindOneError, ObjectId> createMongoObjectId(String hexIdString) {
        try {
            return valid(new ObjectId(hexIdString));

        } catch (IllegalArgumentException e) {
            return invalid(new InvalidIdString(hexIdString));
        }
    }

    private static Promise<Validation<FindOneError, Document>> findOne(MongoCollection<Document> collection, Bson filter) {
        return async(downstream -> collection.find(filter).into(new ArrayList<>(), (result, throwable) -> {
            if (throwable != null) {
                downstream.success(invalid(new FindOneMongoError(throwable)));
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

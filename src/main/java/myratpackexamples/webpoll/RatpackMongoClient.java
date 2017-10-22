package myratpackexamples.webpoll;

import com.mongodb.async.client.MongoCollection;
import org.bson.Document;
import org.bson.conversions.Bson;
import ratpack.exec.Promise;

import java.util.ArrayList;

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

    public static Promise<Document> findOne(MongoCollection<Document> collection, Bson filter) {
        return async(downstream -> collection.find(filter).into(new ArrayList<>(), (result, throwable) -> {
            if (throwable != null) {
                downstream.error(throwable);
            } else {
                if (result.size() != 1) throw new RuntimeException("Exactly one element expected!");
                downstream.success(result.get(0));
            }
        }));
    }
}

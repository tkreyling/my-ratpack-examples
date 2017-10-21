package myratpackexamples.webpoll;

import com.mongodb.async.client.MongoCollection;
import org.bson.Document;
import ratpack.exec.Promise;

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
}

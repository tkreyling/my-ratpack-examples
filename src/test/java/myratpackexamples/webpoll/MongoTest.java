package myratpackexamples.webpoll;

import com.mongodb.async.client.MongoClient;
import com.mongodb.async.client.MongoClients;
import com.mongodb.async.client.MongoCollection;
import com.mongodb.async.client.MongoDatabase;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import ratpack.exec.Operation;
import ratpack.test.exec.ExecHarness;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class MongoTest {

    @Test
    void testDb() throws Exception {
        ExecHarness.executeSingle(Operation.of(() -> {
            try (InMemoryMongoDb inMemoryMongoDb = new InMemoryMongoDb()) {

                MongoClient mongo = MongoClients.create(inMemoryMongoDb.getConnectionString());
                MongoDatabase database = mongo.getDatabase("test");

                CountDownLatch latch = new CountDownLatch(1);

                MongoCollection<Document> collection = database.getCollection("test");

                Document doc = new Document("name", "MongoDB")
                        .append("type", "database")
                        .append("count", 1)
                        .append("info", new Document("x", 203).append("y", 102));

                RatpackMongoClient.insertOne(collection, doc)
                        .result(voidExecResult -> {
                            System.out.println("inserted!");
                            latch.countDown();
                        });

                latch.await(2, TimeUnit.SECONDS);
            }
        }));
    }
}

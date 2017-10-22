package myratpackexamples.webpoll;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MongoTest {
    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testDb() throws Exception {
        ExecHarness.executeSingle(Operation.of(() -> {
            try (InMemoryMongoDb inMemoryMongoDb = new InMemoryMongoDb()) {

                MongoClient mongo = MongoClients.create(inMemoryMongoDb.getConnectionString());
                MongoDatabase database = mongo.getDatabase("test");

                CountDownLatch latch = new CountDownLatch(1);

                MongoCollection<Document> collection = database.getCollection("test");

                Poll poll = new Poll(null, "Sport to play on Friday", asList("basketball"));
                String pollJson = objectMapper.writeValueAsString(poll);

                Document doc = Document.parse(pollJson);

                assertEquals("Sport to play on Friday", doc.getString("topic"));
                assertEquals(asList("basketball"), doc.get("options"));

                RatpackMongoClient.insertOne(collection, doc)
                        .result(voidExecResult -> {
                            System.out.println("inserted!");
                            latch.countDown();

                            assertNotNull(doc.get("_id"));
                        });

                latch.await(2, TimeUnit.SECONDS);
            }
        }));
    }
}

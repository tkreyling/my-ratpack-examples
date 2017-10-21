package myratpackexamples.webpoll;

import com.mongodb.async.client.MongoClient;
import com.mongodb.async.client.MongoClients;
import com.mongodb.async.client.MongoCollection;
import com.mongodb.async.client.MongoDatabase;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import ratpack.exec.Operation;
import ratpack.test.exec.ExecHarness;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class MongoTest {

    private static class InMemoryMongoDb implements Closeable {
        private final MongodExecutable mongodExecutable;

        private final String bindIp = "localhost";
        private final int port = 12345;

        public InMemoryMongoDb() throws IOException {
            MongodStarter starter = MongodStarter.getDefaultInstance();

            IMongodConfig mongodConfig = new MongodConfigBuilder()
                    .version(Version.Main.PRODUCTION)
                    .net(new Net(bindIp, port, Network.localhostIsIPv6()))
                    .build();

            mongodExecutable = starter.prepare(mongodConfig);
            mongodExecutable.start();
        }

        public String getConnectionString() {
            return "mongodb://" + bindIp + ":" + port;
        }

        @Override
        public void close() throws IOException {
            if (mongodExecutable != null)
                mongodExecutable.stop();
        }
    }

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

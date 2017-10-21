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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class MongoTest {
    @Test
    void testDb() throws Exception {
        ExecHarness.executeSingle(Operation.of(() -> {
            MongodStarter starter = MongodStarter.getDefaultInstance();

            String bindIp = "localhost";
            int port = 12345;
            IMongodConfig mongodConfig = new MongodConfigBuilder()
                    .version(Version.Main.PRODUCTION)
                    .net(new Net(bindIp, port, Network.localhostIsIPv6()))
                    .build();

            MongodExecutable mongodExecutable = null;
            try {
                CountDownLatch latch = new CountDownLatch(1);

                mongodExecutable = starter.prepare(mongodConfig);
                mongodExecutable.start();

                MongoClient mongo = MongoClients.create("mongodb://" + bindIp + ":" + port);
                MongoDatabase database = mongo.getDatabase("test");

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

            } finally {
                if (mongodExecutable != null)
                    mongodExecutable.stop();
            }
        }));
    }
}

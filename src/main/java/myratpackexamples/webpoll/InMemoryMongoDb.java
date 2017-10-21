package myratpackexamples.webpoll;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;

import java.io.Closeable;
import java.io.IOException;

class InMemoryMongoDb implements Closeable {
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

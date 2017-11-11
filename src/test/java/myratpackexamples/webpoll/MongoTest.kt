package myratpackexamples.webpoll

import com.fasterxml.jackson.databind.ObjectMapper
import com.mongodb.async.client.MongoClients
import org.bson.Document
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import ratpack.exec.Operation
import ratpack.test.exec.ExecHarness
import java.util.Arrays.asList
import java.util.Collections.emptyList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class MongoTest {
    private val objectMapper = ObjectMapper()

    @Test
    internal fun testDb() {
        ExecHarness.executeSingle(Operation.of {
            InMemoryMongoDb().use { inMemoryMongoDb ->

                val mongo = MongoClients.create(inMemoryMongoDb.connectionString)
                val database = mongo.getDatabase("test")

                val latch = CountDownLatch(1)

                val collection = database.getCollection("test")

                val poll = PollResponse.Poll(
                        null, "Sport to play on Friday", asList("basketball"), emptyList()
                )
                val pollJson = objectMapper.writeValueAsString(poll)

                val doc = Document.parse(pollJson)

                assertEquals("Sport to play on Friday", doc.getString("topic"))
                assertEquals(asList("basketball"), doc["options"])

                collection.insertOne(doc)
                        .result { voidExecResult ->
                            println("inserted!")
                            latch.countDown()

                            assertNotNull(doc["_id"])
                        }

                latch.await(2, TimeUnit.SECONDS)
            }
        })
    }
}

package myratpackexamples.webpoll

import com.fasterxml.jackson.databind.ObjectMapper
import com.mongodb.async.client.MongoClients
import com.mongodb.async.client.MongoCollection
import org.bson.Document
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import ratpack.exec.Operation
import ratpack.test.exec.ExecHarness
import java.util.Arrays.asList

@ExtendWith(InMemoryMongoDbJunitExtension::class)
class MongoTest {
    private val objectMapper = ObjectMapper()

    @Test
    fun `after inserting a document into mongo it has a mongo id`() {
        ExecHarness.executeSingle(Operation.of {
                val collection = createConnectionAndGetCollection()
                val doc = createPollDocument()

                collection.insertOne(doc)
                        .result { assertNotNull(doc["_id"]) }
        })
    }

    private fun createPollDocument(): Document {
        val poll = PollResponse.Poll(
                null, "Sport to play on Friday", asList("basketball"), emptyList()
        )
        val pollJson = objectMapper.writeValueAsString(poll)
        val doc = Document.parse(pollJson)

        assertEquals("Sport to play on Friday", doc.getString("topic"))
        assertEquals(asList("basketball"), doc["options"])

        return doc
    }

    private fun createConnectionAndGetCollection(): MongoCollection<Document> {
        val mongo = MongoClients.create("mongodb://localhost:12345")
        val database = mongo.getDatabase("test")
        return database.getCollection("test")
    }
}

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
                val doc = createPollDocument(options = asList("basketball"))

                collection.insertOne(doc)
                        .result { assertNotNull(doc["_id"]) }
        })
    }

    @Test
    fun `after replacing a document there is one updated document`() {
        ExecHarness.executeSingle(Operation.of {
                val collection = createConnectionAndGetCollection()
                val doc = createPollDocument(options = asList("basketball"))
                val docUpdate = createPollDocument(options = asList("soccer"))

                collection.insertOne(doc)
                        .map { doc.getObjectId("_id").toHexString() }
                        .flatMap { id -> collection.replaceOne(id, docUpdate) }
                        .then {
                            assertEquals(1, it.get().modifiedCount)
                        }

        })
    }

    private fun createPollDocument(options: MutableList<String>): Document {
        val poll = Poll(
                "Sport to play on Friday", options
        )
        val pollJson = objectMapper.writeValueAsString(poll)
        val doc = Document.parse(pollJson)

        assertEquals("Sport to play on Friday", doc.getString("topic"))
        assertEquals(options, doc["options"])

        return doc
    }

    private fun createConnectionAndGetCollection(): MongoCollection<Document> {
        val mongo = MongoClients.create("mongodb://localhost:12345")
        val database = mongo.getDatabase("test")
        return database.getCollection("test")
    }

    data class Poll(
            val topic: String?,
            val options: List<String>
    ) {
        @Suppress("unused")
        private constructor() : this(null, kotlin.collections.emptyList())
    }
}

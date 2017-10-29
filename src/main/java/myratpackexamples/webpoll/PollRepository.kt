package myratpackexamples.webpoll

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.Inject
import com.mongodb.async.client.MongoClients
import com.mongodb.async.client.MongoCollection
import io.vavr.control.Validation
import myratpackexamples.webpoll.RatpackMongoClient.FindOneError
import myratpackexamples.webpoll.RatpackMongoClient.InsertOneError
import myratpackexamples.webpoll.RatpackMongoClient.InsertOneJsonProcessingError
import org.bson.Document
import ratpack.exec.Promise

import io.vavr.control.Validation.invalid
import ratpack.exec.Promise.value

class PollRepository @Inject constructor(val objectMapper: ObjectMapper) {

    private val pollsCollection: MongoCollection<Document>
        get() {
            val mongo = MongoClients.create("mongodb://localhost:12345")
            val database = mongo.getDatabase("webpoll")
            return database.getCollection("polls")
        }

    fun storePoll(pollRequest: PollRequest): Promise<Validation<InsertOneError, Poll>> {
        try {
            val pollJson = objectMapper.writeValueAsString(pollRequest)
            val pollBsonDocument = Document.parse(pollJson)

            return RatpackMongoClient.insertOne(pollsCollection, pollBsonDocument)
                    .map { it.map { _ -> pollBsonDocument }.map(this::mapBsonDocumentToDomainObject) }

        } catch (e: JsonProcessingException) {
            return value(invalid<InsertOneError, Poll>(InsertOneJsonProcessingError(e)))
        }

    }

    fun retrievePoll(pollId: String?): Promise<Validation<FindOneError, Poll>> {
        return RatpackMongoClient.findOneById(pollsCollection, pollId)
                .map { it.map(this::mapBsonDocumentToDomainObject) }
    }

    private fun mapBsonDocumentToDomainObject(document: Document): Poll {
        @Suppress("UNCHECKED_CAST")
        return Poll(
                document.getObjectId("_id").toHexString(),
                document.getString("topic"),
                document["options"] as MutableList<String>
        )
    }
}

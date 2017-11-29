package myratpackexamples.webpoll

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.Inject
import com.mongodb.async.client.MongoClients
import com.mongodb.async.client.MongoCollection
import io.vavr.control.Validation
import io.vavr.control.Validation.invalid
import myratpackexamples.webpoll.InsertOneError.InsertOneJsonProcessingError
import org.bson.Document
import ratpack.exec.Promise
import ratpack.exec.Promise.value

class PollRepository @Inject constructor(val objectMapper: ObjectMapper) {

    private val pollsCollection: MongoCollection<Document>
        get() {
            val mongo = MongoClients.create("mongodb://localhost:12345")
            val database = mongo.getDatabase("webpoll")
            return database.getCollection("polls")
        }

    fun storePoll(poll: PollEntity.Poll): Promise<Validation<InsertOneError, PollResponse.Poll>> {
        try {
            val pollJson = objectMapper.writeValueAsString(poll)
            val pollBsonDocument = Document.parse(pollJson)

            return pollsCollection.insertOne(pollBsonDocument)
                    .map { it.map { _ -> pollBsonDocument }.map({ document -> mapBsonDocumentToDomainObject(document, null) }) }

        } catch (e: JsonProcessingException) {
            return value(invalid<InsertOneError, PollResponse.Poll>(InsertOneJsonProcessingError(e)))
        }

    }

    fun replacePoll(pollId: String?, poll: PollEntity.Poll): Promise<Validation<ReplaceOneError, PollResponse.Poll>> {
        try {
            val pollJson = objectMapper.writeValueAsString(poll)
            val pollBsonDocument = Document.parse(pollJson)

            return pollsCollection.replaceOne(pollId, pollBsonDocument)
                    .map { it.map { _ -> pollBsonDocument }.map { document -> mapBsonDocumentToDomainObject(document, pollId) } }

        } catch (e: JsonProcessingException) {
            return value(invalid<ReplaceOneError, PollResponse.Poll>(ReplaceOneError.ReplaceOneJsonProcessingError(e)))
        }

    }

    fun retrievePoll(pollId: String?): Promise<Validation<FindOneError, PollResponse.Poll>> {
        return pollsCollection.findOneById(pollId)
                .map { it.map { document -> mapBsonDocumentToDomainObject(document, null) } }
    }

    private fun mapBsonDocumentToDomainObject(document: Document, pollId: String?): PollResponse.Poll {
        @Suppress("UNCHECKED_CAST")
        return PollResponse.Poll(
                id = pollId ?: document.getObjectId("_id").toHexString(),
                topic = document.getString("topic"),
                options = document["options"] as MutableList<String>,
                votes = mapVotes(document)
        )
    }

    private fun mapVotes(document: Document): List<PollResponse.Vote> {
        val votes = document["votes"]
        if (votes == null) return emptyList()

        @Suppress("UNCHECKED_CAST")
        return (votes as MutableList<Document>).map {
            PollResponse.Vote(
                    voter = it.getString("voter"),
                    selections = mapSelections(it)
            )
        }
    }

    private fun mapSelections(document: Document): List<PollResponse.Selection> {
        val selections = document["selections"]
        if (selections == null) return emptyList()

        @Suppress("UNCHECKED_CAST")
        return (selections as MutableList<Document>).map {
            PollResponse.Selection(
                    option = it.getString("option"),
                    selected = PollResponse.SelectedResponse.valueOf(it.getString("selected"))
            )
        }
    }
}

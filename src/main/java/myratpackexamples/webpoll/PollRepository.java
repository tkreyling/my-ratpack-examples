package myratpackexamples.webpoll;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.async.client.MongoClient;
import com.mongodb.async.client.MongoClients;
import com.mongodb.async.client.MongoCollection;
import com.mongodb.async.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import io.vavr.control.Validation;
import myratpackexamples.promises.ValidationUtil;
import myratpackexamples.webpoll.RatpackMongoClient.FindOneError;
import myratpackexamples.webpoll.RatpackMongoClient.InvalidIdString;
import org.bson.Document;
import org.bson.types.ObjectId;
import ratpack.exec.Promise;

import java.util.List;

import static io.vavr.control.Validation.invalid;
import static io.vavr.control.Validation.valid;
import static ratpack.exec.Promise.error;

public class PollRepository {
    private ObjectMapper objectMapper = new ObjectMapper();

    public Promise<Poll> storePoll(PollRequest pollRequest) {
        try {
            MongoCollection<Document> collection = getPollsCollection();

            String pollJson = objectMapper.writeValueAsString(pollRequest);
            Document pollBsonDocument = Document.parse(pollJson);

            return RatpackMongoClient.insertOne(collection, pollBsonDocument)
                    .map((ignored) -> pollBsonDocument)
                    .map(this::mapBsonDocumentToDomainObject);

        } catch (JsonProcessingException e) {
            return error(e);
        }
    }

    public Promise<Validation<FindOneError, Poll>> retrievePoll(String pollId) {
        MongoCollection<Document> collection = getPollsCollection();

        Validation<FindOneError, ObjectId> mongoObjectId = createMongoObjectId(pollId);

        return ValidationUtil.flatMapPromise(mongoObjectId, objectId ->
                RatpackMongoClient.findOne(collection, Filters.eq("_id", objectId)))
                .map(validation -> validation.map(this::mapBsonDocumentToDomainObject));
    }

    private Validation<FindOneError, ObjectId> createMongoObjectId(String hexIdString) {
        try {
            return valid(new ObjectId(hexIdString));

        } catch (IllegalArgumentException e) {
            return invalid(new InvalidIdString(hexIdString));
        }
    }

    private Poll mapBsonDocumentToDomainObject(Document document) {
        //noinspection unchecked
        return new Poll(
                document.getObjectId("_id").toHexString(),
                document.getString("topic"),
                (List) document.get("options")
        );
    }

    private MongoCollection<Document> getPollsCollection() {
        MongoClient mongo = MongoClients.create("mongodb://localhost:12345");
        MongoDatabase database = mongo.getDatabase("webpoll");
        return database.getCollection("polls");
    }
}

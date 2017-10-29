package myratpackexamples.webpoll

import com.mongodb.async.client.MongoCollection
import com.mongodb.client.model.Filters
import io.vavr.control.Validation
import io.vavr.control.Validation.invalid
import io.vavr.control.Validation.valid
import myratpackexamples.promises.flatMapPromise
import myratpackexamples.webpoll.FindOneError.*
import myratpackexamples.webpoll.InsertOneError.InsertOneMongoError
import org.bson.Document
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import ratpack.exec.Promise
import ratpack.exec.Promise.async
import java.util.*

sealed class InsertOneError {
    data class InsertOneMongoError(val throwable: Throwable?) : InsertOneError()
    data class InsertOneJsonProcessingError(val throwable: Throwable?) : InsertOneError()
}

fun MongoCollection<Document>.insertOne(document: Document): Promise<Validation<InsertOneError, Void>> {
    return async { downstream ->
        insertOne(document) { result, throwable ->
            if (throwable != null) {
                downstream.success(invalid(InsertOneMongoError(throwable)))
            } else {
                downstream.success(valid(result))
            }
        }
    }
}

sealed class FindOneError {
    object ExactlyOneElementExpected : FindOneError()
    data class InvalidIdString(val idString: String?) : FindOneError()
    data class FindOneMongoError(val throwable: Throwable?) : FindOneError()
}

fun MongoCollection<Document>.findOneById(hexIdString: String?): Promise<Validation<FindOneError, Document>> {
    val mongoObjectId = createMongoObjectId(hexIdString)

    return flatMapPromise(mongoObjectId) { findOne(Filters.eq("_id", it)) }
}

private fun createMongoObjectId(hexIdString: String?): Validation<FindOneError, ObjectId> {
    return try {
        valid(ObjectId(hexIdString))
    } catch (e: IllegalArgumentException) {
        invalid(InvalidIdString(hexIdString))
    }
}

private fun MongoCollection<Document>.findOne(filter: Bson): Promise<Validation<FindOneError, Document>> {
    return async { downstream ->
        find(filter).into(ArrayList()) { result, throwable ->
            if (throwable != null) {
                downstream.success(invalid(FindOneMongoError(throwable)))
            } else {
                if (result.size != 1) {
                    downstream.success(invalid(ExactlyOneElementExpected))
                } else {
                    downstream.success(valid(result[0]))
                }
            }
        }
    }
}

package myratpackexamples.webpoll

import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext

class InMemoryMongoDbJunitExtension : BeforeAllCallback, AfterAllCallback {

    private var inMemoryMongoDb: InMemoryMongoDb? = null

    override fun beforeAll(context: ExtensionContext?) {
        inMemoryMongoDb = InMemoryMongoDb()
    }

    override fun afterAll(context: ExtensionContext?) {
        inMemoryMongoDb?.close()
    }
}
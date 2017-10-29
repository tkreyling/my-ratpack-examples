package myratpackexamples.webpoll

import com.fasterxml.jackson.databind.ObjectMapper
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpResponseStatus
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import ratpack.http.client.ReceivedResponse
import ratpack.test.embed.EmbeddedApp
import ratpack.test.http.TestHttpClient

import java.util.Arrays.asList
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue

internal class PollHandlerTest {
    private val objectMapper = ObjectMapper()

    @Test
    fun `System accepts a valid poll`() = EmbeddedApp
            .of { setupServer(it) }
            .test { httpClient ->
                val pollJson = "{\"topic\":\"Sport to play on Friday\",\"options\":[\"basketball\"]}"

                val response = httpClient.post("poll", pollJson)

                assertEquals(HttpResponseStatus.CREATED.code(), response.statusCode)
                assertTrue(response.headers.contains(HttpHeaderNames.LOCATION))
            }

    @Test
    fun `System rejects an poll with no options`() = EmbeddedApp
            .of { setupServer(it) }
            .test { httpClient ->
                val pollJson = "{\"topic\":\"\"}"

                val response = httpClient.post("poll", pollJson)

                assertEquals(HttpResponseStatus.BAD_REQUEST.code(), response.statusCode)
            }

    @Test
    fun `System retains a valid poll`() = EmbeddedApp
            .of { setupServer(it) }
            .test { httpClient ->
                val pollJson = "{\"topic\":\"Sport to play on Friday\",\"options\":[\"basketball\"]}"

                val createResponse = httpClient.post("poll", pollJson)

                val pollUri = createResponse.headers.get(HttpHeaderNames.LOCATION)

                val poll = httpClient.get(pollUri, Poll::class.java)

                assertEquals("Sport to play on Friday", poll.topic)
                assertEquals(asList("basketball"), poll.options)
            }

    @Test
    fun `System responds with bad request for an invalid id`() = EmbeddedApp
            .of { setupServer(it) }
            .test { httpClient ->
                val response = httpClient.get("poll/999999999")

                assertEquals(HttpResponseStatus.BAD_REQUEST.code(), response.statusCode)
            }

    @Test
    fun `System responds with not found for an not existent poll`() = EmbeddedApp
            .of { setupServer(it) }
            .test { httpClient ->
                val response = httpClient.get("poll/59ecf1ec9bdc9640f8b4adca")

                assertEquals(HttpResponseStatus.NOT_FOUND.code(), response.statusCode)
            }

    private operator fun <T> TestHttpClient.get(uri: String, type: Class<T>): T =
            objectMapper.readValue(get(uri).body.text, type)

    private fun TestHttpClient.post(uri: String, pollJson: String): ReceivedResponse {
        return requestSpec {
            it.body {
                it.type("application/json").text(pollJson)
            }
        }.post(uri)
    }

    companion object {
        @JvmStatic
        private var inMemoryMongoDb: InMemoryMongoDb? = null

        @JvmStatic
        @BeforeAll
        fun setUp() {
            inMemoryMongoDb = InMemoryMongoDb()
        }

        @JvmStatic
        @AfterAll
        fun tearDown() {
            inMemoryMongoDb!!.close()
        }
    }
}
package myratpackexamples.webpoll

import com.fasterxml.jackson.databind.ObjectMapper
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpResponseStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import ratpack.test.embed.EmbeddedApp
import java.util.Arrays.asList

@ExtendWith(InMemoryMongoDbJunitExtension::class)
internal class PollOwnerTest : TestHttpClientMixin {
    override val objectMapper = ObjectMapper()

    @Test
    fun `System accepts a valid poll`() {
        EmbeddedApp.of { setupServer(it) }.test { httpClient ->
            val pollJson = "{\"topic\":\"Sport to play on Friday\",\"options\":[\"basketball\"]}"

            val response = httpClient.post("poll", pollJson)

            assertEquals(HttpResponseStatus.CREATED.code(), response.statusCode)
            assertTrue(response.headers.contains(HttpHeaderNames.LOCATION))
        }
    }

    @Test
    fun `System rejects an poll with no options`() {
        EmbeddedApp.of { setupServer(it) }.test { httpClient ->
            val pollJson = "{\"topic\":\"\"}"

            val response = httpClient.post("poll", pollJson)

            assertEquals(HttpResponseStatus.BAD_REQUEST.code(), response.statusCode)
        }
    }

    @Test
    fun `System retains a valid poll`() {
        EmbeddedApp.of { setupServer(it) }.test { httpClient ->
            val pollJson = "{\"topic\":\"Sport to play on Friday\",\"options\":[\"basketball\"]}"

            val createResponse = httpClient.post("poll", pollJson)

            val pollUri = createResponse.headers.get(HttpHeaderNames.LOCATION)

            val poll = httpClient.get(pollUri, Poll::class.java)

            assertEquals("Sport to play on Friday", poll.topic)
            assertEquals(asList("basketball"), poll.options)
        }
    }

    @Test
    fun `System responds with bad request for an invalid id`() {
        EmbeddedApp.of { setupServer(it) }.test { httpClient ->
            val response = httpClient.get("poll/999999999")

            assertEquals(HttpResponseStatus.BAD_REQUEST.code(), response.statusCode)
        }
    }

    @Test
    fun `System responds with not found for an not existent poll`() {
        EmbeddedApp.of { setupServer(it) }.test { httpClient ->
            val response = httpClient.get("poll/59ecf1ec9bdc9640f8b4adca")

            assertEquals(HttpResponseStatus.NOT_FOUND.code(), response.statusCode)
        }
    }
}
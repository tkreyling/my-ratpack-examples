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
    fun validPostCreatesPoll() {
        val pollJson = "{\"topic\":\"Sport to play on Friday\",\"options\":[\"basketball\"]}"

        EmbeddedApp
                .of { setupServer(it) }
                .test { httpClient ->
                    val response = post(httpClient, "poll", pollJson)

                    assertEquals(HttpResponseStatus.CREATED.code(), response.statusCode)
                    assertTrue(response.headers.contains(HttpHeaderNames.LOCATION))
                }
    }

    @Test
    fun postWithEmptyTopicIsRejected() {
        val pollJson = "{\"topic\":\"\"}"

        EmbeddedApp
                .of { setupServer(it) }
                .test { httpClient ->
                    val response = post(httpClient, "poll", pollJson)
                    assertEquals(HttpResponseStatus.BAD_REQUEST.code(), response.statusCode)
                }
    }

    @Test
    fun systemRetainsValidNewPoll() {
        val pollJson = "{\"topic\":\"Sport to play on Friday\",\"options\":[\"basketball\"]}"

        EmbeddedApp
                .of { setupServer(it) }
                .test { httpClient ->
                    val createResponse = post(httpClient, "poll", pollJson)

                    val pollUri = createResponse.headers.get(HttpHeaderNames.LOCATION)

                    val poll = get(httpClient, pollUri, Poll::class.java)

                    assertEquals("Sport to play on Friday", poll.topic)
                    assertEquals(asList("basketball"), poll.options)
                }
    }

    @Test
    fun invalidPollId() {
        EmbeddedApp
                .of { setupServer(it) }
                .test { httpClient ->
                    val response = httpClient.get("poll/999999999")

                    assertEquals(HttpResponseStatus.BAD_REQUEST.code(), response.statusCode)
                }
    }

    @Test
    fun unknownPollId() {
        EmbeddedApp
                .of { setupServer(it) }
                .test { httpClient ->
                    val response = httpClient.get("poll/59ecf1ec9bdc9640f8b4adca")

                    assertEquals(HttpResponseStatus.NOT_FOUND.code(), response.statusCode)
                }
    }

    private operator fun <T> get(httpClient: TestHttpClient, uri: String, type: Class<T>): T {
        val response = httpClient.get(uri)
        val bodyText = response.body.text
        return objectMapper.readValue(bodyText, type)
    }

    private fun post(httpClient: TestHttpClient, uri: String, pollJson: String): ReceivedResponse {
        return httpClient
                .requestSpec { request -> request.body { body -> body.type("application/json").text(pollJson) } }
                .post(uri)
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
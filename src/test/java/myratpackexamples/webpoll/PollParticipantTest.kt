package myratpackexamples.webpoll

import com.fasterxml.jackson.databind.ObjectMapper
import io.netty.handler.codec.http.HttpHeaderNames.LOCATION
import io.netty.handler.codec.http.HttpResponseStatus
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import ratpack.test.embed.EmbeddedApp

@ExtendWith(InMemoryMongoDbJunitExtension::class)
internal class PollParticipantTest : TestHttpClientMixin {
    override val objectMapper = ObjectMapper()

    @Test
    fun `System rejects an empty vote`() {
        EmbeddedApp.of { setupServer(it) }.test { httpClient ->
            // Given
            val pollJson = """
                {
                    "topic": "Sport to play on Friday",
                    "options": ["basketball", "soccer"]
                }
            """
            val createPollResponse = httpClient.post("poll", pollJson)
            val pollUri = createPollResponse.headers[LOCATION]

            // When
            val voteJson = """
                {
                }
            """
            val response = httpClient.post(pollUri + "/vote", voteJson)

            // Then
            Assertions.assertEquals(HttpResponseStatus.BAD_REQUEST.code(), response.statusCode)
        }
    }

    @Test
    fun `System rejects an vote without voter`() {
        EmbeddedApp.of { setupServer(it) }.test { httpClient ->
            // Given
            val pollJson = """
                {
                    "topic": "Sport to play on Friday",
                    "options": ["basketball"]
                }
            """
            val createPollResponse = httpClient.post("poll", pollJson)
            val pollUri = createPollResponse.headers[LOCATION]

            // When
            val voteJson = """
                {
                    "selections": [
                        {
                            "option": "basketball",
                            "selected": "yes"
                        }
                    ]
                }
            """
            val response = httpClient.post(pollUri + "/vote", voteJson)

            // Then
            Assertions.assertEquals(HttpResponseStatus.BAD_REQUEST.code(), response.statusCode)
        }
    }

    @Test
    fun `System accepts an vote with voter and matching options`() {
        EmbeddedApp.of { setupServer(it) }.test { httpClient ->
            // Given
            val pollJson = """
                {
                    "topic": "Sport to play on Friday",
                    "options": ["basketball", "soccer"]
                }
            """
            val createPollResponse = httpClient.post("poll", pollJson)
            val pollUri = createPollResponse.headers[LOCATION]

            // When
            val voteJson = """
                {
                    "voter": "Markus Mustermann",
                    "selections": [
                        {
                            "option": "basketball",
                            "selected": "yes"
                        },
                        {
                            "option": "soccer",
                            "selected": "no"
                        }
                    ]
                }
            """
            val response = httpClient.post(pollUri + "/vote", voteJson)

            // Then
            Assertions.assertEquals(HttpResponseStatus.CREATED.code(), response.statusCode)
        }
    }

}
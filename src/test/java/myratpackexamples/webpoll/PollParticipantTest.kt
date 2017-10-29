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

}
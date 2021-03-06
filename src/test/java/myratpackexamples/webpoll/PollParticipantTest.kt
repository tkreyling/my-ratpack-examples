package myratpackexamples.webpoll

import com.fasterxml.jackson.databind.ObjectMapper
import io.netty.handler.codec.http.HttpHeaderNames.LOCATION
import io.netty.handler.codec.http.HttpResponseStatus.*
import myratpackexamples.webpoll.PollResponse.*
import myratpackexamples.webpoll.createpoll.PollRequest
import myratpackexamples.webpoll.createvote.VoteRequest.Selection
import myratpackexamples.webpoll.createvote.VoteRequest.Vote
import org.junit.jupiter.api.Assertions.assertEquals
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
            val pollJson = somePoll()
            val createPollResponse = httpClient.post("poll", pollJson)
            val pollUri = createPollResponse.headers[LOCATION]

            // When
            val voteJson = vote(null, null)
            val response = httpClient.post(pollUri + "/vote", voteJson)

            // Then
            assertEquals(BAD_REQUEST.code(), response.statusCode)
        }
    }

    @Test
    fun `System rejects a vote on an invalid poll id`() {
        EmbeddedApp.of { setupServer(it) }.test { httpClient ->
            // Given
            val voteJson = someValidVote()

            // When
            val response = httpClient.post("poll/9999999/vote", voteJson)

            // Then
            assertEquals(BAD_REQUEST.code(), response.statusCode)
        }
    }

    @Test
    fun `System rejects a vote on a not existing poll`() {
        EmbeddedApp.of { setupServer(it) }.test { httpClient ->
            // Given
            val voteJson = someValidVote()

            // When
            val response = httpClient.post("poll/59ecf1ec9bdc9640f8b4adca/vote", voteJson)

            // Then
            assertEquals(NOT_FOUND.code(), response.statusCode)
        }
    }

    @Test
    fun `System rejects a vote without voter`() {
        EmbeddedApp.of { setupServer(it) }.test { httpClient ->
            // Given
            val pollJson = somePoll()
            val createPollResponse = httpClient.post("poll", pollJson)
            val pollUri = createPollResponse.headers[LOCATION]

            // When
            val voteJson = vote(
                    voter = null,
                    selections = someValidSelections()
            )
            val response = httpClient.post(pollUri + "/vote", voteJson)

            // Then
            assertEquals(BAD_REQUEST.code(), response.statusCode)
        }
    }

    @Test
    fun `System rejects a vote with invalid selection`() {
        EmbeddedApp.of { setupServer(it) }.test { httpClient ->
            // Given
            val pollJson = somePoll()
            val createPollResponse = httpClient.post("poll", pollJson)
            val pollUri = createPollResponse.headers[LOCATION]

            // When
            val voteJson = vote(
                    voter = someValidVoter(),
                    selections = listOf(
                            Selection("basketball", "some invalid value")
                    )
            )
            val response = httpClient.post(pollUri + "/vote", voteJson)

            // Then
            assertEquals(BAD_REQUEST.code(), response.statusCode)
        }
    }

    @Test
    fun `System accepts an vote with voter and matching options`() {
        EmbeddedApp.of { setupServer(it) }.test { httpClient ->
            // Given
            val pollJson = somePoll()
            val createPollResponse = httpClient.post("poll", pollJson)
            val pollUri = createPollResponse.headers[LOCATION]

            // When
            val voteJson = someValidVote()
            val response = httpClient.post(pollUri + "/vote", voteJson)

            // Then
            assertEquals(CREATED.code(), response.statusCode)
        }
    }

    @Test
    fun `System rejects an vote with voter and unknown options`() {
        EmbeddedApp.of { setupServer(it) }.test { httpClient ->
            // Given
            val pollJson = somePoll()
            val createPollResponse = httpClient.post("poll", pollJson)
            val pollUri = createPollResponse.headers[LOCATION]

            // When
            val voteJson = vote(
                    voter = someValidVoter(),
                    selections = listOf(
                            Selection("handball", "yes")
                    )
            )
            val response = httpClient.post(pollUri + "/vote", voteJson)

            // Then
            assertEquals(BAD_REQUEST.code(), response.statusCode)
        }
    }

    @Test
    fun `System retains a valid vote`() {
        EmbeddedApp.of { setupServer(it) }.test { httpClient ->
            // Given
            val pollJson = somePoll()
            val createPollResponse = httpClient.post("poll", pollJson)
            val pollUri = createPollResponse.headers[LOCATION]

            // When
            val voteJson = someValidVote()
            httpClient.post(pollUri + "/vote", voteJson)

            // Then
            val poll = httpClient.get(pollUri, Poll::class)

            assertEquals(1, poll.votes.size)
            assertEquals("Max Mustermann", poll.votes[0].voter)
            assertEquals(2, poll.votes[0].selections.size)
            assertEquals("basketball", poll.votes[0].selections[0].option)
            assertEquals("YES", poll.votes[0].selections[0].selected.toString())
        }
    }

    private fun somePoll() = poll(
            topic = "Sport to play on Friday",
            options = listOf("basketball", "soccer")
    )

    private fun poll(
            topic: String,
            options: List<String>
    ) = objectMapper.writeValueAsString(PollRequest(topic, options))

    private fun someValidVote() = vote(
            voter = someValidVoter(),
            selections = someValidSelections()
    )

    private fun someValidVoter() = "Max Mustermann"

    private fun someValidSelections(): List<Selection> {
        return listOf(
                Selection("basketball", "yes"),
                Selection("soccer", "no")
        )
    }

    private fun vote(
            voter: String?,
            selections: List<Selection>?
    ) = objectMapper.writeValueAsString(Vote(voter, selections))

}
package myratpackexamples.webpoll;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ratpack.http.client.ReceivedResponse;
import ratpack.test.embed.EmbeddedApp;
import ratpack.test.http.TestHttpClient;

import java.io.IOException;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PollHandlerTest {
    private ObjectMapper objectMapper = new ObjectMapper();

    private static InMemoryMongoDb inMemoryMongoDb;

    @BeforeAll
    static void setUp() throws Exception {
        inMemoryMongoDb = new InMemoryMongoDb();
    }

    @AfterAll
    static void tearDown() throws Exception {
        inMemoryMongoDb.close();
    }

    @Test
    void validPostCreatesPoll() throws Exception {
        String pollJson = "{\"topic\":\"Sport to play on Friday\",\"options\":[\"basketball\"]}";

        EmbeddedApp
                .of(PollApplication::setupServer)
                .test(httpClient -> {
                    ReceivedResponse response = post(httpClient, "poll", pollJson);

                    assertEquals(HttpResponseStatus.CREATED.code(), response.getStatusCode());
                    assertTrue(response.getHeaders().contains(HttpHeaderNames.LOCATION));
                });
    }

    @Test
    void postWithEmptyTopicIsRejected() throws Exception {
        String pollJson = "{\"topic\":\"\"}";

        EmbeddedApp
                .of(PollApplication::setupServer)
                .test(httpClient -> {
                    ReceivedResponse response = post(httpClient, "poll", pollJson);
                    assertEquals(HttpResponseStatus.BAD_REQUEST.code(), response.getStatusCode());
                });
    }

    @Test
    void systemRetainsValidNewPoll() throws Exception {
        String pollJson = "{\"topic\":\"Sport to play on Friday\",\"options\":[\"basketball\"]}";

        EmbeddedApp
                .of(PollApplication::setupServer)
                .test(httpClient -> {
                    ReceivedResponse createResponse = post(httpClient, "poll", pollJson);

                    String pollUri = createResponse.getHeaders().get(HttpHeaderNames.LOCATION);

                    Poll poll = get(httpClient, pollUri, Poll.class);

                    assertEquals("Sport to play on Friday", poll.getTopic());
                    assertEquals(asList("basketball"), poll.getOptions());
                });
    }

    @Test
    void invalidPollId() throws Exception {
        EmbeddedApp
                .of(PollApplication::setupServer)
                .test(httpClient -> {
                    ReceivedResponse response = httpClient.get("poll/999999999");

                    assertEquals(HttpResponseStatus.BAD_REQUEST.code(), response.getStatusCode());
                });
    }

    @Test
    void unknownPollId() throws Exception {
        EmbeddedApp
                .of(PollApplication::setupServer)
                .test(httpClient -> {
                    ReceivedResponse response = httpClient.get("poll/59ecf1ec9bdc9640f8b4adca");

                    assertEquals(HttpResponseStatus.NOT_FOUND.code(), response.getStatusCode());
                });
    }

    private <T> T get(TestHttpClient httpClient, String uri, Class<T> type) throws IOException {
        ReceivedResponse response = httpClient.get(uri);
        String bodyText = response.getBody().getText();
        return objectMapper.readValue(bodyText, type);
    }

    private ReceivedResponse post(TestHttpClient httpClient, String uri, String pollJson) {
        return httpClient
                .requestSpec(request -> request.body(body -> body.type("application/json").text(pollJson)))
                .post(uri);
    }
}
package myratpackexamples.webpoll;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.jupiter.api.Test;
import ratpack.http.client.ReceivedResponse;
import ratpack.test.embed.EmbeddedApp;
import ratpack.test.http.TestHttpClient;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class PollHandlerTest {
    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void validPostCreatesPoll() throws Exception {
        String pollJson = "{\"topic\":\"Sport to play on Friday\",\"options\":[\"basketball\"]}";

        EmbeddedApp
                .fromHandler(PollHandler::createPoll)
                .test(httpClient -> {
                    ReceivedResponse response = post(httpClient, pollJson);

                    assertEquals(HttpResponseStatus.CREATED.code(), response.getStatusCode());
                    assertTrue(response.getHeaders().contains(HttpHeaderNames.LOCATION));
                });
    }

    @Test
    void postWithEmptyTopicIsRejected() throws Exception {
        String pollJson = "{\"topic\":\"\"}";

        EmbeddedApp
                .fromHandler(PollHandler::createPoll)
                .test(httpClient -> {
                    ReceivedResponse response = post(httpClient, pollJson);
                    assertEquals(HttpResponseStatus.BAD_REQUEST.code(), response.getStatusCode());
                });
    }

    @Test
    void systemRetainsValidNewPoll() throws Exception {
        String pollJson = "{\"topic\":\"Sport to play on Friday\",\"options\":[\"basketball\"]}";

        EmbeddedApp.of(server -> server.handlers(PollHandler::pollHandlerChain))
                .test(httpClient -> {
                    ReceivedResponse createResponse = post(httpClient, pollJson);

                    String pollUri = createResponse.getHeaders().get(HttpHeaderNames.LOCATION);

                    Poll poll = get(httpClient, pollUri, Poll.class);

                    assertEquals("Sport to play on Friday", poll.getTopic());
                });
    }

    private <T> T get(TestHttpClient httpClient, String uri, Class<T> type) throws IOException {
        ReceivedResponse response = httpClient.get(uri);
        String bodyText = response.getBody().getText();
        return objectMapper.readValue(bodyText, type);
    }

    private ReceivedResponse post(TestHttpClient httpClient, String pollJson) {
        return httpClient
                .requestSpec(request -> request.body(body -> body.type("application/json").text(pollJson)))
                .post();
    }
}
package myratpackexamples.webpoll;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.jupiter.api.Test;
import ratpack.http.client.ReceivedResponse;
import ratpack.test.embed.EmbeddedApp;
import ratpack.test.http.TestHttpClient;

import static org.junit.jupiter.api.Assertions.*;

class PollHandlerTest {
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

    private ReceivedResponse post(TestHttpClient httpClient, String pollJson) {
        return httpClient
                .requestSpec(request -> request.body(body -> body.type("application/json").text(pollJson)))
                .post();
    }
}
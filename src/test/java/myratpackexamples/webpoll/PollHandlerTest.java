package myratpackexamples.webpoll;

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
                    assertEquals(201, response.getStatusCode());
                });
    }

    @Test
    void postWithEmptyTopicIsRejected() throws Exception {
        String pollJson = "{\"topic\":\"\"}";

        EmbeddedApp
                .fromHandler(PollHandler::createPoll)
                .test(httpClient -> {
                    ReceivedResponse response = post(httpClient, pollJson);
                    assertEquals(400, response.getStatusCode());
                });
    }

    private ReceivedResponse post(TestHttpClient httpClient, String pollJson) {
        return httpClient
                .requestSpec(request -> request.body(body -> body.type("application/json").text(pollJson)))
                .post();
    }
}
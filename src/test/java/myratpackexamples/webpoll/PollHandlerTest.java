package myratpackexamples.webpoll;

import org.junit.jupiter.api.Test;
import ratpack.http.client.ReceivedResponse;
import ratpack.test.embed.EmbeddedApp;

import static org.junit.jupiter.api.Assertions.*;

class PollHandlerTest {
    @Test
    void validPostCreatesPoll() throws Exception {
        EmbeddedApp
                .fromHandler(PollHandler::createPoll)
                .test(httpClient -> {
                    ReceivedResponse response = httpClient.requestSpec(request ->
                            request.body(body -> body
                                    .type("application/json")
                                    .text("{\"topic\":\"Sport to play on Friday\",\"options\":[\"basketball\"]}"))
                    ).post();
                    assertEquals(201, response.getStatusCode());
                });
    }

    @Test
    void postWithEmptyTopicIsRejected() throws Exception {
        EmbeddedApp
                .fromHandler(PollHandler::createPoll)
                .test(httpClient -> {
                    ReceivedResponse response = httpClient.requestSpec(request ->
                            request.body(body -> body
                                    .type("application/json")
                                    .text("{\"topic\":\"\"}"))
                    ).post();
                    assertEquals(400, response.getStatusCode());
                });
    }
}
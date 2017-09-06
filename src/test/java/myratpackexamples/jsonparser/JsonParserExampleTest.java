package myratpackexamples.jsonparser;

import org.junit.jupiter.api.Test;
import ratpack.http.client.ReceivedResponse;
import ratpack.test.embed.EmbeddedApp;

import static org.junit.jupiter.api.Assertions.*;

class JsonParserExampleTest {
    @Test
    void prependHello() throws Exception {
        EmbeddedApp
                .fromHandler(JsonParserExample::extractName)
                .test(httpClient -> {
                    ReceivedResponse response = httpClient.requestSpec(request ->
                            request.body(body -> body.type("application/json").text("{\"name\":\"John\"}"))
                    ).post();
                    assertEquals("John", response.getBody().getText());
                });
    }

}
package myratpackexamples.simplepost;

import org.junit.jupiter.api.Test;
import ratpack.http.client.ReceivedResponse;
import ratpack.test.embed.EmbeddedApp;

import static org.junit.jupiter.api.Assertions.*;

class SimplePostTest {
    @Test
    void prependHello() throws Exception {
        EmbeddedApp
                .fromHandler(SimplePost::prependHello)
                .test(httpClient -> {
                    ReceivedResponse response = httpClient.request(req -> {
                        req.method("POST");
                        req.getBody().text("world");
                    });
                    assertEquals("hello: world", response.getBody().getText());
                });
    }
}
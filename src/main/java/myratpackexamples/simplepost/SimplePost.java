package myratpackexamples.simplepost;

import ratpack.handling.Context;

public class SimplePost {
    public static void prependHello(Context ctx) {
        ctx.getRequest().getBody().then(data -> ctx.render("hello: " + data.getText()));
    }
}

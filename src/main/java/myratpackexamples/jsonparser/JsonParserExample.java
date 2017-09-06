package myratpackexamples.jsonparser;

import ratpack.handling.Context;

import static ratpack.jackson.Jackson.fromJson;

public class JsonParserExample {
    public static void extractName(Context context) {
        context.render(context.parse(fromJson(Person.class)).map(Person::getFirstname));
    }
}

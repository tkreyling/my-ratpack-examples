package myratpackexamples.jsonparser;

import com.fasterxml.jackson.annotation.JsonProperty;
import ratpack.handling.Context;

import static ratpack.jackson.Jackson.fromJson;

public class JsonParserExample {
    public static class Person {
        private final String name;

        public Person(@JsonProperty("name") String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public static void extractName(Context context) {
        context.render(context.parse(fromJson(Person.class)).map(Person::getName));
    }
}

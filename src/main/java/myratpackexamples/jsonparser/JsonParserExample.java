package myratpackexamples.jsonparser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import ratpack.handling.Context;

import static ratpack.jackson.Jackson.fromJson;

public class JsonParserExample {
    public static void extractName(Context context) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new ParameterNamesModule());

        context.render(context.parse(fromJson(Person.class, objectMapper)).map(Person::getFirstname));
    }
}

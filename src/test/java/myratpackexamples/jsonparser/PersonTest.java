package myratpackexamples.jsonparser;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PersonTest {
    @Test
    public void shouldBeAbleToDeserializePerson() throws IOException {
        // given
        ObjectMapper objectMapper = new ObjectMapper();

        // when
        Person actual = objectMapper.readValue(
                "{\"firstname\":\"joe\",\"lastname\":\"smith\"}",
                Person.class
        );

        // then
        assertEquals("joe", actual.getFirstname());
        assertEquals("smith", actual.getLastname());
    }

}
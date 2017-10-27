package myratpackexamples.validations;

import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.control.Validation;
import lombok.Value;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ValidationsTest {

    @Value
    public static class Person {
        String name;
        Integer age;
    }

    @Test
    void if_there_is_no_validation_failure_in_the_chain_there_is_no_error() {
        Validation<Seq<String>, Person> person = Validation.combine(
                Validation.<String, String>valid("Horst"), Validation.valid(30)
        ).ap(Person::new);

        //noinspection Convert2MethodRef
        Validation<Seq<String>, Person> secondStep = person.flatMap(value -> Validation.valid(value));

        assertThrows(NoSuchElementException.class, secondStep::getError);
        assertEquals(new Person("Horst", 30), secondStep.get());
    }

    @Test
    void if_there_are_validation_failures_in_the_first_stage_further_processing_is_ignored() {
        Validation<Seq<String>, Person> person = Validation.<String, String, Integer>combine(
                Validation.invalid("Name failed"), Validation.invalid("Age failed")
        ).ap(Person::new);

        Validation<Seq<String>, Person> secondStep = person.flatMap(
                ignored -> Validation.invalid(List.of("Validation in additional step failed!"))
        );

        assertEquals(List.of("Name failed", "Age failed"), secondStep.getError());
        assertThrows(NoSuchElementException.class, secondStep::get);
    }

    @Test
    void if_no_failure_in_first_stage_and_failure_in_second_stage_error_is_failure_from_second_stage() {
        Validation<Seq<String>, Person> person = Validation.combine(
                Validation.<String, String>valid("Horst"), Validation.valid(30)
        ).ap(Person::new);

        Validation<Seq<String>, Person> secondStep = person.flatMap(
                ignored -> Validation.invalid(List.of("Validation in additional step failed!"))
        );

        assertEquals(List.of("Validation in additional step failed!"), secondStep.getError());
        assertThrows(NoSuchElementException.class, secondStep::get);
    }
}

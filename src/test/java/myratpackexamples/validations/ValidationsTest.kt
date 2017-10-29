package myratpackexamples.validations

import io.vavr.collection.List
import io.vavr.collection.Seq
import io.vavr.control.Validation
import org.junit.jupiter.api.Test

import java.util.NoSuchElementException

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.function.Executable

import myratpackexamples.webpoll.ap;

class ValidationsTest {

    data class Person(val name: String?, val age: Int?)

    @Test
    fun if_there_is_no_validation_failure_in_the_chain_there_is_no_error() {
        val person = Validation.combine(
                Validation.valid("Horst"),
                Validation.valid<String, Int>(30)
        ).ap(::Person)

        val secondStep = person.flatMap { Validation.valid<Seq<String>, Person>(it) }

        assertThrows(NoSuchElementException::class.java, Executable { secondStep.error })
        assertEquals(Person("Horst", 30), secondStep.get())
    }

    @Test
    fun if_there_are_validation_failures_in_the_first_stage_further_processing_is_ignored() {
        val person = Validation.combine(
                Validation.invalid<String, String>("Name failed"),
                Validation.invalid<String, Int>("Age failed")
        ).ap(::Person)

        val secondStep = person.flatMap {
            Validation.invalid<Seq<String>, Person>(List.of("Validation in additional step failed!"))
        }

        assertEquals(List.of("Name failed", "Age failed"), secondStep.error)
        assertThrows(NoSuchElementException::class.java, Executable { secondStep.get() })
    }

    @Test
    fun if_no_failure_in_first_stage_and_failure_in_second_stage_error_ist() {
        val person = Validation.combine(
                Validation.valid("Horst"),
                Validation.valid<String, Int>(30)
        ).ap(::Person)

        val secondStep = person.flatMap {
            Validation.invalid<Seq<String>, Person>(List.of("Validation in additional step failed!"))
        }

        assertEquals(List.of("Validation in additional step failed!"), secondStep.error)
        assertThrows(NoSuchElementException::class.java, Executable { secondStep.get() })
    }
}

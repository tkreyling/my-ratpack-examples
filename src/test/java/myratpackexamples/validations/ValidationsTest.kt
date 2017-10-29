package myratpackexamples.validations

import io.vavr.collection.List
import io.vavr.collection.Seq
import io.vavr.control.Validation
import myratpackexamples.webpoll.ap
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.util.*

class ValidationsTest {

    data class Person(val name: String?, val age: Int?)

    @Test
    fun `if there is no validation failure in the chain there is no error`() {
        val person = Validation.combine(
                Validation.valid("Horst"),
                Validation.valid<String, Int>(30)
        ).ap(::Person)

        val secondStep = person.flatMap { Validation.valid<Seq<String>, Person>(it) }

        assertThrows(NoSuchElementException::class.java) { secondStep.error }
        assertEquals(Person("Horst", 30), secondStep.get())
    }

    @Test
    fun `if there are validation failures in the first stage further processing is ignored`() {
        val person = Validation.combine(
                Validation.invalid<String, String>("Name failed"),
                Validation.invalid<String, Int>("Age failed")
        ).ap(::Person)

        val secondStep = person.flatMap {
            Validation.invalid<Seq<String>, Person>(List.of("Validation in additional step failed!"))
        }

        assertEquals(List.of("Name failed", "Age failed"), secondStep.error)
        assertThrows(NoSuchElementException::class.java) { secondStep.get() }
    }

    @Test
    fun `if no failure in first stage and failure in second stage error is failure from second stage`() {
        val person = Validation.combine(
                Validation.valid("Horst"),
                Validation.valid<String, Int>(30)
        ).ap(::Person)

        val secondStep = person.flatMap {
            Validation.invalid<Seq<String>, Person>(List.of("Validation in additional step failed!"))
        }

        assertEquals(List.of("Validation in additional step failed!"), secondStep.error)
        assertThrows(NoSuchElementException::class.java) { secondStep.get() }
    }
}

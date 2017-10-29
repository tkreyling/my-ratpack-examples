package myratpackexamples.webpoll

import org.junit.jupiter.api.Test
import ratpack.test.exec.ExecHarness

import java.util.NoSuchElementException

import io.vavr.control.Validation.invalid
import io.vavr.control.Validation.valid
import myratpackexamples.webpoll.ValidationExtensionsTest.MyErrorCodes.PROMISE_FAILED
import myratpackexamples.webpoll.ValidationExtensionsTest.MyErrorCodes.VALIDATION_FAILED
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import ratpack.exec.Promise.value

class ValidationExtensionsTest {

    internal enum class MyErrorCodes {
        VALIDATION_FAILED, PROMISE_FAILED
    }

    @Test
    internal fun `valid with result if validation and promise succeed`() {
        val execResult = ExecHarness.yieldSingle {
            val validation = valid<MyErrorCodes, String>("value")

            validation.flatMapPromise { value(valid<MyErrorCodes, Int>(it.length)) }
        }

        assertEquals(5, execResult.value.get() as Any)
        assertThrows(NoSuchElementException::class.java) { execResult.value.getError() }
        assertNull(execResult.throwable)
    }

    @Test
    internal fun `invalid with no result if validation fails`() {
        val execResult = ExecHarness.yieldSingle {
            val validation = invalid<MyErrorCodes, String>(VALIDATION_FAILED)

            validation.flatMapPromise { value(valid<MyErrorCodes, Int>(it.length)) }
        }

        assertEquals(VALIDATION_FAILED, execResult.value.error)
        assertThrows(NoSuchElementException::class.java) { execResult.value.get() }
        assertNull(execResult.throwable)
    }

    @Test
    internal fun `invalid with no result if promise fails`() {
        val execResult = ExecHarness.yieldSingle {
            val validation = valid<MyErrorCodes, String>("value")

            validation.flatMapPromise { value(invalid<MyErrorCodes, Int>(PROMISE_FAILED)) }
        }

        assertEquals(PROMISE_FAILED, execResult.value.error)
        assertThrows(NoSuchElementException::class.java) { execResult.value.get() }
        assertNull(execResult.throwable)
    }

}

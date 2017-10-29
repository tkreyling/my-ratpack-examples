package myratpackexamples.promises

import io.vavr.control.Validation
import ratpack.exec.Promise

import java.util.function.Function

import io.vavr.control.Validation.invalid
import ratpack.exec.Promise.value

object ValidationUtil {
    fun <E, I, O> flatMapPromise(
            validation: Validation<E, I>, function: (I) -> Promise<Validation<E, O>>): Promise<Validation<E, O>> {

        return if (validation.isInvalid) {
            value(invalid(validation.error))
        } else {
            function.invoke(validation.get())
        }
    }
}

package myratpackexamples.promises

import io.vavr.control.Validation
import io.vavr.control.Validation.invalid
import ratpack.exec.Promise
import ratpack.exec.Promise.value

fun <E, I, O> Validation<E, I>.flatMapPromise(function: (I) -> Promise<Validation<E, O>>): Promise<Validation<E, O>> =
        if (isInvalid) value(invalid(error)) else function.invoke(get())

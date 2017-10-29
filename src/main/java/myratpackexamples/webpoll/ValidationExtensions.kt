package myratpackexamples.webpoll

import io.vavr.Function2
import io.vavr.collection.Seq
import io.vavr.control.Validation
import io.vavr.control.Validation.invalid
import ratpack.exec.Promise
import ratpack.exec.Promise.value

fun <E, R, T1, T2> Validation.Builder<E, T1, T2>.ap(f: (T1, T2) -> R): Validation<Seq<E>, R> =
        ap(object : Function2<T1, T2, R> {
            override fun apply(t1: T1, t2: T2): R = f(t1, t2)
        })

fun <E, I, O> Validation<E, I>.flatMapPromise(function: (I) -> Promise<Validation<E, O>>): Promise<Validation<E, O>> =
        if (isInvalid) value(invalid(error)) else function.invoke(get())
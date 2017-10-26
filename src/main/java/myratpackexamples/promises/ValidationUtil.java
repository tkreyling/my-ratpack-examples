package myratpackexamples.promises;

import io.vavr.control.Validation;
import ratpack.exec.Promise;

import java.util.function.Function;

import static io.vavr.control.Validation.invalid;
import static ratpack.exec.Promise.value;

public class ValidationUtil {
    public static <E, I, O> Promise<Validation<E, O>> flatMapPromise(
            Validation<E, I> validation, Function<I, Promise<Validation<E, O>>> function) {

        if (validation.isInvalid()) {
            return value(invalid(validation.getError()));
        } else {
            return function.apply(validation.get());
        }
    }
}

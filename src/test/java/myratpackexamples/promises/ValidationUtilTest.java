package myratpackexamples.promises;

import io.vavr.control.Validation;
import org.junit.jupiter.api.Test;
import ratpack.exec.ExecResult;
import ratpack.test.exec.ExecHarness;

import java.util.NoSuchElementException;

import static io.vavr.control.Validation.invalid;
import static io.vavr.control.Validation.valid;
import static myratpackexamples.webpoll.ValidationExtensionsKt.flatMapPromise;
import static myratpackexamples.promises.ValidationUtilTest.MyErrorCodes.PROMISE_FAILED;
import static myratpackexamples.promises.ValidationUtilTest.MyErrorCodes.VALIDATION_FAILED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static ratpack.exec.Promise.value;

public class ValidationUtilTest {

    enum MyErrorCodes {
        VALIDATION_FAILED, PROMISE_FAILED
    }

    @Test
    void valid_with_result_if_validation_and_promise_succeed() throws Exception {
        ExecResult<Validation<MyErrorCodes, Integer>> execResult = ExecHarness
                .yieldSingle(e -> {
                    Validation<MyErrorCodes, String> validation = valid("value");

                    return flatMapPromise(validation, string -> value(valid(string.length())));
                });

        assertEquals(5, (Object) execResult.getValue().get());
        assertThrows(NoSuchElementException.class, () -> execResult.getValue().getError());
        assertNull(execResult.getThrowable());
    }

    @Test
    void invalid_with_no_result_if_validation_fails() throws Exception {
        ExecResult<Validation<MyErrorCodes, Integer>> execResult = ExecHarness
                .yieldSingle(e -> {
                    Validation<MyErrorCodes, String> validation = invalid(VALIDATION_FAILED);

                    return flatMapPromise(validation, string -> value(valid(string.length())));
                });

        assertEquals(VALIDATION_FAILED, execResult.getValue().getError());
        assertThrows(NoSuchElementException.class, () -> execResult.getValue().get());
        assertNull(execResult.getThrowable());
    }

    @Test
    void invalid_with_no_result_if_promise_fails() throws Exception {
        ExecResult<Validation<MyErrorCodes, Integer>> execResult = ExecHarness
                .yieldSingle(e -> {
                    Validation<MyErrorCodes, String> validation = valid("value");

                    return flatMapPromise(validation, string -> value(invalid(PROMISE_FAILED)));
                });

        assertEquals(PROMISE_FAILED, execResult.getValue().getError());
        assertThrows(NoSuchElementException.class, () -> execResult.getValue().get());
        assertNull(execResult.getThrowable());
    }

}

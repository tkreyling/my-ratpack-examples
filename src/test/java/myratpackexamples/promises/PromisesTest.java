package myratpackexamples.promises;

import org.junit.jupiter.api.Test;
import ratpack.exec.Promise;
import ratpack.test.exec.ExecHarness;
import static org.junit.jupiter.api.Assertions.assertSame;

public class PromisesTest {
    @Test
    void error() throws Exception {
        Exception exception = new Exception();

        Throwable error = ExecHarness
                .yieldSingle(e -> doSomeAsyncWorkWithError(exception))
                .getThrowable();

        assertSame(exception, error);
    }

    private Promise<Object> doSomeAsyncWorkWithError(Exception exception) {
        return Promise.error(exception);
    }

    @Test
    void throwing_an_exception_will_not_throw_an_exception_but_produce_an_error() throws Exception {
        Exception exception = new Exception();

        Throwable error = ExecHarness
                .yieldSingle(e -> doSomeAsyncWorkAndThrowException(exception))
                .getThrowable();

        assertSame(exception, error);
    }

    private Promise<Object> doSomeAsyncWorkAndThrowException(Exception exception) throws Exception {
        throw exception;
    }
}

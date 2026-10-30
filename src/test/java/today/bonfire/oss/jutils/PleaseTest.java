package today.bonfire.oss.jutils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

@Timeout(30)
class PleaseTest {

    @Test
    void testCheckIfThrows() {
        assertThat(Please.checkIfThrows(() -> {
                    throw new IllegalStateException();
                }))
                .isTrue();
        assertThat(Please.checkIfThrows(() -> {})).isFalse();
    }

    @Test
    void testTryThisWithHandlerSuccess() {
        assertThat(Please.tryThis(() -> "ok", e -> "handled")).isEqualTo("ok");
    }

    @Test
    void testTryThisWithHandlerFailure() {
        Callable<String> failing = () -> {
            throw new IllegalStateException("x");
        };
        assertThat(Please.tryThis(failing, e -> "handled " + e.getMessage())).isEqualTo("handled x");
    }

    @Test
    void testTryThisLoggingOverload() {
        assertThat(Please.tryThis(() -> "ok")).isEqualTo("ok");
        Callable<String> failing = () -> {
            throw new IllegalStateException("x");
        };
        assertThat(Please.tryThis(failing)).isNull();
    }

    @Test
    void testTryThisFinallyRunsOnSuccessAndFailure() {
        AtomicBoolean ranOnSuccess = new AtomicBoolean();
        assertThat(Please.tryThis(() -> "ok", e -> "handled", () -> ranOnSuccess.set(true)))
                .isEqualTo("ok");
        assertThat(ranOnSuccess).isTrue();

        AtomicBoolean ranOnFailure = new AtomicBoolean();
        Callable<String> failing = () -> {
            throw new IllegalStateException("x");
        };
        assertThat(Please.tryThis(failing, e -> "handled", () -> ranOnFailure.set(true)))
                .isEqualTo("handled");
        assertThat(ranOnFailure).isTrue();
    }

    @Test
    void testTryThisRunnableSwallowsException() {
        AtomicBoolean ran = new AtomicBoolean();
        Please.tryThis((Runnable) () -> ran.set(true));
        assertThat(ran).isTrue();
        Please.tryThis((Runnable) () -> {
            throw new IllegalStateException("x");
        });
    }

    @Test
    void testInterruptFlagRestoredWithHandler() {
        Callable<String> interrupting = () -> {
            throw new InterruptedException("stop");
        };
        AtomicReference<Boolean> flagInHandler = new AtomicReference<>();
        try {
            String result = Please.tryThis(interrupting, e -> {
                flagInHandler.set(Thread.currentThread().isInterrupted());
                return "handled";
            });
            assertThat(result).isEqualTo("handled");
            assertThat(flagInHandler.get()).isTrue();
            assertThat(Thread.currentThread().isInterrupted()).isTrue();
        } finally {
            Thread.interrupted();
        }
    }

    @Test
    void testInterruptFlagRestoredWithLoggingOverload() {
        Callable<String> interrupting = () -> {
            throw new InterruptedException("stop");
        };
        try {
            assertThat(Please.tryThis(interrupting)).isNull();
            assertThat(Thread.currentThread().isInterrupted()).isTrue();
        } finally {
            Thread.interrupted();
        }
    }

    @Test
    void testInterruptFlagRestoredWithFinallyOverload() {
        Callable<String> interrupting = () -> {
            throw new InterruptedException("stop");
        };
        AtomicBoolean finallyRan = new AtomicBoolean();
        try {
            Please.tryThis(interrupting, e -> "handled", () -> finallyRan.set(true));
            assertThat(Thread.currentThread().isInterrupted()).isTrue();
            assertThat(finallyRan).isTrue();
        } finally {
            Thread.interrupted();
        }
    }

    @Test
    void testOtherExceptionsDoNotSetInterruptFlag() {
        Callable<String> failing = () -> {
            throw new IllegalStateException("x");
        };
        Please.tryThis(failing);
        assertThat(Thread.currentThread().isInterrupted()).isFalse();
    }
}

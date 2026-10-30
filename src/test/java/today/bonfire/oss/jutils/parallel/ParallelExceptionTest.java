package today.bonfire.oss.jutils.parallel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ParallelExceptionTest {

    @Test
    void testMessageConstructorKeepsMessageAndContextWithoutCause() {
        Context<String> ctx = Context.of("submitter");

        var exception = new ParallelException("failure", ctx);

        assertThat(exception).hasMessage("failure").hasNoCause();
        assertThat(exception.getContext()).isSameAs(ctx);
    }

    @Test
    void testCauseConstructorKeepsCauseAndContextWithDerivedMessage() {
        Context<String> ctx = Context.of("submitter");
        var cause = new IllegalStateException("failure");

        var exception = new ParallelException(cause, ctx);

        assertThat(exception).hasMessage(cause.toString());
        assertThat(exception.getCause()).isSameAs(cause);
        assertThat(exception.getContext()).isSameAs(ctx);
    }

    @Test
    void testConstructorsRejectNullContext() {
        var cause = new IllegalStateException("failure");

        assertThatThrownBy(() -> new ParallelException("failure", null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("context");
        assertThatThrownBy(() -> new ParallelException(cause, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("context");
        assertThatThrownBy(() -> new ParallelException("failure", cause, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("context");
    }
}

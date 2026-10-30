package today.bonfire.oss.jutils.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.http.HttpResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class HttpStatusTest {

    @ParameterizedTest
    @CsvSource({
        // code, informational, success, redirect, clientError, serverError, error, custom
        "99,  false, false, false, false, false, false, false",
        "100, true,  false, false, false, false, false, false",
        "199, true,  false, false, false, false, false, false",
        "200, false, true,  false, false, false, false, false",
        "299, false, true,  false, false, false, false, false",
        "300, false, false, true,  false, false, false, false",
        "399, false, false, true,  false, false, false, false",
        "400, false, false, false, true,  false, true,  false",
        "499, false, false, false, true,  false, true,  false",
        "500, false, false, false, false, true,  true,  false",
        "599, false, false, false, false, true,  true,  false",
        "600, false, false, false, false, false, false, true"
    })
    @DisplayName("Should classify status codes at category boundaries")
    void shouldClassifyBoundaries(
            int code,
            boolean informational,
            boolean success,
            boolean redirect,
            boolean clientError,
            boolean serverError,
            boolean error,
            boolean custom) {
        var status = HttpStatus.of(code);
        assertThat(status.statusCode()).isEqualTo(code);
        assertThat(status.isInformational()).isEqualTo(informational);
        assertThat(status.isSuccess()).isEqualTo(success);
        assertThat(status.isRedirect()).isEqualTo(redirect);
        assertThat(status.isClientError()).isEqualTo(clientError);
        assertThat(status.isServerError()).isEqualTo(serverError);
        assertThat(status.isError()).isEqualTo(error);
        assertThat(status.isCustomStatus()).isEqualTo(custom);
    }

    @Test
    @DisplayName("Should be equal when status codes are equal")
    void shouldImplementEqualsAndHashCode() {
        var a = HttpStatus.of(404);
        var b = HttpStatus.of(404);
        assertThat(a).isEqualTo(a).isEqualTo(b).hasSameHashCodeAs(b);
        assertThat(a).isNotEqualTo(HttpStatus.of(500));
        assertThat(a).isNotEqualTo(null);
        assertThat(a).isNotEqualTo("404");
    }

    @Test
    @DisplayName("toString should return the bare number")
    void shouldReturnBareNumber() {
        assertThat(HttpStatus.of(204)).hasToString("204");
    }

    @Test
    @DisplayName("from should read the status code of the response")
    void shouldCreateFromResponse() {
        @SuppressWarnings("unchecked")
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(503);

        var status = HttpStatus.from(response);

        assertThat(status).isEqualTo(HttpStatus.of(503));
        assertThat(status.isServerError()).isTrue();
    }
}

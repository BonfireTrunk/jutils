package today.bonfire.oss.jutils.web;

import lombok.Getter;
import lombok.experimental.Accessors;

import java.net.http.HttpResponse;

/**
 * Represents an HTTP status code.
 * This class provides methods to create an instance of HttpStatus from an HTTP response,
 * check the category of the status code (e.g., success, error, redirect), and
 * convert the status code to a string representation.
 *
 * @since 1.0
 */
@Accessors(fluent = true, chain = true)
public class HttpStatus {

  /**
   * The HTTP status code.
   */
  @Getter
  private final int statusCode;

  /**
   * Private constructor to create an instance of HttpStatus with the given status code.
   *
   * @param statusCode the HTTP status code to set
   */
  private HttpStatus(int statusCode) {
    this.statusCode = statusCode;
  }

  /**
   * Creates a new HttpStatus instance from an HttpResponse.
   *
   * @param response the HttpResponse to extract the status code from
   * @return a new HttpStatus instance representing the status code of the response
   */
  public static HttpStatus from(HttpResponse<?> response) {
    return of(response.statusCode());
  }

  /**
   * Creates a new HttpStatus instance with the given status code.
   *
   * @param statusCode the status code to use
   * @return a new HttpStatus instance representing the provided status code
   */
  public static HttpStatus of(int statusCode) {
    return new HttpStatus(statusCode);
  }

  /**
   * Returns whether this status code is a redirect (3xx).
   *
   * @return true if this status code is a redirect, false otherwise
   */
  public boolean isRedirect() {
    return statusCode >= 300 && statusCode < 400;
  }

  /**
   * Returns whether this status code is a success (2xx).
   *
   * @return true if this status code is a success, false otherwise
   */
  public boolean isSuccess() {
    return statusCode >= 200 && statusCode < 300;
  }

  /**
   * Returns whether this status code is an error (4xx or 5xx).
   *
   * @return true if this status code is an error, false otherwise
   */
  public boolean isError() {
    return isClientError() || isServerError();
  }

  /**
   * Returns whether this status code is a client error (4xx).
   *
   * @return true if this status code is a client error, false otherwise
   */
  public boolean isClientError() {
    return statusCode >= 400 && statusCode < 500;
  }

  /**
   * Returns whether this status code is a server error (5xx).
   *
   * @return true if this status code is a server error, false otherwise
   */
  public boolean isServerError() {
    return statusCode >= 500 && statusCode < 600;
  }

  /**
   * Returns whether this status code is informational (1xx).
   *
   * @return true if this status code is informational, false otherwise
   */
  public boolean isInformational() {
    return statusCode >= 100 && statusCode < 200;
  }

  /**
   * Returns whether this status code is a custom status (>= 600).
   *
   * @return true if this status code is a custom status, false otherwise
   */
  public boolean isCustomStatus() {
    return statusCode >= 600;
  }

  @Override
  public String toString() {
    return String.valueOf(statusCode);
  }
}

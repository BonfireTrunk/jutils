package today.bonfire.jutils;

import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import today.bonfire.jutils.constants.DigestHash;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

@Log4j2
public class PasswordUtils {

  // cannot change these later easily
  private static final int    DEFAULT_ITERATIONS         = 100_000;
  private static final int    DEFAULT_KEY_LENGTH         = 512;
  private static final String DEFAULT_PASSWORD_ALGORITHM = "PBKDF2WithHmacSHA512";
  private final        int    iterations;

  public PasswordUtils(int iterations) {
    this.iterations = iterations;
  }

  /**
   * salting 😉
   *
   * @param string the string to be salted
   * @param salt   the salt to be added to the string
   * @return the salted string
   */
  public static String addSalt(final String string, final String salt) {
    byte[]     stringBytes = string.getBytes(StandardCharsets.UTF_8);
    byte[]     saltBytes   = salt.getBytes(StandardCharsets.UTF_8);
    ByteBuffer byteBuffer  = ByteBuffer.allocate(stringBytes.length + saltBytes.length);

    int maxLength = Math.max(stringBytes.length, saltBytes.length);
    for (int i = 0; i < maxLength; i++) {
      if (i < stringBytes.length) {
        byteBuffer.put(stringBytes[i]);
      }
      if (i < saltBytes.length) {
        byteBuffer.put(saltBytes[i]);
      }
    }

    return new String(byteBuffer.array(), StandardCharsets.UTF_8);
  }

  /**
   * @return 32 char string, ~192 bits randomness
   */
  public static String generateSalt() {
    return RandUtils.generateNewToken(24);
  }

  /**
   * @param prefix the prefix of the API key, keep it less than 5 characters
   * @return 86 char string, ~488 bits randomness
   */
  public static String generateApiKey(@NonNull String prefix) {
    if (prefix.length() > 4) {
      throw new IllegalArgumentException("prefix must be less than 5 characters");
    }
    var key = RandUtils.generateNewToken(64);
    return prefix + key.substring(prefix.length());
  }

  /**
   * Verifies if the given API key matches the hashed key from the database.
   *
   * @param apiKey      the API key to be verified
   * @param hashedKeyDb the hashed API key from the database
   * @return true if the API key matches the hashed key, false otherwise
   */
  public static boolean verifyApiKey(String apiKey, String hashedKeyDb) {
    if (StringUtils.isAnyBlank(apiKey, hashedKeyDb) || apiKey.length() < 32) return false;
    String optEncrypted = hashApiKey(apiKey);
    return hashedKeyDb.equals(optEncrypted);
  }

  /**
   * using sha3_512 to digest hash values, cannot be retrieved again. should be fast and good enough to not being able
   * to recover the key again. hopefully for next 20 years.
   * do not need iterative hash as key itself has high randomness since it should use system generated
   *
   * @return hashed key
   */
  public static String hashApiKey(@NotNull @NonNull String key) {
    return Encoder.toBase64(Digest.getDigest(DigestHash.SHA_3_512).digest(Encoder.toByteArray(key)));
  }

  public boolean verifyPassword(String password, String hashedPassword, String salt) {
    String optEncrypted = hashPassword(password, salt);
    return hashedPassword.equals(optEncrypted);
  }


  public String hashPassword(String password, String salt) {
    PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), Encoder.toByteArray(salt), iterations, DEFAULT_KEY_LENGTH);
    try {
      SecretKeyFactory fac            = SecretKeyFactory.getInstance(DEFAULT_PASSWORD_ALGORITHM);
      byte[]           securePassword = fac.generateSecret(spec).getEncoded();
      return Encoder.toBase64(securePassword);
    } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
      log.error("Error Password hashing ", ex);
      throw new RuntimeException(ex);
    } finally {
      spec.clearPassword();
    }
  }

  /**
   * builder for {@link PasswordUtils}
   */
  public static final class Builder {

    private int iterations = DEFAULT_ITERATIONS;

    public Builder iteration(int iterations) {
      this.iterations = iterations;
      return this;
    }

    public PasswordUtils build() {
      return new PasswordUtils(iterations);
    }
  }
}

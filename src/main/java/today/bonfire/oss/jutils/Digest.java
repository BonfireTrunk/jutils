package today.bonfire.oss.jutils;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.jspecify.annotations.Nullable;
import today.bonfire.oss.jutils.constants.HashAlgorithm;
import today.bonfire.oss.jutils.constants.HmacAlgorithm;

/**
 * Static helpers for message digests, HMACs and HMAC verification. Parameters must not be null, except for the
 * expected signatures in verification methods and both arguments of {@link #isEqual(byte[], byte[])}. Other null
 * arguments throw {@link NullPointerException}; strings are converted to bytes as UTF-8.
 */
public final class Digest {

    private Digest() {}

    /**
     * Computes a message digest.
     *
     * @param algorithm the hash algorithm
     * @param data      the bytes to hash
     *
     * @return the raw digest; its length is fixed by the algorithm
     *
     * @throws NullPointerException  if an argument is null
     * @throws IllegalStateException if the JVM does not provide the algorithm
     */
    public static byte[] hash(HashAlgorithm algorithm, byte[] data) {
        Objects.requireNonNull(algorithm, "algorithm");
        Objects.requireNonNull(data, "data");
        try {
            return MessageDigest.getInstance(algorithm.jcaName()).digest(data);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Hash algorithm not available: " + algorithm.jcaName(), e);
        }
    }

    /**
     * Computes a message digest of the UTF-8 bytes of a string.
     *
     * @param algorithm the hash algorithm
     * @param data      the string to hash
     *
     * @return the raw digest; its length is fixed by the algorithm
     *
     * @throws NullPointerException  if an argument is null
     * @throws IllegalStateException if the JVM does not provide the algorithm
     */
    public static byte[] hash(HashAlgorithm algorithm, String data) {
        Objects.requireNonNull(data, "data");
        return hash(algorithm, data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Computes a message digest of the UTF-8 bytes of a string and returns it as lowercase hexadecimal.
     *
     * @param algorithm the hash algorithm
     * @param data      the string to hash
     *
     * @return the digest in lowercase hex, two characters per byte
     *
     * @throws NullPointerException  if an argument is null
     * @throws IllegalStateException if the JVM does not provide the algorithm
     */
    public static String hashHex(HashAlgorithm algorithm, String data) {
        return Objects.requireNonNull(Encoder.toBase16(hash(algorithm, data)));
    }

    /**
     * Computes a message digest of the UTF-8 bytes of a string and returns it as URL-safe Base64 without padding.
     *
     * @param algorithm the hash algorithm
     * @param data      the string to hash
     *
     * @return the digest in URL-safe Base64 (alphabet {@code A-Z a-z 0-9 - _}), no padding
     *
     * @throws NullPointerException  if an argument is null
     * @throws IllegalStateException if the JVM does not provide the algorithm
     */
    public static String hashBase64Url(HashAlgorithm algorithm, String data) {
        return Objects.requireNonNull(Encoder.toBase64Url(hash(algorithm, data)));
    }

    /**
     * Computes a message digest of the UTF-8 bytes of a string and returns it as RFC 4648 Base32.
     *
     * @param algorithm the hash algorithm
     * @param data      the string to hash
     *
     * @return the digest in lowercase Base32 (alphabet {@code a-z 2-7}), no padding
     *
     * @throws NullPointerException  if an argument is null
     * @throws IllegalStateException if the JVM does not provide the algorithm
     */
    public static String hashBase32(HashAlgorithm algorithm, String data) {
        return Objects.requireNonNull(Encoder.toBase32(hash(algorithm, data)));
    }

    /**
     * Computes an HMAC.
     *
     * @param algorithm the HMAC algorithm
     * @param data      the bytes to authenticate
     * @param key       the secret key; must not be empty
     *
     * @return the raw HMAC; its length is fixed by the algorithm
     *
     * @throws NullPointerException     if an argument is null
     * @throws IllegalArgumentException if the key is empty or rejected by the algorithm
     * @throws IllegalStateException    if the JVM does not provide the algorithm
     */
    public static byte[] hmac(HmacAlgorithm algorithm, byte[] data, byte[] key) {
        Objects.requireNonNull(algorithm, "algorithm");
        Objects.requireNonNull(data, "data");
        Objects.requireNonNull(key, "key");
        if (key.length == 0) {
            throw new IllegalArgumentException("key must not be empty");
        }
        try {
            Mac mac = Mac.getInstance(algorithm.jcaName());
            mac.init(new SecretKeySpec(key, algorithm.jcaName()));
            return mac.doFinal(data);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("HMAC algorithm not available: " + algorithm.jcaName(), e);
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException("Invalid HMAC key", e);
        }
    }

    /**
     * Computes an HMAC over the UTF-8 bytes of a string, keyed with the UTF-8 bytes of a string.
     *
     * @param algorithm the HMAC algorithm
     * @param data      the string to authenticate
     * @param key       the secret key; must not be empty
     *
     * @return the raw HMAC; its length is fixed by the algorithm
     *
     * @throws NullPointerException     if an argument is null
     * @throws IllegalArgumentException if the key is empty or rejected by the algorithm
     * @throws IllegalStateException    if the JVM does not provide the algorithm
     */
    public static byte[] hmac(HmacAlgorithm algorithm, String data, String key) {
        Objects.requireNonNull(data, "data");
        Objects.requireNonNull(key, "key");
        return hmac(algorithm, data.getBytes(StandardCharsets.UTF_8), key.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Computes an HMAC over the UTF-8 bytes of a string and returns it as lowercase hexadecimal.
     *
     * @param algorithm the HMAC algorithm
     * @param data      the string to authenticate
     * @param key       the secret key; must not be empty
     *
     * @return the HMAC in lowercase hex, two characters per byte
     *
     * @throws NullPointerException     if an argument is null
     * @throws IllegalArgumentException if the key is empty or rejected by the algorithm
     * @throws IllegalStateException    if the JVM does not provide the algorithm
     */
    public static String hmacHex(HmacAlgorithm algorithm, String data, String key) {
        return Objects.requireNonNull(Encoder.toBase16(hmac(algorithm, data, key)));
    }

    /**
     * Computes an HMAC over the UTF-8 bytes of a string and returns it as URL-safe Base64 without padding.
     *
     * @param algorithm the HMAC algorithm
     * @param data      the string to authenticate
     * @param key       the secret key; must not be empty
     *
     * @return the HMAC in URL-safe Base64 (alphabet {@code A-Z a-z 0-9 - _}), no padding
     *
     * @throws NullPointerException     if an argument is null
     * @throws IllegalArgumentException if the key is empty or rejected by the algorithm
     * @throws IllegalStateException    if the JVM does not provide the algorithm
     */
    public static String hmacBase64Url(HmacAlgorithm algorithm, String data, String key) {
        return Objects.requireNonNull(Encoder.toBase64Url(hmac(algorithm, data, key)));
    }

    /**
     * Computes an HMAC and compares its bytes with the expected signature using {@link #isEqual(byte[], byte[])}.
     * The comparison is constant time for equal lengths. A null, empty or wrong-length signature returns false.
     *
     * <p>For signatures supplied as text, the caller removes any scheme prefix such as {@code sha256=} first.
     * For standard Base64, decode with {@link Encoder#fromBase64Basic(String)} and use this byte array overload.
     *
     * @param algorithm the HMAC algorithm
     * @param data      the bytes to authenticate, may be empty
     * @param key       the secret key; must not be empty
     * @param expected  the expected raw signature, may be null or empty
     *
     * @return true if the expected signature matches the computed HMAC; false otherwise
     *
     * @throws NullPointerException     if algorithm, data or key is null
     * @throws IllegalArgumentException if the key is empty or rejected by the algorithm
     * @throws IllegalStateException    if the JVM does not provide the algorithm
     */
    public static boolean verifyHmac(HmacAlgorithm algorithm, byte[] data, byte[] key, byte @Nullable [] expected) {
        return isEqual(hmac(algorithm, data, key), expected);
    }

    /**
     * Computes an HMAC over the UTF-8 bytes of data and key and compares bytes using {@link #isEqual(byte[], byte[])}.
     * The expected signature is hexadecimal in either case; surrounding whitespace is ignored. A null, blank,
     * wrong-length or invalid hexadecimal signature returns false. The comparison is constant time for equal lengths.
     *
     * <p>The caller removes any scheme prefix such as {@code sha256=} first. For standard Base64, decode with
     * {@link Encoder#fromBase64Basic(String)} and use {@link #verifyHmac(HmacAlgorithm, byte[], byte[], byte[])}.
     *
     * @param algorithm   the HMAC algorithm
     * @param data        the string to authenticate, may be empty
     * @param key         the secret key; must not be empty
     * @param expectedHex the expected hexadecimal signature, may be null, empty or blank
     *
     * @return true if the decoded signature matches the computed HMAC; false otherwise
     *
     * @throws NullPointerException     if algorithm, data or key is null
     * @throws IllegalArgumentException if the key is empty or rejected by the algorithm
     * @throws IllegalStateException    if the JVM does not provide the algorithm
     */
    public static boolean verifyHmacHex(
            HmacAlgorithm algorithm, String data, String key, @Nullable String expectedHex) {
        var actual = hmac(algorithm, data, key);
        try {
            return isEqual(actual, Encoder.fromBase16(expectedHex));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Computes an HMAC over the UTF-8 bytes of data and key and compares bytes using {@link #isEqual(byte[], byte[])}.
     * The expected signature is URL-safe Base64 with optional padding; surrounding whitespace is ignored. A null,
     * blank, wrong-length or invalid URL-safe Base64 signature returns false. The comparison is constant time for
     * equal lengths.
     *
     * <p>The caller removes any scheme prefix such as {@code sha256=} first. For standard Base64, decode with
     * {@link Encoder#fromBase64Basic(String)} and use {@link #verifyHmac(HmacAlgorithm, byte[], byte[], byte[])}.
     *
     * @param algorithm         the HMAC algorithm
     * @param data              the string to authenticate, may be empty
     * @param key               the secret key; must not be empty
     * @param expectedBase64Url the expected URL-safe Base64 signature, may be null, empty or blank
     *
     * @return true if the decoded signature matches the computed HMAC; false otherwise
     *
     * @throws NullPointerException     if algorithm, data or key is null
     * @throws IllegalArgumentException if the key is empty or rejected by the algorithm
     * @throws IllegalStateException    if the JVM does not provide the algorithm
     */
    public static boolean verifyHmacBase64Url(
            HmacAlgorithm algorithm, String data, String key, @Nullable String expectedBase64Url) {
        var actual = hmac(algorithm, data, key);
        try {
            return isEqual(actual, Encoder.fromBase64Url(expectedBase64Url));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Compares two byte arrays in constant time for equal lengths, using {@link MessageDigest#isEqual(byte[], byte[])}.
     * Use it to compare MACs, hashes and tokens instead of {@code Arrays.equals}.
     *
     * @param a the first array, may be null
     * @param b the second array, may be null
     *
     * @return true if both are null or both have the same content; false if only one is null or the content differs
     */
    public static boolean isEqual(byte @Nullable [] a, byte @Nullable [] b) {
        return MessageDigest.isEqual(a, b);
    }
}

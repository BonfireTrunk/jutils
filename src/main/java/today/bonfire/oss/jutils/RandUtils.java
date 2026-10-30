package today.bonfire.oss.jutils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Utility class for generating random numbers, tokens, and IDs.
 */
public final class RandUtils {
    /**
     * Default byte length for token generation. 36 bytes provides:
     * - 288 bits (36 * 8) of entropy
     * - Results in 48 characters when base64 encoded (36 * 4/3)
     * - Provides sufficient collision resistance for most use cases
     * - Balances security with string length
     */
    private static final int DEF_RANDOM_BYTE_LEN = 36;

    /**
     * Base64 URL-safe alphabet with '-' and '_' for padding-free encoding
     * Used in general token generation where case sensitivity is required
     */
    private static final char[] DEFAULT_ALPHABET_64 =
            "-0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz".toCharArray();

    /**
     * Standard alphanumeric alphabet (0-9, A-Z, a-z)
     * Used when only alphanumeric characters are needed
     */
    private static final char[] DEFAULT_ALPHABET_62 =
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();

    /**
     * Word-safe alphabet avoiding similar looking characters
     * Useful for human readable IDs where visual confusion should be minimized
     * Excludes characters like: 0/O, 1/l/I, etc.
     */
    private static final char[] WORDSAFE_ALPHABET_32 = "23456789CFGHJMPQRVWXcfghjmpqrvwx".toCharArray();

    /**
     * Case-insensitive alphabet for generating IDs
     * Useful when case sensitivity doesn't matter and consistency is needed
     */
    private static final char[] CI_ALPHABET_32 = "23456789abcdefghijklmnpqrstvwxyz".toCharArray();

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder BASE64ENCODER_NOPADD =
            Base64.getUrlEncoder().withoutPadding();
    private static final int TUID_TIMESTAMP_BYTES = 6;
    private static final int PASSPHRASE_WORD_COUNT = 8192;

    private RandUtils() {}

    /**
     * Returns a random {@code int} value between the specified origin (inclusive) and bound (exclusive). Negative
     * ranges are allowed.
     *
     * @param start the least value returned
     * @param bound the upper bound (excluded)
     *
     * @return a random {@code int} value between the origin (inclusive) and the bound (exclusive)
     *
     * @throws IllegalArgumentException if bound is not greater than start
     */
    public static int newInt(int start, int bound) {
        if (bound <= start) {
            throw new IllegalArgumentException("bound must be greater than start");
        }
        return secureRandom.nextInt(start, bound);
    }

    /**
     * Returns a random {@code long} value between the specified origin (inclusive) and bound (exclusive). Negative
     * ranges are allowed.
     *
     * @param start the least value returned
     * @param bound the upper bound (excluded)
     *
     * @return a random {@code long} value between the origin (inclusive) and the bound (exclusive)
     *
     * @throws IllegalArgumentException if bound is not greater than start
     */
    public static long newLong(long start, long bound) {
        if (bound <= start) {
            throw new IllegalArgumentException("bound must be greater than start");
        }
        return secureRandom.nextLong(start, bound);
    }

    /**
     * Generates a token of 36 random bytes (288 bits) encoded as URL-safe Base64 without padding.
     *
     * @return a 48 character token using the alphabet {@code A-Z a-z 0-9 - _}
     */
    public static String generateNewToken() {
        return generateNewToken(DEF_RANDOM_BYTE_LEN);
    }

    /**
     * Generates a token of random bytes encoded as URL-safe Base64 without padding. The token has
     * {@code ceil(byteLength * 4 / 3)} characters, for example 1 byte gives 2 characters, 12 bytes give 16 and 36
     * bytes give 48. The alphabet is {@code A-Z a-z 0-9 - _}.
     *
     * @param byteLength number of random bytes, must be positive
     *
     * @return the token
     *
     * @throws IllegalArgumentException if byteLength is not positive
     */
    public static String generateNewToken(int byteLength) {
        if (byteLength <= 0) {
            throw new IllegalArgumentException("byteLength must be positive");
        }
        return BASE64ENCODER_NOPADD.encodeToString(generateByteArray(byteLength));
    }

    /**
     * Generates a byte array of secure random bytes.
     *
     * @param byteLength length of the array; 0 returns an empty array
     *
     * @return byte array of the specified length
     *
     * @throws IllegalArgumentException if byteLength is negative
     */
    public static byte[] generateByteArray(int byteLength) {
        if (byteLength < 0) {
            throw new IllegalArgumentException("byteLength must not be negative");
        }
        var randomBytes = new byte[byteLength];
        secureRandom.nextBytes(randomBytes);
        return randomBytes;
    }

    /**
     * Generates a nano ID using the 62-character alphabet {@code 0-9 A-Z a-z}.
     *
     * @param size the length of the nano ID to be generated
     *
     * @return a nano ID of exactly {@code size} characters
     *
     * @throws IllegalArgumentException if size is not positive
     */
    public static String nanoId62(int size) {
        return nanoId(DEFAULT_ALPHABET_62, size);
    }

    /**
     * Generates a nano ID using the 64-character alphabet {@code -0-9 A-Z _ a-z}.
     *
     * @param size the length of the nano ID to be generated
     *
     * @return a nano ID of exactly {@code size} characters
     *
     * @throws IllegalArgumentException if size is not positive
     */
    public static String nanoId(int size) {
        return nanoId(DEFAULT_ALPHABET_64, size);
    }

    /**
     * Generates a nano ID from the given alphabet. Each character is chosen uniformly: for an alphabet whose length
     * is a power of two up to 256, random bytes are masked to the alphabet size; otherwise
     * {@link SecureRandom#nextInt(int)} is used, which rejects biased values.
     *
     * @param alphabet the characters to choose from; characters may repeat, which weights them accordingly
     * @param size     the length of the nano ID to be generated
     *
     * @return a nano ID of exactly {@code size} characters
     *
     * @throws IllegalArgumentException if alphabet is null or empty, or size is not positive
     */
    public static String nanoId(final char @Nullable [] alphabet, final int size) {
        if (alphabet == null || alphabet.length == 0) {
            throw new IllegalArgumentException("alphabet must not be null or empty");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("size must be positive");
        }
        int length = alphabet.length;
        var result = new char[size];
        if (length <= 256 && (length & (length - 1)) == 0) {
            int mask = length - 1;
            var bytes = generateByteArray(size);
            for (int i = 0; i < size; i++) {
                result[i] = alphabet[bytes[i] & mask];
            }
        } else {
            for (int i = 0; i < size; i++) {
                result[i] = alphabet[secureRandom.nextInt(length)];
            }
        }
        return new String(result);
    }

    /**
     * Generates a word-safe nano ID from the 32-character alphabet {@code 23456789CFGHJMPQRVWXcfghjmpqrvwx}, which
     * avoids look-alike characters and vowels.
     *
     * @param size the length of the nano ID to be generated
     *
     * @return a word-safe nano ID of exactly {@code size} characters
     *
     * @throws IllegalArgumentException if size is not positive
     */
    public static String nanoIdWordSafe(int size) {
        return nanoId(WORDSAFE_ALPHABET_32, size);
    }

    /**
     * Generates a case-insensitive nano ID from the 32-character lowercase alphabet
     * {@code 23456789abcdefghijklmnpqrstvwxyz}.
     *
     * @param size the length of the nano ID to be generated
     *
     * @return a nano ID of exactly {@code size} characters
     *
     * @throws IllegalArgumentException if size is not positive
     */
    public static String nanoIdCaseInsensitive(int size) {
        return nanoId(CI_ALPHABET_32, size);
    }

    /**
     * Generates a bucket or folder name: 20 random bytes (160 bits) as lowercase RFC 4648 Base32 without padding.
     * It is always 32 characters from {@code a-z 2-7}, valid as an S3 bucket or folder name.
     *
     * @return a 32 character name
     */
    public static String genBucketName() {
        return Objects.requireNonNull(Encoder.toBase32(generateByteArray(20)));
    }

    /**
     * Generates a URL path segment with good collision resistance: 44 alphanumeric characters ({@code 0-9 A-Z a-z}),
     * about 262 bits of entropy. Suitable for items such as images or files in object storage.
     *
     * @return a 44 character string
     */
    public static String getUrlPath() {
        return nanoId62(44);
    }

    /**
     * Simulates rolling a six-sided die a specified number of times and returns the concatenated result as an integer.
     *
     * @param times the number of times to roll the die; must be positive.
     *
     * @return a number formed by concatenating the results of each die roll.
     *   but returned as a string. You can convert it to an int/long/bigint if you want.
     *
     * @throws IllegalArgumentException if {@code times} is less than or equal to zero.
     */
    public static String diceRolls(int times) {
        if (times <= 0) {
            throw new IllegalArgumentException("times must be positive");
        }
        StringBuilder sum = new StringBuilder();
        for (int i = 0; i < times; i++) {
            sum.append(secureRandom.nextInt(1, 7));
        }
        return sum.toString();
    }

    /**
     * Generates a passphrase of {@code words} words separated by {@code "-"}. See
     * {@link #passphrase(int, String)}.
     *
     * @param words the number of words, at least 1
     *
     * @return the passphrase
     *
     * @throws IllegalArgumentException if words is less than 1
     * @throws IllegalStateException    if the bundled wordlist is missing or malformed
     */
    public static String passphrase(int words) {
        return passphrase(words, "-");
    }

    /**
     * Generates a passphrase of words joined by a separator. Each word is chosen uniformly and independently from the
     * 8,192 words of the bundled list, which gives 13 bits of entropy per word (6 words are 78 bits). Every word is
     * 3 to 9 lowercase letters, and no word is a prefix of another, so the words stay recoverable even with an empty
     * separator.
     *
     * <p>The list is adapted from the EFF Large Wordlist for Passphrases (https://www.eff.org/dice) and is licensed
     * CC BY 4.0; see NOTICE.
     *
     * @param words     the number of words, at least 1
     * @param separator text placed between words; may be empty
     *
     * @return the passphrase
     *
     * @throws IllegalArgumentException if words is less than 1
     * @throws NullPointerException     if separator is null
     * @throws IllegalStateException    if the bundled wordlist is missing or malformed
     */
    public static String passphrase(int words, String separator) {
        if (words < 1) {
            throw new IllegalArgumentException("words must be at least 1");
        }
        Objects.requireNonNull(separator, "separator");
        var list = WordList.WORDS;
        var sb = new StringBuilder();
        for (int i = 0; i < words; i++) {
            if (i > 0) sb.append(separator);
            sb.append(list.get(secureRandom.nextInt(PASSPHRASE_WORD_COUNT)));
        }
        return sb.toString();
    }

    /**
     * Generates a timestamp-based unique ID (TUID), similar to a ULID or UUIDv7: a 48 bit big-endian epoch
     * millisecond timestamp followed by 80 random bits, encoded as sortable Base32 ({@link Encoder#toBase32Lex}).
     *
     * <p>IDs of the same size and encoding sort lexicographically by creation millisecond. The order within one
     * millisecond is random, so IDs are not monotonic.
     *
     * @return a 26 character TUID (10 timestamp characters plus 16 random characters)
     */
    public static String tuid() {
        return tuid(10, true);
    }

    /**
     * Generates a TUID with {@code size} random bytes, encoded as sortable Base64. Equivalent to
     * {@code tuid(size, false)}.
     *
     * @param size the number of random bytes, must be positive
     *
     * @return a TUID of {@code ceil((6 + size) * 8 / 6)} characters
     *
     * @throws IllegalArgumentException if size is not positive
     */
    public static String tuid(int size) {
        return tuid(size, false);
    }

    /**
     * Generates a timestamp-based unique ID (TUID): a 6 byte big-endian epoch millisecond timestamp followed by
     * {@code size} random bytes. With {@code base32} the bytes are encoded with {@link Encoder#toBase32Lex}
     * (uppercase Crockford alphabet, {@code ceil((6 + size) * 8 / 5)} characters), otherwise with
     * {@link Encoder#toBase64Lex} (ASCII-ascending alphabet, {@code ceil((6 + size) * 8 / 6)} characters).
     *
     * <p>IDs of the same size and encoding sort lexicographically by creation millisecond. The order within one
     * millisecond is random, so IDs are not monotonic.
     *
     * @param size   the number of random bytes, must be positive
     * @param base32 true for sortable Base32, false for sortable Base64
     *
     * @return the TUID
     *
     * @throws IllegalArgumentException if size is not positive
     */
    public static String tuid(int size, boolean base32) {
        if (size <= 0) {
            throw new IllegalArgumentException("size must be positive");
        }
        var bytes = new byte[TUID_TIMESTAMP_BYTES + size];
        System.arraycopy(epochMillisBytes(), 0, bytes, 0, TUID_TIMESTAMP_BYTES);
        System.arraycopy(generateByteArray(size), 0, bytes, TUID_TIMESTAMP_BYTES, size);
        return Objects.requireNonNull(base32 ? Encoder.toBase32Lex(bytes) : Encoder.toBase64Lex(bytes));
    }

    /**
     * Extracts the creation time of a TUID.
     *
     * @param tuid   a TUID produced by {@link #tuid(int, boolean)}
     * @param base32 true if the TUID is encoded as sortable Base32, false if sortable Base64
     *
     * @return the creation time with millisecond precision
     *
     * @throws IllegalArgumentException if the input is not valid for the encoding or decodes to fewer than 7 bytes
     */
    public static Instant tuidTimestamp(String tuid, boolean base32) {
        var bytes = base32 ? Encoder.fromBase32Lex(tuid) : Encoder.fromBase64Lex(tuid);
        if (bytes == null || bytes.length < TUID_TIMESTAMP_BYTES + 1) {
            throw new IllegalArgumentException("tuid must decode to at least 7 bytes");
        }
        long millis = 0;
        for (int i = 0; i < TUID_TIMESTAMP_BYTES; i++) {
            millis = (millis << 8) | (bytes[i] & 0xFF);
        }
        return Instant.ofEpochMilli(millis);
    }

    /**
     * Returns the current time as 6 big-endian epoch millisecond bytes encoded with {@link Encoder#toBase32Lex}.
     * The result is 10 uppercase characters and sorts lexicographically in time order.
     *
     * @return a 10 character sortable timestamp
     */
    public static String time() {
        return Objects.requireNonNull(Encoder.toBase32Lex(epochMillisBytes()));
    }

    private static byte[] epochMillisBytes() {
        var eight = ByteBuffer.allocate(Long.BYTES)
                .putLong(System.currentTimeMillis())
                .array();
        return Arrays.copyOfRange(eight, Long.BYTES - TUID_TIMESTAMP_BYTES, Long.BYTES);
    }

    /** Lazy holder so the wordlist is only read when a passphrase is first requested. */
    private static final class WordList {
        static final List<String> WORDS = load();

        private static List<String> load() {
            try (var in = RandUtils.class.getResourceAsStream("passphrase_wordlist.txt")) {
                if (in == null) {
                    throw new IllegalStateException("passphrase_wordlist.txt not found on the classpath");
                }
                var words = new ArrayList<String>(PASSPHRASE_WORD_COUNT);
                for (String line : new String(in.readAllBytes(), StandardCharsets.UTF_8).split("\n")) {
                    if (line.isBlank()) continue;
                    words.add(line.strip());
                }
                if (words.size() != PASSPHRASE_WORD_COUNT) {
                    throw new IllegalStateException("Expected " + PASSPHRASE_WORD_COUNT
                            + " words in passphrase_wordlist.txt but found " + words.size());
                }
                return List.copyOf(words);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }
}

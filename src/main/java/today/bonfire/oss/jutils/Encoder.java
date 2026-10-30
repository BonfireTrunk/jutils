package today.bonfire.oss.jutils;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.HexFormat;
import org.jsoup.Jsoup;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Safelist;
import org.jspecify.annotations.Nullable;

/**
 * Static helpers to encode and decode strings and byte arrays: URL form encoding, HTML escaping, Base64 (URL-safe,
 * standard, MIME and a sortable variant), Base32 (RFC 4648 and a sortable variant) and Base16.
 */
public final class Encoder {

    private static final Base64.Encoder BASE64_URL_ENCODER =
            Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder BASE64_URL_DECODER = Base64.getUrlDecoder();
    private static final Base64.Encoder BASE64_BASIC_ENCODER = Base64.getEncoder();
    private static final Base64.Decoder BASE64_BASIC_DECODER = Base64.getDecoder();
    private static final Base64.Encoder BASE64_MIME_ENCODER = Base64.getMimeEncoder();
    private static final Base64.Decoder BASE64_MIME_DECODER = Base64.getMimeDecoder();
    private static final HexFormat HEX = HexFormat.of();

    private static final Alphabet BASE32 = new Alphabet("abcdefghijklmnopqrstuvwxyz234567", 5, true);
    private static final Alphabet BASE32_LEX = new Alphabet("0123456789ABCDEFGHJKMNPQRSTVWXYZ", 5, true);
    private static final Alphabet BASE64_LEX =
            new Alphabet("-0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz", 6, false);

    private Encoder() {}

    /**
     * Encodes a string with {@link URLEncoder} using UTF-8. This is {@code application/x-www-form-urlencoded}
     * encoding: a space becomes {@code '+'}, not {@code %20}.
     *
     * @param value the string to encode, may be null, empty or blank
     *
     * @return the encoded string, or null if {@code value} is null
     */
    public static @Nullable String urlEncode(@Nullable String value) {
        return value == null ? null : URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    /**
     * Decodes a {@code application/x-www-form-urlencoded} string with {@link URLDecoder} using UTF-8. {@code '+'}
     * becomes a space.
     *
     * @param value the string to decode, may be null, empty or blank
     *
     * @return the decoded string, or null if {@code value} is null
     *
     * @throws IllegalArgumentException if the input contains a malformed percent escape
     */
    public static @Nullable String urlDecode(@Nullable String value) {
        return value == null ? null : URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    /**
     * Escapes a string for use in HTML text and in quoted (single or double) attribute values. The characters
     * {@code & < > " '} are replaced with {@code &amp; &lt; &gt; &quot; &#x27;}; everything else is unchanged.
     *
     * @param value the text to escape, may be null or empty
     *
     * @return the escaped string, or null if {@code value} is null
     */
    public static @Nullable String escapeHtml(@Nullable String value) {
        if (value == null) return null;
        var sb = new StringBuilder(value.length() + 16);
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '&' -> sb.append("&amp;");
                case '<' -> sb.append("&lt;");
                case '>' -> sb.append("&gt;");
                case '"' -> sb.append("&quot;");
                case '\'' -> sb.append("&#x27;");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Converts HTML to plain text: all tags are removed and entities are decoded. Uses JSoup's {@link Cleaner} with
     * {@link Safelist#none()}.
     *
     * <p>The result is plain text with entities decoded, so for example {@code &lt;b&gt;} becomes the characters
     * {@code <b>}. It is NOT safe to insert into HTML; use {@link #escapeHtml(String)} for that.
     *
     * @param value the HTML to convert, may be null or empty
     *
     * @return the plain text, or null if {@code value} is null
     */
    public static @Nullable String htmlToText(@Nullable String value) {
        if (value == null) return null;
        var cleaner = new Cleaner(Safelist.none());
        return cleaner.clean(Jsoup.parse(value)).text();
    }

    /**
     * Encodes bytes as Base64 with the URL-safe alphabet ({@code A-Z a-z 0-9 - _}) and no padding.
     *
     * @param bytes the bytes to encode, may be null or empty
     *
     * @return the encoded string (empty for an empty array), or null if {@code bytes} is null
     */
    public static @Nullable String toBase64Url(byte @Nullable [] bytes) {
        if (bytes == null) return null;
        return BASE64_URL_ENCODER.encodeToString(bytes);
    }

    /**
     * Encodes the UTF-8 bytes of a string as Base64 with the URL-safe alphabet and no padding.
     *
     * @param s the string to encode, may be null or empty
     *
     * @return the encoded string (empty for an empty string), or null if {@code s} is null
     */
    public static @Nullable String toBase64Url(@Nullable String s) {
        if (s == null) return null;
        return toBase64Url(toByteArray(s));
    }

    /**
     * Decodes Base64 written with the URL-safe alphabet. Padding is optional and surrounding whitespace is ignored.
     *
     * @param s the encoded string, may be null or empty
     *
     * @return the decoded bytes (empty for an empty string), or null if {@code s} is null
     *
     * @throws IllegalArgumentException if the input is not valid URL-safe Base64
     */
    public static byte @Nullable [] fromBase64Url(@Nullable String s) {
        if (s == null) return null;
        return BASE64_URL_DECODER.decode(s.strip());
    }

    /**
     * Encodes the UTF-8 bytes of a string as Base64 with the standard alphabet ({@code A-Z a-z 0-9 + /}) and
     * {@code '='} padding.
     *
     * @param s the string to encode, may be null or empty
     *
     * @return the encoded string (empty for an empty string), or null if {@code s} is null
     */
    public static @Nullable String toBase64Basic(@Nullable String s) {
        if (s == null) return null;
        return toBase64Basic(toByteArray(s));
    }

    /**
     * Encodes bytes as Base64 with the standard alphabet ({@code A-Z a-z 0-9 + /}) and {@code '='} padding.
     *
     * @param bytes the bytes to encode, may be null or empty
     *
     * @return the encoded string (empty for an empty array), or null if {@code bytes} is null
     */
    public static @Nullable String toBase64Basic(byte @Nullable [] bytes) {
        if (bytes == null) return null;
        return BASE64_BASIC_ENCODER.encodeToString(bytes);
    }

    /**
     * Decodes Base64 written with the standard alphabet. Padding is optional.
     *
     * @param s the encoded string, may be null or empty
     *
     * @return the decoded bytes (empty for an empty string), or null if {@code s} is null
     *
     * @throws IllegalArgumentException if the input is not valid standard Base64
     */
    public static byte @Nullable [] fromBase64Basic(@Nullable String s) {
        if (s == null) return null;
        return BASE64_BASIC_DECODER.decode(s);
    }

    /**
     * Encodes bytes as Base64 MIME: the standard alphabet with padding, in lines of at most 76 characters separated
     * by {@code \r\n}.
     *
     * @param bytes the bytes to encode, may be null or empty
     *
     * @return the encoded string (empty for an empty array), or null if {@code bytes} is null
     */
    public static @Nullable String toBase64Mime(byte @Nullable [] bytes) {
        if (bytes == null) return null;
        return BASE64_MIME_ENCODER.encodeToString(bytes);
    }

    /**
     * Encodes the UTF-8 bytes of a string as Base64 MIME: the standard alphabet with padding, in lines of at most 76
     * characters separated by {@code \r\n}.
     *
     * @param s the string to encode, may be null or empty
     *
     * @return the encoded string (empty for an empty string), or null if {@code s} is null
     */
    public static @Nullable String toBase64Mime(@Nullable String s) {
        if (s == null) return null;
        return toBase64Mime(toByteArray(s));
    }

    /**
     * Decodes Base64 MIME. Characters outside the Base64 alphabet, such as line breaks, are ignored.
     *
     * @param s the encoded string, may be null or empty
     *
     * @return the decoded bytes (empty for an empty string), or null if {@code s} is null
     *
     * @throws IllegalArgumentException if the remaining characters are not valid Base64
     */
    public static byte @Nullable [] fromBase64Mime(@Nullable String s) {
        if (s == null) return null;
        return BASE64_MIME_DECODER.decode(s);
    }

    /**
     * Encodes bytes as Base32 (RFC 4648): lowercase alphabet {@code a-z 2-7}, no padding. The output length is
     * {@code ceil(n * 8 / 5)}.
     *
     * @param bytes the bytes to encode, may be null or empty
     *
     * @return the encoded string (empty for an empty array), or null if {@code bytes} is null
     */
    public static @Nullable String toBase32(byte @Nullable [] bytes) {
        if (bytes == null) return null;
        return encode(bytes, BASE32);
    }

    /**
     * Encodes the UTF-8 bytes of a string as Base32 (RFC 4648): lowercase alphabet {@code a-z 2-7}, no padding.
     *
     * @param s the string to encode, may be null or empty
     *
     * @return the encoded string (empty for an empty string), or null if {@code s} is null
     */
    public static @Nullable String toBase32(@Nullable String s) {
        if (s == null) return null;
        return toBase32(toByteArray(s));
    }

    /**
     * Decodes Base32 (RFC 4648). Matching is case-insensitive and trailing {@code '='} padding is ignored.
     *
     * @param s the encoded string, may be null or empty
     *
     * @return the decoded bytes (empty for an empty string), or null if {@code s} is null
     *
     * @throws IllegalArgumentException if the input contains a character outside the alphabet or has an impossible
     *                                  length (length without padding modulo 8 is 1, 3 or 6)
     */
    public static byte @Nullable [] fromBase32(@Nullable String s) {
        if (s == null) return null;
        return decode(stripPadding(s), BASE32);
    }

    /**
     * Encodes bytes as sortable Base32 using the Crockford alphabet {@code 0123456789ABCDEFGHJKMNPQRSTVWXYZ}
     * (uppercase), no padding. The output length is {@code ceil(n * 8 / 5)}. For inputs of equal length the string
     * order equals the unsigned byte order.
     *
     * @param bytes the bytes to encode, may be null or empty
     *
     * @return the encoded string (empty for an empty array), or null if {@code bytes} is null
     */
    public static @Nullable String toBase32Lex(byte @Nullable [] bytes) {
        if (bytes == null) return null;
        return encode(bytes, BASE32_LEX);
    }

    /**
     * Decodes the output of {@link #toBase32Lex(byte[])}. Matching is case-insensitive; only the 32 alphabet
     * characters are accepted (the Crockford aliases I, L and O are rejected).
     *
     * @param s the encoded string, may be null or empty
     *
     * @return the decoded bytes (empty for an empty string), or null if {@code s} is null
     *
     * @throws IllegalArgumentException if the input contains a character outside the alphabet or has an impossible
     *                                  length (length modulo 8 is 1, 3 or 6)
     */
    public static byte @Nullable [] fromBase32Lex(@Nullable String s) {
        if (s == null) return null;
        return decode(s, BASE32_LEX);
    }

    /**
     * Encodes bytes as sortable Base64 using the ASCII-ascending alphabet
     * {@code -0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz}, 6 bits per character with the most
     * significant bits first, no padding. The output length is {@code ceil(n * 8 / 6)}. For inputs of equal length
     * the string order equals the unsigned byte order.
     *
     * @param bytes the bytes to encode, may be null or empty
     *
     * @return the encoded string (empty for an empty array), or null if {@code bytes} is null
     */
    public static @Nullable String toBase64Lex(byte @Nullable [] bytes) {
        if (bytes == null) return null;
        return encode(bytes, BASE64_LEX);
    }

    /**
     * Decodes the output of {@link #toBase64Lex(byte[])}. Matching is case-sensitive.
     *
     * @param s the encoded string, may be null or empty
     *
     * @return the decoded bytes (empty for an empty string), or null if {@code s} is null
     *
     * @throws IllegalArgumentException if the input contains a character outside the alphabet or its length modulo 4
     *                                  is 1
     */
    public static byte @Nullable [] fromBase64Lex(@Nullable String s) {
        if (s == null) return null;
        return decode(s, BASE64_LEX);
    }

    /**
     * Encodes bytes as Base16 (hexadecimal), lowercase, two characters per byte.
     *
     * @param bytes the bytes to encode, may be null or empty
     *
     * @return the encoded string (empty for an empty array), or null if {@code bytes} is null
     */
    public static @Nullable String toBase16(byte @Nullable [] bytes) {
        if (bytes == null) return null;
        return HEX.formatHex(bytes);
    }

    /**
     * Decodes Base16 (hexadecimal) in either case.
     *
     * @param s the encoded string, may be null or empty
     *
     * @return the decoded bytes (empty for an empty string), or null if {@code s} is null
     *
     * @throws IllegalArgumentException if the length is odd or the input contains a non-hexadecimal character
     */
    public static byte @Nullable [] fromBase16(@Nullable String s) {
        if (s == null) return null;
        return HEX.parseHex(s);
    }

    /**
     * Converts a string to a byte array using UTF-8 encoding.
     *
     * @param value the string to convert, must not be null
     *
     * @return the byte array representation of the string
     *
     * @throws NullPointerException if value is null
     */
    public static byte[] toByteArray(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Converts bytes to a string using UTF-8 decoding. Malformed sequences are replaced with U+FFFD.
     *
     * @param bytes the bytes to convert, may be null or empty
     *
     * @return the string, empty for an empty array, or null if {@code bytes} is null
     */
    public static @Nullable String toStr(byte @Nullable [] bytes) {
        if (bytes == null) return null;
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private static String stripPadding(String s) {
        int end = s.length();
        while (end > 0 && s.charAt(end - 1) == '=') end--;
        return s.substring(0, end);
    }

    private static String encode(byte[] in, Alphabet alphabet) {
        int bits = alphabet.bitsPerChar;
        int mask = (1 << bits) - 1;
        var out = new StringBuilder((in.length * 8 + bits - 1) / bits);
        int buffer = 0;
        int bufferedBits = 0;
        for (byte b : in) {
            buffer = (buffer << 8) | (b & 0xFF);
            bufferedBits += 8;
            while (bufferedBits >= bits) {
                bufferedBits -= bits;
                out.append(alphabet.chars[(buffer >> bufferedBits) & mask]);
            }
            buffer &= (1 << bufferedBits) - 1;
        }
        if (bufferedBits > 0) {
            out.append(alphabet.chars[(buffer << (bits - bufferedBits)) & mask]);
        }
        return out.toString();
    }

    private static byte[] decode(String in, Alphabet alphabet) {
        int bits = alphabet.bitsPerChar;
        int length = in.length();
        // Leftover bits of a final partial character can never complete a byte once they reach one full character.
        if ((length * bits) % 8 >= bits) {
            throw new IllegalArgumentException("Invalid length for " + alphabet.name() + ": " + length);
        }
        var out = new byte[length * bits / 8];
        int buffer = 0;
        int bufferedBits = 0;
        int pos = 0;
        for (int i = 0; i < length; i++) {
            char c = in.charAt(i);
            int value = c < alphabet.values.length ? alphabet.values[c] : -1;
            if (value < 0) {
                throw new IllegalArgumentException(
                        "Invalid character '" + c + "' at index " + i + " for " + alphabet.name());
            }
            buffer = (buffer << bits) | value;
            bufferedBits += bits;
            if (bufferedBits >= 8) {
                bufferedBits -= 8;
                out[pos++] = (byte) (buffer >> bufferedBits);
                buffer &= (1 << bufferedBits) - 1;
            }
        }
        return out;
    }

    private static final class Alphabet {
        final char[] chars;
        final int bitsPerChar;
        final int[] values = new int[128];

        Alphabet(String alphabet, int bitsPerChar, boolean ignoreCase) {
            this.chars = alphabet.toCharArray();
            this.bitsPerChar = bitsPerChar;
            Arrays.fill(values, -1);
            for (int i = 0; i < chars.length; i++) {
                values[chars[i]] = i;
                if (ignoreCase) {
                    values[Character.toLowerCase(chars[i])] = i;
                    values[Character.toUpperCase(chars[i])] = i;
                }
            }
        }

        String name() {
            return "base" + (1 << bitsPerChar);
        }
    }
}

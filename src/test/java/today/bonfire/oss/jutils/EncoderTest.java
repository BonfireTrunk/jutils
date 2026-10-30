package today.bonfire.oss.jutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import org.junit.jupiter.api.Test;

class EncoderTest {

    private static byte[] fixed(int n) {
        var b = new byte[n];
        for (int i = 0; i < n; i++) b[i] = (byte) (i * 37 + 200);
        return b;
    }

    @Test
    void urlEncodeAndDecode() {
        assertThat(Encoder.urlEncode(null)).isNull();
        assertThat(Encoder.urlEncode("")).isEmpty();
        assertThat(Encoder.urlEncode("a b&c=é")).isEqualTo("a+b%26c%3D%C3%A9");
        assertThat(Encoder.urlDecode(null)).isNull();
        assertThat(Encoder.urlDecode("")).isEmpty();
        assertThat(Encoder.urlDecode("a+b%26c%3D%C3%A9")).isEqualTo("a b&c=é");
    }

    @Test
    void urlEncodeBlankIsEncodedNotReturnedAsIs() {
        assertThat(Encoder.urlEncode(" ")).isEqualTo("+");
        assertThat(Encoder.urlDecode("+")).isEqualTo(" ");
        assertThat(Encoder.urlDecode("%20")).isEqualTo(" ");
    }

    @Test
    void urlDecodeMalformedThrows() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> Encoder.urlDecode("%zz"));
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> Encoder.urlDecode("%"));
    }

    @Test
    void escapeHtml() {
        assertThat(Encoder.escapeHtml(null)).isNull();
        assertThat(Encoder.escapeHtml("")).isEmpty();
        assertThat(Encoder.escapeHtml("plain")).isEqualTo("plain");
        assertThat(Encoder.escapeHtml("& < > \" '")).isEqualTo("&amp; &lt; &gt; &quot; &#x27;");
        assertThat(Encoder.escapeHtml("<img src=x onerror=alert(1)>")).isEqualTo("&lt;img src=x onerror=alert(1)&gt;");
    }

    @Test
    void escapeHtmlDoesNotDecodeEntities() {
        assertThat(Encoder.escapeHtml("&lt;b&gt;")).isEqualTo("&amp;lt;b&amp;gt;");
    }

    @Test
    void escapeHtmlReturnsUnchangedInputInstance() {
        var plain = new String("Plain text with spaces, Unicode é, and digits 12345.");
        assertThat(Encoder.escapeHtml(plain)).isSameAs(plain);
        assertThat(Encoder.escapeHtml("")).isSameAs("");
    }

    @Test
    void escapeHtmlPreservesPrefixBeforeEachEscapedCharacter() {
        assertThat(Encoder.escapeHtml("prefix & suffix")).isEqualTo("prefix &amp; suffix");
        assertThat(Encoder.escapeHtml("prefix < suffix")).isEqualTo("prefix &lt; suffix");
        assertThat(Encoder.escapeHtml("prefix > suffix")).isEqualTo("prefix &gt; suffix");
        assertThat(Encoder.escapeHtml("prefix \" suffix")).isEqualTo("prefix &quot; suffix");
        assertThat(Encoder.escapeHtml("prefix ' suffix")).isEqualTo("prefix &#x27; suffix");
    }

    @Test
    void htmlToTextStripsTagsAndDecodesEntities() {
        assertThat(Encoder.htmlToText(null)).isNull();
        assertThat(Encoder.htmlToText("")).isEmpty();
        assertThat(Encoder.htmlToText("<p>Hello <b>world</b></p>")).isEqualTo("Hello world");
        assertThat(Encoder.htmlToText("&lt;img src=x onerror=alert(1)&gt;")).isEqualTo("<img src=x onerror=alert(1)>");
    }

    @Test
    void htmlToTextSeparatesBlocksAndLineBreaks() {
        assertThat(Encoder.htmlToText("<p>Hello</p><p>World</p>")).isEqualTo("Hello World");
        assertThat(Encoder.htmlToText("line1<br>line2")).isEqualTo("line1 line2");
        assertThat(Encoder.htmlToText("<ul><li>a</li><li>b</li></ul>")).isEqualTo("a b");
    }

    @Test
    void htmlToTextExcludesHeadAndScriptContent() {
        assertThat(Encoder.htmlToText("<html><head><title>Title</title></head><body>Body</body></html>"))
                .isEqualTo("Body");
        assertThat(Encoder.htmlToText("<title>Title</title>")).isEmpty();
        assertThat(Encoder.htmlToText("<p>Hello</p><script>alert('hidden')</script><p>World</p>"))
                .isEqualTo("Hello World");
    }

    @Test
    void base64Url() {
        assertThat(Encoder.toBase64Url((byte[]) null)).isNull();
        assertThat(Encoder.toBase64Url((String) null)).isNull();
        assertThat(Encoder.toBase64Url(new byte[0])).isEmpty();
        assertThat(Encoder.toBase64Url("")).isEmpty();
        assertThat(Encoder.toBase64Url(new byte[] {(byte) 0xFB, (byte) 0xFF})).isEqualTo("-_8");
        assertThat(Encoder.toBase64Url("f")).isEqualTo("Zg");
        assertThat(Encoder.toBase64Url("é")).isEqualTo("w6k");
    }

    @Test
    void fromBase64Url() {
        assertThat(Encoder.fromBase64Url(null)).isNull();
        assertThat(Encoder.fromBase64Url("")).isEmpty();
        assertThat(Encoder.fromBase64Url("Zg")).isEqualTo(new byte[] {'f'});
        assertThat(Encoder.fromBase64Url("Zg==")).isEqualTo(new byte[] {'f'});
        assertThat(Encoder.fromBase64Url("  Zg \n")).isEqualTo(new byte[] {'f'});
        assertThat(Encoder.fromBase64Url("-_8")).isEqualTo(new byte[] {(byte) 0xFB, (byte) 0xFF});
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> Encoder.fromBase64Url("+/8"));
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> Encoder.fromBase64Url("Z"));
    }

    @Test
    void base64Basic() {
        assertThat(Encoder.toBase64Basic((String) null)).isNull();
        assertThat(Encoder.toBase64Basic((byte[]) null)).isNull();
        assertThat(Encoder.toBase64Basic("")).isEmpty();
        assertThat(Encoder.toBase64Basic("f")).isEqualTo("Zg==");
        assertThat(Encoder.toBase64Basic(new byte[] {(byte) 0xFB, (byte) 0xFF})).isEqualTo("+/8=");
        assertThat(Encoder.fromBase64Basic(null)).isNull();
        assertThat(Encoder.fromBase64Basic("")).isEmpty();
        assertThat(Encoder.fromBase64Basic("+/8=")).isEqualTo(new byte[] {(byte) 0xFB, (byte) 0xFF});
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> Encoder.fromBase64Basic("-_8="));
    }

    @Test
    void base64Mime() {
        assertThat(Encoder.toBase64Mime((byte[]) null)).isNull();
        assertThat(Encoder.toBase64Mime((String) null)).isNull();
        assertThat(Encoder.toBase64Mime("")).isEmpty();
        var data = new byte[100];
        var encoded = Encoder.toBase64Mime(data);
        assertThat(encoded).contains("\r\n");
        assertThat(encoded.split("\r\n")[0]).hasSize(76);
        assertThat(Encoder.fromBase64Mime(encoded)).isEqualTo(data);
        assertThat(Encoder.toBase64Mime("f")).isEqualTo("Zg==");
        assertThat(Encoder.fromBase64Mime(null)).isNull();
        assertThat(Encoder.fromBase64Mime("")).isEmpty();
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> Encoder.fromBase64Mime("Z"));
    }

    @Test
    void base32Rfc4648Vectors() {
        assertThat(Encoder.toBase32("")).isEmpty();
        assertThat(Encoder.toBase32("f")).isEqualTo("my");
        assertThat(Encoder.toBase32("fo")).isEqualTo("mzxq");
        assertThat(Encoder.toBase32("foo")).isEqualTo("mzxw6");
        assertThat(Encoder.toBase32("foob")).isEqualTo("mzxw6yq");
        assertThat(Encoder.toBase32("fooba")).isEqualTo("mzxw6ytb");
        assertThat(Encoder.toBase32("foobar")).isEqualTo("mzxw6ytboi");
        assertThat(Encoder.toBase32("foobar".getBytes(StandardCharsets.UTF_8))).isEqualTo("mzxw6ytboi");
        assertThat(Encoder.toBase32((String) null)).isNull();
        assertThat(Encoder.toBase32((byte[]) null)).isNull();
    }

    @Test
    void fromBase32() {
        assertThat(Encoder.fromBase32(null)).isNull();
        assertThat(Encoder.fromBase32("")).isEmpty();
        assertThat(Encoder.toStr(Encoder.fromBase32("mzxw6ytboi"))).isEqualTo("foobar");
        assertThat(Encoder.toStr(Encoder.fromBase32("MZXW6YTBOI"))).isEqualTo("foobar");
        assertThat(Encoder.toStr(Encoder.fromBase32("MZXW6YQ="))).isEqualTo("foob");
        assertThat(Encoder.toStr(Encoder.fromBase32("MY======"))).isEqualTo("f");
        assertThat(Encoder.toStr(Encoder.fromBase32(" \t\nMY======\n\t "))).isEqualTo("f");
    }

    @Test
    void fromBase32RejectsInvalid() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> Encoder.fromBase32("mzxw1"));
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> Encoder.fromBase32("m=y"));
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> Encoder.fromBase32("mé"));
        for (int len : new int[] {1, 3, 6, 9, 11, 14}) {
            var s = "a".repeat(len);
            assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> Encoder.fromBase32(s));
        }
        for (int len : new int[] {2, 4, 5, 7, 8}) {
            assertThat(Encoder.fromBase32("a".repeat(len))).hasSize(len * 5 / 8);
        }
    }

    @Test
    void base32LexMatchesRecordedOutputOfPreviousImplementation() {
        assertThat(Encoder.toBase32Lex(new byte[0])).isEmpty();
        assertThat(Encoder.toBase32Lex(fixed(1))).isEqualTo("S0");
        assertThat(Encoder.toBase32Lex(fixed(2))).isEqualTo("S3PG");
        assertThat(Encoder.toBase32Lex(fixed(3))).isEqualTo("S3PH4");
        assertThat(Encoder.toBase32Lex(fixed(4))).isEqualTo("S3PH4DR");
        assertThat(Encoder.toBase32Lex(fixed(5))).isEqualTo("S3PH4DTW");
        assertThat(Encoder.toBase32Lex(fixed(6))).isEqualTo("S3PH4DTWG4");
        assertThat(Encoder.toBase32Lex(fixed(16))).isEqualTo("S3PH4DTWG6KCQW0N79FR9AEEYC");
        assertThat(Encoder.toBase32Lex(null)).isNull();
    }

    @Test
    void base32LexRoundTripAndCase() {
        for (int n = 0; n <= 20; n++) {
            var bytes = fixed(n);
            var encoded = Encoder.toBase32Lex(bytes);
            assertThat(encoded).hasSize((n * 8 + 4) / 5);
            assertThat(Encoder.fromBase32Lex(encoded)).isEqualTo(bytes);
            assertThat(Encoder.fromBase32Lex(encoded.toLowerCase())).isEqualTo(bytes);
        }
        assertThat(Encoder.fromBase32Lex(null)).isNull();
        assertThat(Encoder.fromBase32Lex("")).isEmpty();
    }

    @Test
    void fromBase32LexRejectsAliasesAndBadLengths() {
        for (var bad : new String[] {"II", "LL", "OO", "UU", "A=", "A B"}) {
            assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> Encoder.fromBase32Lex(bad));
        }
        for (int len : new int[] {1, 3, 6, 9}) {
            var s = "0".repeat(len);
            assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> Encoder.fromBase32Lex(s));
        }
    }

    @Test
    void base32LexIsSortable() {
        var random = new Random(42);
        for (int size : new int[] {1, 5, 8, 16}) {
            var list = new ArrayList<byte[]>();
            for (int i = 0; i < 200; i++) {
                var b = new byte[size];
                random.nextBytes(b);
                list.add(b);
            }
            list.sort(Arrays::compareUnsigned);
            var encoded = list.stream().map(Encoder::toBase32Lex).toList();
            assertThat(encoded).isSorted();
        }
    }

    @Test
    void base64LexKnownValues() {
        assertThat(Encoder.toBase64Lex(null)).isNull();
        assertThat(Encoder.toBase64Lex(new byte[0])).isEmpty();
        assertThat(Encoder.toBase64Lex(new byte[] {0})).isEqualTo("--");
        assertThat(Encoder.toBase64Lex(new byte[] {(byte) 0xFF})).isEqualTo("zk");
        assertThat(Encoder.toBase64Lex(new byte[] {0, 0, 0})).isEqualTo("----");
        assertThat(Encoder.toBase64Lex(new byte[] {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF}))
                .isEqualTo("zzzz");
    }

    @Test
    void base64LexRoundTripAndLength() {
        for (int n = 0; n <= 20; n++) {
            var bytes = fixed(n);
            var encoded = Encoder.toBase64Lex(bytes);
            assertThat(encoded).hasSize((n * 8 + 5) / 6);
            assertThat(Encoder.fromBase64Lex(encoded)).isEqualTo(bytes);
        }
        assertThat(Encoder.fromBase64Lex(null)).isNull();
        assertThat(Encoder.fromBase64Lex("")).isEmpty();
    }

    @Test
    void fromBase64LexRejectsInvalid() {
        for (int len : new int[] {1, 5, 9}) {
            var s = "-".repeat(len);
            assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> Encoder.fromBase64Lex(s));
        }
        for (var bad : new String[] {"+-", "/-", "=-", "--=-", "é-"}) {
            assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> Encoder.fromBase64Lex(bad));
        }
    }

    @Test
    void base64LexIsSortable() {
        var random = new Random(7);
        for (int size : new int[] {1, 2, 3, 7, 16}) {
            var list = new ArrayList<byte[]>();
            for (int i = 0; i < 200; i++) {
                var b = new byte[size];
                random.nextBytes(b);
                list.add(b);
            }
            list.sort(Arrays::compareUnsigned);
            var encoded = list.stream().map(Encoder::toBase64Lex).toList();
            assertThat(encoded).isSorted();
        }
    }

    @Test
    void base16() {
        assertThat(Encoder.toBase16(null)).isNull();
        assertThat(Encoder.toBase16(new byte[0])).isEmpty();
        assertThat(Encoder.toBase16(new byte[] {0, 15, (byte) 0xAB, (byte) 0xFF}))
                .isEqualTo("000fabff");
        assertThat(Encoder.fromBase16(null)).isNull();
        assertThat(Encoder.fromBase16("")).isEmpty();
        assertThat(Encoder.fromBase16("000fABff")).isEqualTo(new byte[] {0, 15, (byte) 0xAB, (byte) 0xFF});
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> Encoder.fromBase16("abc"));
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> Encoder.fromBase16("zz"));
    }

    @Test
    void decodersIgnoreSurroundingWhitespace() {
        for (var whitespace : new String[] {" ", "\t", "\n", "\u2003", " \t\n"}) {
            assertThat(Encoder.fromBase64Url(whitespace + "AQ==" + whitespace)).isEqualTo(new byte[] {1});
            assertThat(Encoder.fromBase64Basic(whitespace + "AQ==" + whitespace))
                    .isEqualTo(new byte[] {1});
            assertThat(Encoder.fromBase32(whitespace + "MY======" + whitespace)).isEqualTo(new byte[] {'f'});
            assertThat(Encoder.fromBase32Lex(whitespace + "S0" + whitespace)).isEqualTo(fixed(1));
            assertThat(Encoder.fromBase64Lex(whitespace + "zk" + whitespace)).isEqualTo(new byte[] {(byte) 0xFF});
            assertThat(Encoder.fromBase16(whitespace + "ab" + whitespace)).isEqualTo(new byte[] {(byte) 0xAB});
            assertThat(Encoder.fromBase64Url(whitespace)).isEmpty();
            assertThat(Encoder.fromBase64Basic(whitespace)).isEmpty();
            assertThat(Encoder.fromBase32(whitespace)).isEmpty();
            assertThat(Encoder.fromBase32Lex(whitespace)).isEmpty();
            assertThat(Encoder.fromBase64Lex(whitespace)).isEmpty();
            assertThat(Encoder.fromBase16(whitespace)).isEmpty();
        }
    }

    @Test
    void decodersRejectInnerWhitespace() {
        for (var whitespace : new String[] {" ", "\t", "\n", "\u2003"}) {
            assertThatExceptionOfType(IllegalArgumentException.class)
                    .isThrownBy(() -> Encoder.fromBase64Url("A" + whitespace + "Q=="));
            assertThatExceptionOfType(IllegalArgumentException.class)
                    .isThrownBy(() -> Encoder.fromBase64Basic("A" + whitespace + "Q=="));
            assertThatExceptionOfType(IllegalArgumentException.class)
                    .isThrownBy(() -> Encoder.fromBase32("M" + whitespace + "Y======"));
            assertThatExceptionOfType(IllegalArgumentException.class)
                    .isThrownBy(() -> Encoder.fromBase32Lex("S" + whitespace + "0"));
            assertThatExceptionOfType(IllegalArgumentException.class)
                    .isThrownBy(() -> Encoder.fromBase64Lex("z" + whitespace + "k"));
            assertThatExceptionOfType(IllegalArgumentException.class)
                    .isThrownBy(() -> Encoder.fromBase16("a" + whitespace + "b"));
        }
    }

    @Test
    void toByteArrayAndToStr() {
        assertThat(Encoder.toByteArray("é")).isEqualTo(new byte[] {(byte) 0xC3, (byte) 0xA9});
        assertThat(Encoder.toByteArray("")).isEmpty();
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> Encoder.toByteArray(null));
        assertThat(Encoder.toStr(null)).isNull();
        assertThat(Encoder.toStr(new byte[0])).isEmpty();
        assertThat(Encoder.toStr(new byte[] {(byte) 0xC3, (byte) 0xA9})).isEqualTo("é");
    }

    @Test
    void oldStringDecodersReplacement() {
        assertThat(Encoder.toStr(Encoder.fromBase64Url(Encoder.toBase64Url("hello é"))))
                .isEqualTo("hello é");
        assertThat(Encoder.toStr(Encoder.fromBase64Mime(Encoder.toBase64Mime("hello"))))
                .isEqualTo("hello");
    }
}

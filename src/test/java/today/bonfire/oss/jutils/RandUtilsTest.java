package today.bonfire.oss.jutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

@Slf4j
public class RandUtilsTest {

    @BeforeAll
    public static void setUp() {
        MDC.put("uid", RandUtils.tuid());
    }

    @Test
    public void testTuidnegative() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> RandUtils.tuid(-1));
    }

    @Test
    public void testTuidZeroSizeRejected() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> RandUtils.tuid(0));
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> RandUtils.tuid(0, true));
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> RandUtils.tuid(0, false));
    }

    @Test
    public void testTuidLength() {
        String tuid = RandUtils.tuid(12, true);
        assertThat(tuid.length()).isEqualTo(29);
    }

    @Test
    public void testTuidLengthFormulas() {
        assertThat(RandUtils.tuid()).hasSize(26);
        for (int size = 1; size <= 20; size++) {
            assertThat(RandUtils.tuid(size, true)).hasSize((int) Math.ceil((6 + size) * 8 / 5.0));
            assertThat(RandUtils.tuid(size, false)).hasSize((int) Math.ceil((6 + size) * 8 / 6.0));
            assertThat(RandUtils.tuid(size)).hasSize((int) Math.ceil((6 + size) * 8 / 6.0));
        }
    }

    @Test
    public void testTuidAlphabets() {
        assertThat(RandUtils.tuid(10, true)).matches("[0-9A-HJKMNP-TV-Z]+");
        assertThat(RandUtils.tuid(10, false)).matches("[-0-9A-Z_a-z]+");
    }

    @Test
    public void testTuidSortableAcrossMilliseconds() throws InterruptedException {
        var firstBase32 = RandUtils.tuid(10, true);
        var firstBase64 = RandUtils.tuid(10, false);
        Thread.sleep(5);
        var secondBase32 = RandUtils.tuid(10, true);
        var secondBase64 = RandUtils.tuid(10, false);
        assertThat(firstBase32).isLessThan(secondBase32);
        assertThat(firstBase64).isLessThan(secondBase64);
    }

    @Test
    public void testTuidTimestampRoundTrip() {
        var before = Instant.now().minusMillis(1);
        var base32 = RandUtils.tuidTimestamp(RandUtils.tuid(), true);
        var base64 = RandUtils.tuidTimestamp(RandUtils.tuid(5, false), false);
        var after = Instant.now().plusMillis(1);
        assertThat(base32).isBetween(before, after);
        assertThat(base64).isBetween(before, after);
    }

    @Test
    public void testTuidTimestampInvalidInput() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> RandUtils.tuidTimestamp("not valid!", true));
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> RandUtils.tuidTimestamp("not valid!", false));
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> RandUtils.tuidTimestamp("", true));
        // 6 bytes only: a timestamp with no random part is not a tuid
        var sixBytes = Encoder.toBase32Lex(new byte[6]);
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> RandUtils.tuidTimestamp(sixBytes, true));
    }

    @Test
    public void testTuidUnique() {
        var set = new HashSet<String>();
        for (int i = 0; i < 100000; i++) {
            var t = RandUtils.tuid();
            assertThat(set.contains(t)).isFalse();
            set.add(t);
        }
        assertThat(set.size()).isEqualTo(100000);
    }

    @Test
    public void testTime() {
        var t = RandUtils.time();
        assertThat(t).hasSize(10).matches("[0-9A-HJKMNP-TV-Z]+");
        var millis = Encoder.fromBase32Lex(t);
        assertThat(millis).hasSize(6);
    }

    @Test
    public void testTimeSortable() throws InterruptedException {
        var first = RandUtils.time();
        Thread.sleep(5);
        assertThat(first).isLessThan(RandUtils.time());
    }

    @Test
    public void testNewIntNegativeRange() {
        for (int i = 0; i < 50; i++) {
            assertThat(RandUtils.newInt(-2, -1)).isEqualTo(-2);
            assertThat(RandUtils.newInt(-5, 5)).isBetween(-5, 4);
        }
    }

    @Test
    public void testNewLongNegativeRange() {
        for (int i = 0; i < 50; i++) {
            assertThat(RandUtils.newLong(-2L, -1L)).isEqualTo(-2L);
            assertThat(RandUtils.newLong(-5L, 5L)).isBetween(-5L, 4L);
        }
    }

    @Test
    public void testNewIntAndLongInvalidBounds() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> RandUtils.newInt(1, 1));
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> RandUtils.newInt(2, 1));
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> RandUtils.newLong(1L, 1L));
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> RandUtils.newLong(2L, 1L));
    }

    @Test
    public void testGenerateNewToken() {
        assertThat(RandUtils.generateNewToken()).hasSize(48);
        assertThat(RandUtils.generateNewToken(1)).hasSize(2);
        assertThat(RandUtils.generateNewToken(2)).hasSize(3);
        assertThat(RandUtils.generateNewToken(12)).hasSize(16);
        assertThat(RandUtils.generateNewToken(36)).matches("[-A-Za-z0-9_]{48}");
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> RandUtils.generateNewToken(0));
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> RandUtils.generateNewToken(-1));
    }

    @Test
    public void testGenerateByteArray() {
        assertThat(RandUtils.generateByteArray(0)).isEmpty();
        assertThat(RandUtils.generateByteArray(7)).hasSize(7);
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> RandUtils.generateByteArray(-1))
                .withMessageContaining("must not be negative");
    }

    @Test
    public void testNanoIdVariants() {
        assertThat(RandUtils.nanoId(17)).matches("[-0-9A-Z_a-z]{17}");
        assertThat(RandUtils.nanoId62(40)).matches("[0-9A-Za-z]{40}");
        assertThat(RandUtils.nanoIdWordSafe(40)).matches("[23456789CFGHJMPQRVWXcfghjmpqrvwx]{40}");
        assertThat(RandUtils.nanoIdCaseInsensitive(40)).matches("[23456789abcdefghijklmnpqrstvwxyz]{40}");
        assertThat(RandUtils.nanoId("ab".toCharArray(), 30)).matches("[ab]{30}");
        assertThat(RandUtils.nanoId("x".toCharArray(), 5)).isEqualTo("xxxxx");
    }

    @Test
    public void testNanoIdValidation() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> RandUtils.nanoId(0));
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> RandUtils.nanoId62(-1));
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> RandUtils.nanoIdWordSafe(0));
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> RandUtils.nanoIdCaseInsensitive(0));
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> RandUtils.nanoId(new char[0], 5));
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> RandUtils.nanoId(null, 5));
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> RandUtils.nanoId("ab".toCharArray(), 0));
    }

    @Test
    public void testNanoIdReachesEveryCharacterOfLargeAlphabet() {
        // nanoIdFast could never pick characters at index >= 256; with 300 characters and 30000 samples each
        // character is expected about 100 times.
        var alphabet = new char[300];
        for (int i = 0; i < alphabet.length; i++) alphabet[i] = (char) (0x100 + i);
        var seen = new HashSet<Character>();
        for (char c : RandUtils.nanoId(alphabet, 30000).toCharArray()) seen.add(c);
        assertThat(seen).hasSize(300);
    }

    @Test
    public void testNanoIdDistribution62() {
        int samples = 62 * 2000;
        var counts = new HashMap<Character, Integer>();
        for (char c : RandUtils.nanoId62(samples).toCharArray()) counts.merge(c, 1, Integer::sum);
        assertThat(counts).hasSize(62);
        // expected 2000 each, standard deviation about 44; the bound is more than 10 sigma wide
        assertThat(counts.values()).allSatisfy(n -> assertThat(n).isBetween(1500, 2500));
    }

    @Test
    public void testNanoIdDistributionNonPowerOfTwo() {
        for (int length : new int[] {3, 37, 100, 255}) {
            var alphabet = new char[length];
            for (int i = 0; i < length; i++) alphabet[i] = (char) (0x100 + i);
            int samples = length * 2000;
            var id = RandUtils.nanoId(alphabet, samples);
            assertThat(id).hasSize(samples);
            var counts = new HashMap<Character, Integer>();
            for (char c : id.toCharArray()) counts.merge(c, 1, Integer::sum);
            assertThat(counts).hasSize(length);
            for (char c : alphabet) assertThat(counts.get(c)).isBetween(1500, 2500);
        }
    }

    @Test
    public void testNanoIdNonPowerOfTwoSizes() {
        for (int length : new int[] {3, 37, 100, 255}) {
            var alphabet = new char[length];
            for (int i = 0; i < length; i++) alphabet[i] = (char) (0x100 + i);
            for (int size : new int[] {1, 2, 1000}) {
                assertThat(RandUtils.nanoId(alphabet, size)).hasSize(size);
            }
        }
    }

    @Test
    public void testNanoIdDistributionPowerOfTwo() {
        int samples = 64 * 2000;
        var counts = new HashMap<Character, Integer>();
        for (char c : RandUtils.nanoId(samples).toCharArray()) counts.merge(c, 1, Integer::sum);
        assertThat(counts).hasSize(64);
        assertThat(counts.values()).allSatisfy(n -> assertThat(n).isBetween(1500, 2500));
    }

    @Test
    public void testGenBucketName() {
        for (int i = 0; i < 20; i++) {
            assertThat(RandUtils.genBucketName()).matches("[a-z2-7]{32}");
        }
    }

    @Test
    public void testGetUrlPath() {
        for (int i = 0; i < 20; i++) {
            assertThat(RandUtils.getUrlPath()).matches("[0-9A-Za-z]{44}");
        }
    }

    @Test
    public void testDiceRolls() {
        assertThat(RandUtils.diceRolls(30)).matches("[1-6]{30}");
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> RandUtils.diceRolls(0));
    }

    @Test
    public void testPassphrase() {
        var p = RandUtils.passphrase(6);
        assertThat(p.split("-", -1)).hasSize(6).allSatisfy(w -> assertThat(w).isNotEmpty());
        assertThat(RandUtils.passphrase(1)).doesNotContain("-" + "-");
    }

    @Test
    public void testPassphraseSeparator() {
        assertThat(RandUtils.passphrase(4, " ").split(" ", -1)).hasSize(4);
        assertThat(RandUtils.passphrase(3, "")).isNotEmpty().doesNotContain(" ");
        assertThat(RandUtils.passphrase(1, "::")).doesNotContain("::");
    }

    @Test
    public void testPassphraseWordsComeFromList() throws IOException {
        var list = new HashSet<>(bundledWords());
        var words = RandUtils.passphrase(200).split("-");
        assertThat(words).hasSize(200).allSatisfy(w -> assertThat(list).contains(w));
    }

    @Test
    public void testPassphraseWordList() throws IOException {
        var words = bundledWords();
        assertThat(words)
                .hasSize(8192)
                .doesNotHaveDuplicates()
                .isSorted()
                .allSatisfy(w -> assertThat(w).matches("^[a-z]{3,9}$"));
        // In a sorted list, a word that is a prefix of any later word is a prefix of the next one.
        for (int i = 0; i < words.size() - 1; i++) {
            assertThat(words.get(i + 1)).doesNotStartWith(words.get(i));
        }
    }

    private static List<String> bundledWords() throws IOException {
        try (var in = RandUtils.class.getResourceAsStream("passphrase_wordlist.txt")) {
            assertThat(in).isNotNull();
            return new String(in.readAllBytes(), StandardCharsets.UTF_8).lines().toList();
        }
    }

    @Test
    public void testPassphraseInvalidWordCount() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> RandUtils.passphrase(0));
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> RandUtils.passphrase(-1, "-"));
    }
}

package today.bonfire.oss.jutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import today.bonfire.oss.jutils.constants.HashAlgorithm;
import today.bonfire.oss.jutils.constants.HmacAlgorithm;

class DigestTest {

    private static final String SHA256_ABC = "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad";
    private static final String SHA3_256_ABC = "3a985da74fe225b2045c172d6bd390bd855f086e3e9d525b46bfe24511431532";
    private static final String DATA = "what do ya want for nothing?";
    private static final String HMAC256 = "5bdcc146bf60754e6a042426089575c75a003f089d2739839dec58b964ec3843";
    private static final String HMAC384 =
            "af45d2e376484031617f78d2b58a6b1b9c7ef464f5a01b47e42ec3736322445e8e2240ca5e69e2c78b3239ecfab21649";
    private static final String HMAC512 =
            "164b7a7bfcf819e2e395fbe73b56e0a387bd64222e831fd610270cd7ea2505549758bf75c05a994a6d034f65f8f0e6fd"
                    + "caeab1a34d4a6b4b636e070a38bce737";

    @Test
    void hashHexKnownVectors() {
        assertThat(Digest.hashHex(HashAlgorithm.SHA_256, "abc")).isEqualTo(SHA256_ABC);
        assertThat(Digest.hashHex(HashAlgorithm.SHA3_256, "abc")).isEqualTo(SHA3_256_ABC);
    }

    @Test
    void hashLengthsPerAlgorithm() {
        assertThat(Digest.hash(HashAlgorithm.SHA_256, "abc")).hasSize(32);
        assertThat(Digest.hash(HashAlgorithm.SHA_384, "abc")).hasSize(48);
        assertThat(Digest.hash(HashAlgorithm.SHA_512, "abc")).hasSize(64);
        assertThat(Digest.hash(HashAlgorithm.SHA3_256, "abc")).hasSize(32);
        assertThat(Digest.hash(HashAlgorithm.SHA3_384, "abc")).hasSize(48);
        assertThat(Digest.hash(HashAlgorithm.SHA3_512, "abc")).hasSize(64);
    }

    @Test
    void hashBytesAndStringAgree() {
        assertThat(Digest.hash(HashAlgorithm.SHA_256, "abc"))
                .isEqualTo(Digest.hash(HashAlgorithm.SHA_256, "abc".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void hashEmptyInput() {
        assertThat(Digest.hashHex(HashAlgorithm.SHA_256, ""))
                .isEqualTo("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
    }

    @Test
    void hashUsesUtf8() {
        assertThat(Digest.hash(HashAlgorithm.SHA_256, "é"))
                .isEqualTo(Digest.hash(HashAlgorithm.SHA_256, new byte[] {(byte) 0xC3, (byte) 0xA9}));
    }

    @Test
    void hashBase64UrlAndBase32() {
        assertThat(Digest.hashBase64Url(HashAlgorithm.SHA3_256, "abc"))
                .isEqualTo("Ophdp0_iJbIEXBcta9OQvYVfCG4-nVJbRr_iRRFDFTI");
        assertThat(Digest.hashBase32(HashAlgorithm.SHA3_256, "abc"))
                .isEqualTo("hkmf3j2p4is3ebc4c4wwxu4qxwcv6cdoh2ovew2gx7rekekdcuza");
    }

    @Test
    void hashNullArgumentsThrow() {
        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> Digest.hash(HashAlgorithm.SHA_256, (String) null));
        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> Digest.hash(HashAlgorithm.SHA_256, (byte[]) null));
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> Digest.hash(null, "abc"));
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> Digest.hashHex(null, "abc"));
        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> Digest.hashBase64Url(HashAlgorithm.SHA_256, null));
        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> Digest.hashBase32(HashAlgorithm.SHA_256, null));
    }

    @Test
    void hmacRfc4231Case2() {
        assertThat(Digest.hmacHex(HmacAlgorithm.HMAC_SHA_256, DATA, "Jefe")).isEqualTo(HMAC256);
        assertThat(Digest.hmacHex(HmacAlgorithm.HMAC_SHA_384, DATA, "Jefe")).isEqualTo(HMAC384);
        assertThat(Digest.hmacHex(HmacAlgorithm.HMAC_SHA_512, DATA, "Jefe")).isEqualTo(HMAC512);
    }

    @Test
    void hmacHexIsLowercase() {
        assertThat(Digest.hmacHex(HmacAlgorithm.HMAC_SHA_256, DATA, "Jefe")).isEqualTo(HMAC256.toLowerCase());
    }

    @Test
    void hmacBytesAndStringAgree() {
        assertThat(Digest.hmac(HmacAlgorithm.HMAC_SHA_256, DATA, "Jefe"))
                .isEqualTo(Digest.hmac(
                        HmacAlgorithm.HMAC_SHA_256,
                        DATA.getBytes(StandardCharsets.UTF_8),
                        "Jefe".getBytes(StandardCharsets.UTF_8)))
                .hasSize(32);
    }

    @Test
    void hmacBase64Url() {
        assertThat(Digest.hmacBase64Url(HmacAlgorithm.HMAC_SHA_256, DATA, "Jefe"))
                .isEqualTo("W9zBRr9gdU5qBCQmCJV1x1oAPwidJzmDnexYuWTsOEM");
    }

    @Test
    void hmacEmptyKeyThrowsIllegalArgument() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> Digest.hmac(HmacAlgorithm.HMAC_SHA_256, DATA, ""));
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> Digest.hmac(HmacAlgorithm.HMAC_SHA_256, new byte[1], new byte[0]));
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> Digest.hmacHex(HmacAlgorithm.HMAC_SHA_256, DATA, ""));
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> Digest.hmacBase64Url(HmacAlgorithm.HMAC_SHA_256, DATA, ""));
    }

    @Test
    void hmacNullArgumentsThrow() {
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> Digest.hmac(null, DATA, "Jefe"));
        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> Digest.hmac(HmacAlgorithm.HMAC_SHA_256, (String) null, "Jefe"));
        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> Digest.hmac(HmacAlgorithm.HMAC_SHA_256, DATA, (String) null));
        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> Digest.hmac(HmacAlgorithm.HMAC_SHA_256, (byte[]) null, new byte[1]));
        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> Digest.hmac(HmacAlgorithm.HMAC_SHA_256, new byte[1], (byte[]) null));
    }

    @Test
    void isEqualCases() {
        assertThat(Digest.isEqual(new byte[] {1, 2}, new byte[] {1, 2})).isTrue();
        assertThat(Digest.isEqual(new byte[] {1, 2}, new byte[] {1, 3})).isFalse();
        assertThat(Digest.isEqual(new byte[] {1, 2}, new byte[] {1})).isFalse();
        assertThat(Digest.isEqual(new byte[0], new byte[0])).isTrue();
        assertThat(Digest.isEqual(null, null)).isTrue();
        assertThat(Digest.isEqual(null, new byte[0])).isFalse();
        assertThat(Digest.isEqual(new byte[0], null)).isFalse();
    }

    @Test
    void algorithmEnumsExposeJcaNames() {
        assertThat(HashAlgorithm.SHA3_512.jcaName()).isEqualTo("SHA3-512");
        assertThat(HashAlgorithm.SHA_256).hasToString("SHA-256");
        assertThat(HmacAlgorithm.HMAC_SHA_384.jcaName()).isEqualTo("HmacSHA384");
        assertThat(HmacAlgorithm.HMAC_SHA_512).hasToString("HmacSHA512");
    }
}

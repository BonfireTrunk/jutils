package today.bonfire.oss.jutils.constants;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class CountryCodeTest {

    @ParameterizedTest
    @EnumSource(CountryCode.class)
    void everyRegexCompilesAndIsAnchored(CountryCode country) {
        String regex = country.postCodeRegex();
        if (regex.isEmpty()) {
            return;
        }
        assertThat(regex).startsWith("^").endsWith("$");
        assertThat(Pattern.compile(regex)).isNotNull();
    }

    @ParameterizedTest
    @CsvSource({
        "AD, AD123",
        "AE, 12345 67891",
        "AI, AI-1234",
        "AL, 1234",
        "AM, 1234",
        "AR, A1234AAA",
        "AS, 96799-1234",
        "AT, 1234",
        "AU, 1234",
        "AX, 12345",
        "AZ, AZ 1234",
        "BA, 12345",
        "BB, BB12345",
        "BD, 1234",
        "BE, 1234",
        "BG, 1234",
        "BH, 1234",
        "BL, 12345",
        "BM, AA 12",
        "BN, AA1234",
        "BR, 12345-678",
        "BT, 12345",
        "BY, 123456",
        "CA, A1A 2A3",
        "CC, 1234",
        "CD, 1234567",
        "CH, 1234",
        "CL, 1234567",
        "CN, 123456",
        "CO, 123456",
        "CR, 12345",
        "CU, CP 12345",
        "CV, 1234",
        "CX, 1234",
        "CY, 1234",
        "CZ, 123 45",
        "DE, 12345",
        "DJ, 12345",
        "DK, 1234",
        "DO, 12345",
        "DZ, 12345",
        "EC, 123456",
        "EE, 12345",
        "EG, 12345",
        "EH, 12345",
        "ES, 12345",
        "ET, 1234",
        "FI, 12345",
        "FK, FIQQ 1ZZ",
        "FM, 12345",
        "FO, 123",
        "FR, 12345",
        "GA, 12",
        "GB, A1 2AA",
        "GE, 1234",
        "GF, 97300",
        "GG, GY1 2AB",
        "GH, AA-123-4567",
        "GI, GX11 1AA",
        "GL, 1234",
        "GN, 123",
        "GP, 97100",
        "GR, 123 45",
        "GS, SIQQ 1ZZ",
        "GT, 12345",
        "GU, 96912",
        "GW, 1234",
        "GY, 1234567",
        "HK, 123456",
        "HM, 1234",
        "HN, 12345",
        "HR, 12345",
        "HT, HT1234",
        "HU, 1234",
        "ID, 12345",
        "IE, A65 F4E2",
        "IL, 1234567",
        "IM, IM1 2AB",
        "IN, 123456",
        "IO, BBND 1ZZ",
        "IQ, 12345",
        "IR, 1234567891",
        "IS, 123",
        "IT, 12345",
        "JE, JE2 3AB",
        "JO, 12345",
        "JP, 123-4567",
        "KE, 12345",
        "KG, 123456",
        "KH, 123456",
        "KI, AA1234",
        "KN, KN1234",
        "KP, 123-456",
        "KR, 12345",
        "XK, 12345",
        "KW, 12345",
        "KY, KY1-2345",
        "KZ, 123456",
        "LA, 12345",
        "LB, 1234 5678",
        "LC, LC123-456",
        "LI, 9412",
        "LK, 12345",
        "LR, 1234",
        "LS, 123",
        "LT, LT-12345",
        "LU, L-1234",
        "LV, LV-1234",
        "MA, 12345",
        "MC, 12345",
        "MD, MD-1234",
        "ME, 12345",
        "MF, 12345",
        "MG, 123",
        "MH, 96960-1234",
        "MK, 1234",
        "MM, 12345",
        "MN, 12345",
        "MO, 123456",
        "MP, 96950",
        "MQ, 12345",
        "MS, MSR1234",
        "MT, AAA 1234",
        "MU, 12345",
        "MV, 12345",
        "MW, 123456",
        "MX, 12345",
        "MY, 12345",
        "MZ, 1234",
        "NA, 12345",
        "NC, 12345",
        "NE, 1234",
        "NF, 1234",
        "NG, 123456",
        "NI, 12345",
        "NL, 1234 AA",
        "NO, 1234",
        "NP, 12345",
        "NR, NRU68",
        "NU, 1234",
        "NZ, 1234",
        "OM, 123",
        "PA, 12345",
        "PE, 12345",
        "PF, 98700",
        "PG, 123",
        "PH, 1234",
        "PK, 12345",
        "PL, 12-345",
        "PM, 97500",
        "PN, PCRN 1ZZ",
        "PR, 00901-1234",
        "PS, P1234567",
        "PT, 1234-567",
        "PW, 96940",
        "PY, 123456",
        "RE, 97400",
        "RO, 123456",
        "RS, 12345",
        "RU, 123456",
        "SA, 12345",
        "SD, 12345",
        "SE, 123 45",
        "SG, 123456",
        "SH, STHL 1ZZ",
        "SI, 1234",
        "SJ, 1234",
        "SK, 123 45",
        "SM, 47891",
        "SN, 12345",
        "SO, AA  12345",
        "SV, CP 1234",
        "SZ, A123",
        "TC, TKCA 1ZZ",
        "TF, 12345",
        "TH, 12345",
        "TJ, 123456",
        "TL, TL12345",
        "TM, 123456",
        "TN, 1234",
        "TR, 12345",
        "TT, 123456",
        "TW, 12345",
        "TZ, 12345",
        "UA, 12345",
        "UG, 12345",
        "US, 12345-6789",
        "UY, 12345",
        "UZ, 123456",
        "VA, 12345",
        "VC, VC1234",
        "VE, 1234",
        "VG, VG1234",
        "VI, 00802-1234",
        "VN, 123456",
        "WF, 98600",
        "YT, 12345",
        "ZA, 1234",
        "ZM, 12345",
        "CS, 12345"
    })
    void acceptsSampleCodeDerivedFromGeoNamesFormat(CountryCode country, String postCode) {
        assertThat(country.hasPostCodes()).isTrue();
        assertThat(country.isValidPostCode(postCode)).isTrue();
    }

    @ParameterizedTest
    @CsvSource({
        "AD, AD, 100",
        "AI, AI-, 1234",
        "AX, FI, 12345",
        "AZ, 'AZ ', 1234",
        "BB, BB, 12345",
        "CY, CY, 1234",
        "CY, CY-, 1234",
        "FI, FI, 12345",
        "FO, FO, 123",
        "HR, HR, 12345",
        "HT, HT, 1234",
        "KN, KN, 1234",
        "MS, MSR, 1234",
        "SI, SI, 1234",
        "TL, TL, 12345",
        "VC, VC, 1234",
        "VG, VG, 1234"
    })
    void optionalCountryPrefixAppearsAtMostOnce(CountryCode country, String prefix, String postCode) {
        assertThat(country.isValidPostCode(prefix + postCode)).isTrue();
        assertThat(country.isValidPostCode(prefix + prefix + postCode)).isFalse();
        assertThat(country.isValidPostCode(postCode)).isTrue();
    }

    @Test
    void everyCountryWithARegexHasASampleCode() {
        long withRegex = Arrays.stream(CountryCode.values())
                .filter(c -> !c.postCodeRegex().isEmpty())
                .count();
        assertThat(withRegex).isEqualTo(200);
    }

    @ParameterizedTest
    @CsvSource({
        "US, 1234",
        "US, ABCDE",
        "US, 12345-67",
        "CA, D1A 0B1",
        "CA, K1A0B",
        "GB, 12345",
        "GB, SW1A 1A",
        "IN, 12345",
        "DE, 1234",
        "NL, 12345",
        "AS, 12345",
        "WS, 96799",
        "WS, AS 96799",
        "JE, SW1A 1AA",
        "GG, JE2 3AB",
        "IM, GY1 1AA"
    })
    void rejectsInvalidCodes(CountryCode country, String postCode) {
        assertThat(country.isValidPostCode(postCode)).isFalse();
    }

    @ParameterizedTest
    @EnumSource(
            value = CountryCode.class,
            names = {"AF", "AG", "AO", "AQ", "BF", "TD", "XX"})
    void countriesWithoutARegexHaveNoPostCodes(CountryCode country) {
        assertThat(country.postCodeRegex()).isEmpty();
        assertThat(country.hasPostCodes()).isFalse();
        assertThat(country.isValidPostCode("12345")).isFalse();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    void blankOrNullPostCodeIsInvalid(String postCode) {
        assertThat(CountryCode.US.isValidPostCode(postCode)).isFalse();
    }

    @ParameterizedTest
    @CsvSource({
        "CA, K1A 0B1",
        "CA, k1a 0b1",
        "CA, K1A0B1",
        "CA, '  k1a 0b1  '",
        "GB, SW1A 1AA",
        "GB, sw1a 1aa",
        "GB, M1 1AE",
        "GG, GY1 1AA",
        "US, 12345",
        "US, 12345-6789"
    })
    void regressions(CountryCode country, String postCode) {
        assertThat(country.isValidPostCode(postCode)).isTrue();
    }

    @Test
    void chadHasNoPostCodes() {
        assertThat(CountryCode.TD.hasPostCodes()).isFalse();
        assertThat(CountryCode.TD.isValidPostCode("TKCA 1ZZ")).isFalse();
    }

    @ParameterizedTest
    @CsvSource({"in, IN", "' US ', US", "us, US", "Gb, GB", "cs, CS"})
    void fromCodeFindsCountriesIgnoringCaseAndWhitespace(String code, CountryCode expected) {
        assertThat(CountryCode.fromCode(code)).contains(expected);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"zz", " ", "USA", "XX", "x"})
    void fromCodeIsEmptyForNullBlankAndUnknown(String code) {
        assertThat(CountryCode.fromCode(code)).isEmpty();
    }

    @Test
    void obsoleteCodesAreKeptButDeprecated() throws NoSuchFieldException {
        assertThat(CountryCode.class.getField("CS").isAnnotationPresent(Deprecated.class))
                .isTrue();
        assertThat(CountryCode.class.getField("AN").isAnnotationPresent(Deprecated.class))
                .isTrue();
    }
}

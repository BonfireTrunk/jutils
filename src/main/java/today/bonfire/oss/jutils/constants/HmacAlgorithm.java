package today.bonfire.oss.jutils.constants;

/**
 * HMAC algorithms accepted by {@link today.bonfire.oss.jutils.Digest}, named by their JCA standard algorithm name.
 */
public enum HmacAlgorithm {
    HMAC_SHA_256("HmacSHA256"),
    HMAC_SHA_384("HmacSHA384"),
    HMAC_SHA_512("HmacSHA512");

    private final String jcaName;

    HmacAlgorithm(String jcaName) {
        this.jcaName = jcaName;
    }

    /**
     * Returns the JCA standard name of this algorithm, as accepted by {@link javax.crypto.Mac#getInstance}.
     *
     * @return the JCA name, for example {@code HmacSHA256}; never null
     */
    public String jcaName() {
        return jcaName;
    }

    /**
     * Returns the JCA standard name of this algorithm.
     *
     * @return the same value as {@link #jcaName()}
     */
    @Override
    public String toString() {
        return jcaName;
    }
}

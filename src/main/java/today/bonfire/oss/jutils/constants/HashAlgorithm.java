package today.bonfire.oss.jutils.constants;

/**
 * Message digest algorithms accepted by {@link today.bonfire.oss.jutils.Digest}, named by their JCA standard
 * algorithm name.
 */
public enum HashAlgorithm {
    SHA_256("SHA-256"),
    SHA_384("SHA-384"),
    SHA_512("SHA-512"),
    SHA3_256("SHA3-256"),
    SHA3_384("SHA3-384"),
    SHA3_512("SHA3-512");

    private final String jcaName;

    HashAlgorithm(String jcaName) {
        this.jcaName = jcaName;
    }

    /**
     * Returns the JCA standard name of this algorithm, as accepted by {@link java.security.MessageDigest#getInstance}.
     *
     * @return the JCA name, for example {@code SHA3-256}; never null
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

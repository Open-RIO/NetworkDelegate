package jaci.openrio.delegate;

import java.security.MessageDigest;
import java.util.Arrays;

/**
 * A class responsible for Hashing passwords for the Delegate Server and Client. Keep in mind passwords hashed here are
 * NOT 100% SECURE. This is for simple identification purposes, making sure the client is who they say they are. This uses
 * the SHA and MD5 algorithms for hashing, and should not be used in large scale projects where security is the number 1
 * priority. Additionally, this class is vulnerable to 'reflection'. You shouldn't be using Java for things where security
 * is of the utmost importance.
 *
 * @author Jaci
 */
public class Security {

    public static class Password {
        HashType algorithm;
        byte[] hash;

        public Password(String pword_raw, HashType algorithm) {
            hash = algorithm.hash(pword_raw);
            this.algorithm = algorithm;
        }

        public boolean matches(byte[] compare) {
            return Arrays.equals(compare, hash);
        }
    }

    public static enum HashType {
        MD5("MD5"),
        SHA1("SHA-1"),
        SHA256("SHA-256");

        String id;
        MessageDigest digest;

        HashType(String name) {
            this.id = name;
            try {
                this.digest = MessageDigest.getInstance(id);
            } catch (Exception e) {
                System.err.println("Could not initialize digest: " + id);
            }
        }

        public String getHashID() {
            return id;
        }

        public static HashType match(String id) {
            for (HashType type : HashType.values()) {
                if (type.name().equals(id) || type.getHashID().equals(id))
                    return type;
            }
            return null;
        }

        public byte[] hash(String input) {
            digest.reset();
            byte[] s = digest.digest(input.getBytes());
            digest.reset();
            return s;
        }
    }

}

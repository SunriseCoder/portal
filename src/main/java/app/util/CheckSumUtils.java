package app.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class CheckSumUtils {

    public static boolean isChunksDigestsValid(byte[] wholeCheckSum) {
        try {
            int chunkDigestLength = wholeCheckSum.length - 16;
            byte[] chunkDigests = new byte[chunkDigestLength];
            System.arraycopy(wholeCheckSum, 0, chunkDigests, 0, chunkDigestLength);

            byte[] expectedFinalDigest = new byte[16];
            System.arraycopy(wholeCheckSum, wholeCheckSum.length - 16, expectedFinalDigest, 0, 16);

            boolean valid = isCheckSumValid(chunkDigests, expectedFinalDigest);
            return valid;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isCheckSumValid(byte[] input, byte[] expectedCheckSum) {
        try {
            MessageDigest digestCalculator = getCalculator();
            byte[] actualCheckSum = digestCalculator.digest(input);

            boolean equals = Arrays.equals(expectedCheckSum, actualCheckSum);
            return equals;
        } catch (Exception e) {
            return false;
        }
    }

    public static MessageDigest getCalculator() throws NoSuchAlgorithmException {
        MessageDigest calculator = MessageDigest.getInstance("MD5");
        return calculator;
    }
}

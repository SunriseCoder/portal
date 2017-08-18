package app.util;

import java.security.MessageDigest;
import java.util.Arrays;

public class CheckSumUtils {

    public static boolean isChunkCheckValid(byte[] wholeCheckSum) {
        try {
            int chunkDigestLength = wholeCheckSum.length - 16;
            byte[] chunkDigests = new byte[chunkDigestLength];
            System.arraycopy(wholeCheckSum, 0, chunkDigests, 0, chunkDigestLength);

            byte[] expectedFinalDigest = new byte[16];
            System.arraycopy(wholeCheckSum, wholeCheckSum.length - 16, expectedFinalDigest, 0, 16);

            MessageDigest digestCalculator = MessageDigest.getInstance("MD5");

            byte[] actualFinalDigest = digestCalculator.digest(chunkDigests);
            byte[] oneChunkDigest = new byte[16];
            System.arraycopy(wholeCheckSum, 0, oneChunkDigest, 0, 16);

            boolean equals = Arrays.equals(expectedFinalDigest, actualFinalDigest);
            return equals;
        } catch (Exception e) {
            return false;
        }
    }
}

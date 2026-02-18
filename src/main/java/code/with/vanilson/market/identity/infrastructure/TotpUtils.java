package code.with.vanilson.market.identity.infrastructure;

import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Hex;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

@Component
public class TotpUtils {

    public String generateSecretKey() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[20];
        random.nextBytes(bytes);
        Base32 base32 = new Base32();
        return base32.encodeToString(bytes);
    }

    public String getTOTPCode(String secretKey) {
        Base32 base32 = new Base32();
        byte[] bytes = base32.decode(secretKey);
        String hexKey = Hex.encodeHexString(bytes);
        long time = (System.currentTimeMillis() / 1000) / 30;
        String hexTime = Long.toHexString(time);
        while (hexTime.length() < 16) {
            hexTime = "0" + hexTime;
        }
        return getTOTP(hexKey, hexTime);
    }

    public boolean verifyCode(String secretKey, String code) {
        String generatedCode = getTOTPCode(secretKey);
        return generatedCode.equals(code);
    }

    private String getTOTP(String key, String time) {
        return totp(key, time, "6", "HmacSHA1");
    }

    // Simplified TOTP implementation reference from RFC 6238
    // In production, consider using a library like Google Auth or similar if robust
    // features needed
    // But for scaffold we implement the core HMAC SHA1 logic
    private String totp(String key, String time, String returnDigits, String crypto) {
        int codeDigits = Integer.decode(returnDigits);
        String result = null;

        // Using the counter
        // First 8 bytes are for the movingFactor
        // Compliant with base RFC 4226 (HOTP)
        while (time.length() < 16)
            time = "0" + time;

        // Get the HEX in a Byte[]
        byte[] msg = hexStr2Bytes(time);
        byte[] k = hexStr2Bytes(key);

        byte[] hash = hmac_sha(crypto, k, msg);

        // put selected bytes into result int
        int offset = hash[hash.length - 1] & 0xf;

        int binary = ((hash[offset] & 0x7f) << 24) |
                ((hash[offset + 1] & 0xff) << 16) |
                ((hash[offset + 2] & 0xff) << 8) |
                (hash[offset + 3] & 0xff);

        int otp = binary % ((int) Math.pow(10, codeDigits));

        result = Integer.toString(otp);
        while (result.length() < codeDigits) {
            result = "0" + result;
        }
        return result;
    }

    private byte[] hmac_sha(String crypto, byte[] keyBytes, byte[] text) {
        try {
            Mac hmac;
            hmac = Mac.getInstance(crypto);
            SecretKeySpec macKey = new SecretKeySpec(keyBytes, "RAW");
            hmac.init(macKey);
            return hmac.doFinal(text);
        } catch (Exception gse) {
            throw new RuntimeException("Problem calculating HMAC", gse);
        }
    }

    private byte[] hexStr2Bytes(String hex) {
        // Adding one byte to get the right conversion
        // Values starting with "0" can be converted
        byte[] bArray = new java.math.BigInteger("10" + hex, 16).toByteArray();

        // Copy all the REAL bytes, not the "first"
        byte[] ret = new byte[bArray.length - 1];
        for (int i = 0; i < ret.length; i++)
            ret[i] = bArray[i + 1];
        return ret;
    }
}

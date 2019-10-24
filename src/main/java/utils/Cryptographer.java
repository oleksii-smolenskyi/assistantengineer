package utils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;

public class Cryptographer {
    private String k = "Bar12345Bar12345";
    private final Key aesKey;
    private Cipher cipherEncrypt;
    private Cipher cipherDecrypt;

    public Cryptographer(String k) {
        this.k = k;
        aesKey = new SecretKeySpec(k.getBytes(), "AES");
        try {
            cipherEncrypt = Cipher.getInstance("AES");
            cipherEncrypt.init(Cipher.ENCRYPT_MODE, aesKey);
            cipherDecrypt = Cipher.getInstance("AES");
            cipherDecrypt.init(Cipher.DECRYPT_MODE, aesKey);
        } catch (Exception e) {
            new Exception("Fatal cryptographer error!");
        }
    }

    public String encrypt(String in) {
        if (in == null)
            in = "";
        try {
            return new String(cipherEncrypt.doFinal(in.getBytes()));
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            new Exception("Error encrypt data.");
        }
        return null;
    }

    public String decrypt(String in) {
        if (in == null)
            return null;
        try {
            return new String(cipherDecrypt.doFinal(in.getBytes()));
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            new Exception("Error decrypt data.");
        }
        return null;
    }
}

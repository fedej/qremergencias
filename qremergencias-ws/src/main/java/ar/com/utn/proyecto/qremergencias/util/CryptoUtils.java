package ar.com.utn.proyecto.qremergencias.util;

import com.google.common.io.ByteStreams;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class CryptoUtils {

    private static final String CHARSET_NAME = "ISO-8859-1";
    private static final IvParameterSpec IV_PARAMETER_SPEC;

    static {
        try {
            IV_PARAMETER_SPEC = new IvParameterSpec("4e5Wa71fYoT7MFEX".getBytes(CHARSET_NAME));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private static Cipher ENCRYPTING_CIPHER;
    private static Cipher DECRYPTING_CIPHER;

    private static Cipher initCipher(final int mode) {
        try {
            final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            final InputStream in = Thread.currentThread()
                    .getContextClassLoader().getResourceAsStream("keyPair/privateKey");
            final MessageDigest md = MessageDigest.getInstance("MD5");
            final SecretKeySpec key = new SecretKeySpec(md.digest(ByteStreams.toByteArray(in)), "AES");
            cipher.init(mode, key, IV_PARAMETER_SPEC);
            return cipher;
        } catch (final NoSuchAlgorithmException | NoSuchPaddingException
                | InvalidKeyException | IOException | InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        }
    }

    private static void initDecryptingCipher() {
        DECRYPTING_CIPHER = initCipher(Cipher.DECRYPT_MODE);
    }

    private static void initEncryptingCipher() {
        ENCRYPTING_CIPHER = initCipher(Cipher.ENCRYPT_MODE);
    }

    private CryptoUtils() {
    }

    public static String encryptText(final byte[] msg)
            throws NoSuchAlgorithmException, NoSuchPaddingException,
            IOException, IllegalBlockSizeException,
            BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {

        if (ENCRYPTING_CIPHER == null) {
            initEncryptingCipher();
        }

        return new String(ENCRYPTING_CIPHER.doFinal(msg), CHARSET_NAME);
    }

    public static byte[] decryptText(final String msg)
            throws InvalidKeyException, IOException,
            IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException {

        if (DECRYPTING_CIPHER == null) {
            initDecryptingCipher();
        }

        return DECRYPTING_CIPHER.doFinal(msg.getBytes(CHARSET_NAME));
    }

}

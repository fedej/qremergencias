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
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

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

    public static boolean verifySignature(final String publicKey, final String qrContent) {
        try {
            final Signature sig = Signature.getInstance("SHA256withECDSA");
            sig.initVerify(getPublicKey(publicKey));
            sig.update(getUserAndTimestamp(qrContent).getBytes(CHARSET_NAME));
            return sig.verify(getSignature(qrContent));
        } catch (final NoSuchAlgorithmException | UnsupportedEncodingException
                | InvalidKeyException | SignatureException | InvalidKeySpecException e) {
            return false;
        }
    }

    private static byte[] getSignature(final String qr) {
        try {
            return getSignatureContent(qr).getBytes("ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    private static String getUserAndTimestamp(final String qr) {
        final Integer signatureSize = Integer.valueOf(qr.substring(0, 3));
        return qr.substring(3 + signatureSize, qr.length()).replace(" ", "");
    }

    private static String getSignatureContent(final String qr) {
        final Integer signatureSize = Integer.valueOf(qr.substring(0, 3));
        return qr.substring(3, signatureSize + 3);
    }

    private static PublicKey getPublicKey(final String key) throws InvalidKeySpecException,
            NoSuchAlgorithmException, UnsupportedEncodingException {
        final byte[] decode = Base64.getDecoder().decode(key);
        final X509EncodedKeySpec spec = new X509EncodedKeySpec(decode);
        final KeyFactory kf = KeyFactory.getInstance("EC");
        return kf.generatePublic(spec);
    }

}

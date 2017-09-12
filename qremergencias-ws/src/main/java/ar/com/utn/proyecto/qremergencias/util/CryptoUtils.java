package ar.com.utn.proyecto.qremergencias.util;

import com.google.common.io.ByteStreams;
import org.springframework.core.io.Resource;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public final class CryptoUtils {

    private static final Cipher CIPHER;

    static {
        try {
            CIPHER = Cipher.getInstance("RSA");
        } catch (final NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    private CryptoUtils() {

    }

    public static String encryptText(final byte[] msg, final PrivateKey key)
            throws NoSuchAlgorithmException, NoSuchPaddingException,
            UnsupportedEncodingException, IllegalBlockSizeException,
            BadPaddingException, InvalidKeyException {
        CIPHER.init(Cipher.ENCRYPT_MODE, key);
        return new String(Base64.getEncoder().encode(CIPHER.doFinal(msg)), Charset.forName("UTF-8"));
    }

    public static String decryptText(final String msg, final PublicKey key)
            throws InvalidKeyException, UnsupportedEncodingException,
            IllegalBlockSizeException, BadPaddingException {
        CIPHER.init(Cipher.DECRYPT_MODE, key);
        return new String(CIPHER.doFinal(Base64.getDecoder().decode(msg)), "UTF-8");
    }

    public static PrivateKey getPrivate(final Resource file) throws IOException,
            NoSuchAlgorithmException, InvalidKeySpecException {
        final byte[] keyBytes = ByteStreams.toByteArray(file.getInputStream());
        return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
    }

    public static PublicKey getPublic(final Resource file) throws IOException,
            NoSuchAlgorithmException, InvalidKeySpecException {
        final byte[] keyBytes = ByteStreams.toByteArray(file.getInputStream());
        return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(keyBytes));
    }

}

package net.trollheim.gitdefender.actions.safestore;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Collections;
import java.util.List;


public final class EcdhUtils {
    private static final byte[] SEPARATOR = ".".getBytes();
    static String CIPHER = "AES/GCM/NoPadding";
    static private final SecureRandom random = new SecureRandom();

    public static KeySet ephemeralDh(PublicKey publicKey)
            throws Exception {
        var keygen = KeyPairGenerator.getInstance("EC");
        var ephemeral = keygen.generateKeyPair();
        var secret = ecdh(ephemeral.getPrivate(), publicKey);
        return new KeySet(secret, ephemeral.getPublic());
    }


    public static SecretKey ecdh(PrivateKey privateKey, PublicKey publicKey)
            throws Exception {
        KeyAgreement keyAgreement = KeyAgreement.getInstance("ECDH", BouncyCastleProvider.PROVIDER_NAME);
        keyAgreement.init(privateKey);
        keyAgreement.doPhase(publicKey, true);
        var secret = keyAgreement.generateSecret("AES");

        return secret;
    }

    public static InputStream encrypt(InputStream is, PublicKey key) throws Exception {
        var cipher = Cipher.getInstance(CIPHER);

        var secret = ephemeralDh(key);
        byte[] iv = new byte[12];
        random.nextBytes(iv);
        System.out.println("===== encrypt ===\n shared key : "+Base64.getEncoder().encodeToString(secret.shared().getEncoded()));
        cipher.init(Cipher.ENCRYPT_MODE, secret.shared(),new GCMParameterSpec(128, iv));

        ByteArrayInputStream pkIs = new ByteArrayInputStream(Base64.getEncoder().encode(secret.ephemeralPub().getEncoded()));

        ByteArrayInputStream ivStream = new ByteArrayInputStream(Base64.getEncoder().encode(iv));

        CipherInputStream cis = new CipherInputStream(is,cipher );
        List<InputStream> streams = List.of(
                pkIs,
                new ByteArrayInputStream(SEPARATOR),
                ivStream,
                new ByteArrayInputStream(SEPARATOR),
                cis
        );

        return new SequenceInputStream(Collections.enumeration(streams));
    }

    public static InputStream decrypt(InputStream is, byte[] key) throws Exception {
        KeyFactory kf = KeyFactory.getInstance("EC");

        var pub =  readBit(is);
        var iv =   readBit(is);

        EcdhUtils kp = new EcdhUtils();
        PrivateKey privateKey = kf.generatePrivate(new PKCS8EncodedKeySpec(key));
        PublicKey publicKey = kf.generatePublic(new X509EncodedKeySpec(pub));
        SecretKey secretKey = kp.ecdh(privateKey, publicKey);

        System.out.println("===== encrypt ===\n shared key : "+Base64.getEncoder().encodeToString(secretKey.getEncoded()));

        var cipher = Cipher.getInstance(CIPHER);

        cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(128, iv));
        return new CipherInputStream(is,cipher);
    }

    private static byte[] readBit(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        do {
            int value = is.read();
            if (value==-1) break;
            char ch= (char) value;
            if (ch=='.') break;
            sb.append(ch);
        } while (true);
        return Base64.getDecoder().decode(sb.toString());
    }
}

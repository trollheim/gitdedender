package net.trollheim.gitdefender.actions.safestore;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.Test;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;
import java.util.stream.Collectors;


class KeyProviderTest {

    @Test
    public void keyExchangeTest() throws Exception {

        Provider provider = new BouncyCastleProvider();
        Security.addProvider(provider);

        var keygen = KeyPairGenerator.getInstance("EC");
        var alice = keygen.generateKeyPair();
        System.out.println("priv "+Base64.getEncoder().encodeToString(alice.getPrivate().getEncoded()));
        System.out.println("pub  "+Base64.getEncoder().encodeToString(alice.getPublic().getEncoded()));


        String testMessage = "Hello World";
        InputStream is = new ByteArrayInputStream(testMessage.getBytes());
        InputStream encrypted = EcdhUtils.encrypt(is, alice.getPublic());
        InputStream plaintext = EcdhUtils.decrypt(encrypted, alice.getPrivate().getEncoded());
        String text = new BufferedReader(
                new InputStreamReader(plaintext, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));
        System.out.println(text);

     }
    public static IvParameterSpec generateIv() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }


    public static String encrypt(String algorithm, String input, SecretKey key,
                                 IvParameterSpec iv) throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {

        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);
        byte[] cipherText = cipher.doFinal(input.getBytes());
        return Base64.getEncoder()
                .encodeToString(cipherText);
    }

    public static String decrypt(String algorithm, String cipherText, SecretKey key,
                                 IvParameterSpec iv) throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {

        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.DECRYPT_MODE, key, iv);
        byte[] plainText = cipher.doFinal(Base64.getDecoder()
                .decode(cipherText));
        return new String(plainText);
    }

}
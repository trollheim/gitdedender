package net.trollheim.gitdefender.tools;

import net.trollheim.gitdefender.actions.safestore.EcdhUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Enumeration;
import java.util.List;


public class DecryptFile {

    static String CIPHER = "AES/GCM/NoPadding";


    public static void main(String[] args) throws Exception {
        Provider provider = new BouncyCastleProvider();
        Security.addProvider(provider);


        var privm = Base64.getDecoder().decode("MEECAQAwEwYHKoZIzj0CAQYIKoZIzj0DAQcEJzAlAgEBBCBmEx2ScFEcyyVylLywoApjY5J9QWdAmXYb1w1KR4hdDA==");
        var is = Files.newInputStream(Path.of("backup.enc"));

        var decrypted = EcdhUtils.decrypt(is, privm);

        Files.copy(decrypted, Path.of("bob2.zip"));


    }


}

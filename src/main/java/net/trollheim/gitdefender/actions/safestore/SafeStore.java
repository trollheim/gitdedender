package net.trollheim.gitdefender.actions.safestore;

import net.trollheim.gitdefender.Constants;
import net.trollheim.gitdefender.actions.ActionConfig;
import net.trollheim.gitdefender.actions.DefenderAction;
import net.trollheim.gitdefender.model.GitRepo;
import org.kohsuke.github.GHRepository;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

public class SafeStore implements DefenderAction {

    private static String REF = "refs/heads/master";

    private final KeyFactory keyFactory;
    public SafeStore(){
        try {
            keyFactory = KeyFactory.getInstance("EC");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }



    @Override
    public void execute(GHRepository repo, ActionConfig config) {
        try {
            String key = config.get(Constants.PUBLIC_KEY_VALUE_FIELD);
            String path = config.get(Constants.OUTPUT_FILE_LOCATION_FIELD);


            byte[] bytes = Base64.getDecoder().decode(key);
            PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(bytes));
            InputStream is = repo.readZip(x -> x, REF);

             Files.copy(EcdhUtils.encrypt(is,publicKey), Path.of(path));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}

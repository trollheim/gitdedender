package net.trollheim.gitdefender.actions.safestore;

import javax.crypto.SecretKey;
import java.security.PublicKey;

public record KeySet(SecretKey shared, PublicKey ephemeralPub) {

}

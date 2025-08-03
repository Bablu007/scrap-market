package com.scrapmarket.scrap_market.util;

import lombok.experimental.UtilityClass;

import javax.crypto.Cipher;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@UtilityClass
public class RsaUtil {
    private static final String ALGORITHM = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    private static final String PUBLIC_KEY_FILE = "rsa_public.key";
    private static final String PRIVATE_KEY_FILE = "rsa_private.key";

    static {
        try {
            if (!new File(PUBLIC_KEY_FILE).exists() || !new File(PRIVATE_KEY_FILE).exists()) {
                generateAndSaveKeyPair();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize RSA key files", e);
        }
    }

    public static void generateAndSaveKeyPair() throws Exception {
        KeyPair keyPair = generateKeyPair();

        // Save public key
        try (FileOutputStream fos = new FileOutputStream(PUBLIC_KEY_FILE)) {
            fos.write(keyPair.getPublic().getEncoded());
        }

        // Save private key
        try (FileOutputStream fos = new FileOutputStream(PRIVATE_KEY_FILE)) {
            fos.write(keyPair.getPrivate().getEncoded());
        }
    }

    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        return keyGen.generateKeyPair();
    }

    public static PublicKey getPublicKey() throws Exception {
        byte[] keyBytes = readFileToBytes(PUBLIC_KEY_FILE);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        return factory.generatePublic(spec);
    }

    public static PrivateKey getPrivateKey() throws Exception {
        byte[] keyBytes = readFileToBytes(PRIVATE_KEY_FILE);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        return factory.generatePrivate(spec);
    }

    public static String getEncodedPublicKey() throws Exception {
        return Base64.getEncoder().encodeToString(getPublicKey().getEncoded());
    }

    public static String encrypt(String data, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes()));
    }

    public static String decrypt(String encrypted, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encrypted));
        return new String(decryptedBytes);
    }

    private static byte[] readFileToBytes(String filename) throws IOException {
        try (FileInputStream fis = new FileInputStream(filename)) {
            return fis.readAllBytes();
        }
    }
}


package com.sockets.test;

import com.sockets.test.utils.Base64;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

public class SSLContextBuilder {


    public static SSLContext from(String domain) {
        String webserverCertPath = "C:\\Certbot\\live\\" + domain + "\\cert.pem";
        String webserverKeyPath = "C:\\Certbot\\live\\" + domain + "\\privkey.pem";

        javax.net.ssl.SSLContext context;
        String password = "";
        try {
            context = javax.net.ssl.SSLContext.getInstance("TLS");

            byte[] certBytes = parseDERFromPEM(getBytes(new File(webserverCertPath)));
            byte[] keyBytes = parseDERFromPEM(getBytes(new File(webserverKeyPath)));

            X509Certificate cert = generateCertificateFromDER(certBytes);
            RSAPrivateKey key = generatePrivateKeyFromDER(keyBytes);

            KeyStore keystore = KeyStore.getInstance("JKS");
            keystore.load(null);
            keystore.setCertificateEntry("cert-alias", cert);
            keystore.setKeyEntry("key-alias", key, password.toCharArray(), new Certificate[]{cert});

            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(keystore, password.toCharArray());

            KeyManager[] km = kmf.getKeyManagers();

            context.init(km, null, null);
        } catch (Exception e) {
            context = null;
        }
        System.out.println("cert " + domain + " activated");
        return context;
    }

    private static byte[] parseDERFromPEM(byte[] pem) {
        return Base64.decode(removeFirstAndLastLine(new String(pem)));
    }

    public static KeyPair generateKeyPairs() {
        KeyPairGenerator keyGen;
        try {
            keyGen = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
        keyGen.initialize(4096);
        return keyGen.generateKeyPair();
    }

    public static String removeFirstAndLastLine(String input) {
        String[] lines = input.split("\n");
        if (lines.length <= 2) {
            return ""; // Если строк меньше или равно 2, возвращаем пустую строку
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < lines.length - 1; i++) {
            sb.append(lines[i]).append("\n");
        }
        return sb.toString().trim(); // Удаляем последний символ новой строки
    }

    private static RSAPrivateKey generatePrivateKeyFromDER(byte[] keyBytes)
            throws InvalidKeySpecException, NoSuchAlgorithmException {
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);

        KeyFactory factory = KeyFactory.getInstance("RSA");
        return (RSAPrivateKey) factory.generatePrivate(spec);
    }

    private static X509Certificate generateCertificateFromDER(byte[] certBytes)
            throws CertificateException {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");

        return (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(certBytes));
    }

    private static byte[] getBytes(File file) {
        byte[] bytesArray = new byte[(int) file.length()];

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            fis.read(bytesArray);
            fis.close();
        } catch (IOException e) {
        }
        return bytesArray;
    }
}

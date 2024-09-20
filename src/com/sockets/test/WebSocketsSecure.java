package com.sockets.test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Scanner;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import org.java_websocket.server.DefaultSSLWebSocketServerFactory;


public class WebSocketsSecure extends Sockets {

    public WebSocketsSecure(int port) {
        super(port);
        SSLContext context = getContext();
        if (context != null) {
            System.out.println("WSS context activated");
            setWebSocketFactory(new DefaultSSLWebSocketServerFactory(getContext()));
        }
    }

    private static SSLContext getContext() {
        SSLContext context = null;
        String password = "password";
        try {
            context = SSLContext.getInstance("TLS");

            byte[] certBytes = parseDERFromPEM(getBytes(new File("C:\\wamp\\bin\\apache\\apache2.4.51\\conf\\ssl\\webserver.cert")),
                    "-----BEGIN CERTIFICATE-----", "-----END CERTIFICATE-----");
            byte[] keyBytes = parseDERFromPEM(getBytes(new File("C:\\wamp\\bin\\apache\\apache2.4.51\\conf\\ssl\\webserver.key")),
                    "-----BEGIN RSA PRIVATE KEY-----", "-----END RSA PRIVATE KEY-----");

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
        return context;
    }

    private static byte[] parseDERFromPEM(byte[] pem, String beginDelimiter, String endDelimiter) {
        String data = new String(pem);
        String[] tokens = data.split(beginDelimiter);
        tokens = tokens[1].split(endDelimiter);
        return Base64.decode(tokens[0]);
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

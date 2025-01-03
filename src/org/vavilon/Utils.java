package org.vavilon;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.vavilon.utils.MD5;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Random;

import static org.vavilon.contracts.mining.Miner.hashMod;

public class Utils {
    public static final Random random = new Random();
    public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    public static final long WEEK_SEC = 604800L;

    public static String md5(String input) {
        return MD5.hash(input);
    }

    public static Double round(Double value) {
        return round(value, 4);
    }

    public static Double round(Double value, int precision) {
        return Math.round(value * Math.pow(10, precision)) / Math.pow(10, precision);
    }

    public static int random() {
        return Math.abs(random.nextInt());
    }

    public static Long time() {
        return System.currentTimeMillis() / 1000;
    }

    static String readFile(String path) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded);
    }

    public static void writeFile(String path, Object object) throws IOException {
        Files.write(Paths.get(path), gson.toJson(object).getBytes());
    }

    public static int getPortOffset(String domain) {
        return Math.toIntExact(hashMod(domain, 16L));
    }

    public static void disableCertificateValidation() throws Exception {
        TrustManager[] trustAllCertificates = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    @Override
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
        };

        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, trustAllCertificates, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
    }

    public Double last(List<Double> list) {
        return list.get(list.size() - 1);
    }
}

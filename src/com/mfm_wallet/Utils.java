package com.mfm_wallet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sockets.test.utils.MD5;
import fi.iki.elonen.NanoHTTPD;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Utils {

    public static Random random = new Random();
    public static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static String md5(String input) {
        return MD5.hash(input);
    }

    public static Map<String, String> error(Object message) {
        if (message instanceof String) {
            throw new RuntimeException((String) message);
        } else {
            throw new RuntimeException(gson.toJson(message));
        }
    }

    public static Double round(Double value, int precision) {
        return Math.round(value * Math.pow(10, precision)) / Math.pow(10, precision);
    }

    static Map<String, String> parseParams(NanoHTTPD.IHTTPSession session) {
        Map<String, String> params = new HashMap<>();
        params.putAll(session.getHeaders());
        if (session.getMethod() == NanoHTTPD.Method.POST) {
            try {
                session.parseBody(params);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        params.putAll(session.getParms());
        return params;
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
        String hash = MD5.hash(domain);
        BigInteger big = new BigInteger(hash, 16);
        return big.mod(BigInteger.valueOf(16)).intValue();
    }
}

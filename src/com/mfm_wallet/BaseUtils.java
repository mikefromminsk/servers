package com.mfm_wallet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fi.iki.elonen.NanoHTTPD;

import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

public class BaseUtils {

    static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    static String md5(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] messageDigest = md.digest(input.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : messageDigest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    static Map<String, String> error(String message) {
        throw new RuntimeException(message);
    }

    static Map<String, String> parseParams(NanoHTTPD.IHTTPSession session) {
        Map<String, String> params = new HashMap<>();
        params.putAll(session.getHeaders());
        params.putAll(session.getParms());
        if (session.getMethod().equals("POST")) {
            gson.fromJson(new InputStreamReader(session.getInputStream()), Map.class)
                    .forEach((k, v) -> params.put((String) k, (String) v));
        }
        return params;
    }

    static String getString(Map<String, String> params, String key) {
        return params.get(key);
    }

    static String getRequired(Map<String, String> params, String key) {
        String value = getString(params, key);
        if (value == null) {
            error(key + " is empty");
        }
        return value;
    }

    static Double getDoubleRequired(Map<String, String> params, String key) {
        String value = getRequired(params, key);
        return Double.parseDouble(value);
    }

    static NanoHTTPD.Response commit(Map<String, String> response) {
        response.put("success", "true");
        return newFixedLengthResponse(gson.toJson(response));
    }
}

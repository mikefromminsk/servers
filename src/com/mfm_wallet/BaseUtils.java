package com.mfm_wallet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sockets.test.utils.MD5;
import fi.iki.elonen.NanoHTTPD;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

public class BaseUtils {

    static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    static String md5(String input) {
        return MD5.hash(input);
    }

    public static Map<String, String> error(Object message) {
        if (message instanceof String) {
            throw new RuntimeException((String) message);
        } else {
            throw new RuntimeException(gson.toJson(message));
        }
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
        params.put("script_path", session.getUri().substring(1));
        return params;
    }

    public static String getString(Map<String, String> params, String key) {
        return params.get(key);
    }

    public static String getRequired(Map<String, String> params, String key) {
        String value = getString(params, key);
        if (value == null) {
            error(key + " is empty");
        }
        return value;
    }

    public static Double getDouble(Map<String, String> params, String key) {
        return getDouble(params, key, null);
    }

    public static Double getDouble(Map<String, String> params, String key, Double defaultValue) {
        String value = getString(params, key);
        return value == null ? defaultValue : Double.parseDouble(value);
    }

    public static Long getLong(Map<String, String> params, String key, Long defaultValue) {
        String value = getString(params, key);
        return value == null ? defaultValue : Long.parseLong(value);
    }

    public static Double getDoubleRequired(Map<String, String> params, String key) {
        String value = getRequired(params, key);
        return Double.parseDouble(value);
    }

    static void broadcast(String channel, String message) {
        // Implement broadcast logic here
    }

    public static Long time() {
        return (long) System.currentTimeMillis() / 1000;
    }
}

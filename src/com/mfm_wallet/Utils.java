package com.mfm_wallet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sockets.test.utils.MD5;
import fi.iki.elonen.NanoHTTPD;

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

    public static void broadcast(String channel, String message) {
        // Implement broadcast logic here
    }

    public static Long time() {
        return System.currentTimeMillis() / 1000;
    }
}

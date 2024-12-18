package com.sockets.test.utils;

import com.google.gson.Gson;

import java.net.HttpURLConnection;
import java.net.URL;

public class Request {
    public static Gson json = new Gson();

    public static void post(String redirectUrl) {
        post(redirectUrl, null, null, null);
    }

    public static void post(String redirectUrl,
                            Object data) {
        post(redirectUrl, data, null, null);
    }

    public static void post(String redirectUrl,
                            Object data,
                            Success success) {
        post(redirectUrl, data, success, null);
    }

    public static void post(String redirectUrl,
                            Object data,
                            Success success,
                            Success error) {
        try {
            if (redirectUrl == null) {
                return;
            }
            if (!redirectUrl.contains("://")) {
                redirectUrl = "http://localhost" + redirectUrl;
            }
            byte[] byteData = new byte[0];
            if (data != null)
                byteData = json.toJson(data).getBytes();
            URL url = new URL(redirectUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Content-Length", "" + byteData.length);
            conn.setDoOutput(true);
            conn.getOutputStream().write(byteData, 0, byteData.length);

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                if (success != null)
                    success.run(StringUtils.convertToString(conn.getInputStream()));
            } else {
                String response = StringUtils.convertToString(conn.getErrorStream());
                if (error != null) {
                    error.run(response);
                } else {
                    System.out.println(redirectUrl);
                    System.out.println(response);
                }
            }
        } catch (Exception e) {
            if (error != null) {
                error.run(e.getMessage());
            }
        }
    }
}

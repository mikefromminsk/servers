package com.hatosh.utils;

import com.google.gson.Gson;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.StringJoiner;

public class Request {
    public static Gson json = new Gson();

    public static void post(String redirectUrl) {
        post(redirectUrl, null, null, null);
    }

    public static void post(String redirectUrl,
                            Map<String, String> params) {
        post(redirectUrl, params, null, null);
    }

    public static void post(String redirectUrl,
                            Map<String, String> params,
                            Success success) {
        post(redirectUrl, params, success, null);
    }

    public static void post(String redirectUrl,
                            Map<String, String> params,
                            Success success,
                            Success error) {
        try {
            if (redirectUrl == null) {
                return;
            }
            if (!redirectUrl.contains("://")) {
                if (redirectUrl.contains("localhost")) {
                    redirectUrl = "http://" + redirectUrl;
                } else {
                    redirectUrl = "https://" + redirectUrl;
                }
            }

            StringJoiner paramsStr = new StringJoiner("&");
            for (String key : params.keySet()) {
                String value = params.get(key);
                paramsStr.add(key + "=" + value);
            }
            byte[] byteData = paramsStr.toString().getBytes("UTF-8");

            URL url = new URL(redirectUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
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

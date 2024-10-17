package com.sockets.test;

import com.google.gson.Gson;
import com.sockets.test.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

public class Backend {
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
                            SuccessCallback success) {
        post(redirectUrl, data, success, null);
    }

    public static void post(String redirectUrl,
                            Object data,
                            SuccessCallback success,
                            ErrorCallback error) {
        try {
            if (redirectUrl == null) {
                return;
            }
            if (!redirectUrl.contains("://")) {
                redirectUrl = "http://localhost/" + redirectUrl;
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
                    success.run(Utils.convertToString(conn.getInputStream()));
            } else {
                String response = Utils.convertToString(conn.getErrorStream());
                System.out.println(response);
                if (error != null)
                    error.run(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    interface SuccessCallback {
        void run(String response);
    }

    interface ErrorCallback {
        void run(String response);
    }
}

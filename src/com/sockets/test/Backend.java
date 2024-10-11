package com.sockets.test;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

public class Backend {
    private static final Logger log = LoggerFactory.getLogger(Backend.class);
    public static Gson json = new Gson();

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
                if (error != null)
                    error.run(Utils.convertToString(conn.getErrorStream()));
                else
                    log.error(new Date() + " " + responseCode + " " + redirectUrl);
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

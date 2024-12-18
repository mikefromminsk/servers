package com.sockets.test;

import com.sockets.test.utils.SSLContextBuilder;
import com.sockets.test.utils.StringUtils;
import com.sun.net.httpserver.*;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;


public class TelegramRedirector {

    private int port;

    public TelegramRedirector(int port) {
        this.port = port;
    }

    public void start() throws Exception {
        HttpsServer server = HttpsServer.create(new InetSocketAddress(port), 0);
        SSLContext sslContext = SSLContextBuilder.from("telegram");
        if (sslContext != null){
            System.out.println("HTTPS context activated");
            server.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
                public void configure(HttpsParameters params) {
                    params.setSSLParameters(sslContext.getDefaultSSLParameters());
                }
            });
        }

        regBot(server, "mytoken_space_bot");
        server.setExecutor(null);
        server.start();
        System.out.println("HTTPS telegram redirector started on port: " + port);
    }

    public void regBot(HttpsServer server, String botName) {
        server.createContext("/" + botName, new RedirectHandler("http://localhost/mfm-telegram/hook.php?bot=" + botName));
    }


    public class RedirectHandler implements HttpHandler {

        private final String redirectUrl;

        public RedirectHandler(String redirectUrl) {
            this.redirectUrl = redirectUrl;
        }



        @Override
        public void handle(HttpExchange request) {
            try {
                byte[] input = StringUtils.convertToBytes(request.getRequestBody());
                System.out.println(new String(input));

                URL url = new URL(redirectUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod(request.getRequestMethod());
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Content-Length", Integer.toString(input.length));
                conn.setDoOutput(true);
                conn.getOutputStream().write(input, 0, input.length);

                String response = "{success: true}";
                request.sendResponseHeaders(200, response.length());
                OutputStream os = request.getResponseBody();
                os.write(response.getBytes());
                os.close();

                int responseCode = conn.getResponseCode();
                System.out.println("Response Code: " + responseCode);
                if (responseCode != 200) {
                    System.out.println("Response Body: " + StringUtils.convertToString(conn.getErrorStream()));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
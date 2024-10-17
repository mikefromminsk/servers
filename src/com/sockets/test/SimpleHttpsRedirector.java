package com.sockets.test;

import com.sockets.test.utils.Utils;
import com.sun.net.httpserver.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;


public class SimpleHttpsRedirector {

    private static final Logger log = LoggerFactory.getLogger(SimpleHttpsRedirector.class);
    private int port;

    public SimpleHttpsRedirector(int port) {
        this.port = port;
    }

    public void start() throws Exception {
        HttpsServer server = HttpsServer.create(new InetSocketAddress(port), 0);
        SSLContext sslContext = SSLContextBuilder.from(
                "C:\\wamp\\bin\\apache\\apache2.4.51\\conf\\ssl\\telegram\\webserver.cert",
                "C:\\wamp\\bin\\apache\\apache2.4.51\\conf\\ssl\\telegram\\webserver.key"
        );
        if (sslContext != null)
            server.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
                public void configure(HttpsParameters params) {
                    params.setSSLParameters(sslContext.getDefaultSSLParameters());
                }
            });

        regBot(server, "mytoken_space_bot");
        regBot(server, "mytoken_world_bot");
        server.setExecutor(null);
        server.start();
        Sockets.log("HTTPS telegram redirector started on port: " + port);
    }

    public void regBot(HttpsServer server, String botName) {
        server.createContext("/" + botName, new RedirectHandler("http://localhost/mfm-telegram/api/hook.php?bot=" + botName));
    }


    public class RedirectHandler implements HttpHandler {

        private final String redirectUrl;

        public RedirectHandler(String redirectUrl) {
            this.redirectUrl = redirectUrl;
        }



        @Override
        public void handle(HttpExchange request) {
            try {
                byte[] input = Utils.convertToBytes(request.getRequestBody());
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
                    System.out.println("Response Body: " + Utils.convertToString(conn.getErrorStream()));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
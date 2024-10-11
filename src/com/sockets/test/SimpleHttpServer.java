package com.sockets.test;

import com.google.gson.Gson;
import com.sockets.test.model.Message;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.java_websocket.WebSocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.stream.Collectors;

import static com.sockets.test.Sockets.channels;
import static com.sockets.test.Sockets.log;

public class SimpleHttpServer {

    int port;

    public SimpleHttpServer(int port) {
        this.port = port;
    }

    static Gson json = new Gson();

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/test", new MyHandler());
        server.setExecutor(null);
        server.start();
        log("Http started on port: " + port);
    }

    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange request) throws IOException {
            String requestBody = new BufferedReader(new InputStreamReader(request.getRequestBody()))
                    .lines().collect(Collectors.joining("\n"));

            Message message = json.fromJson(requestBody, Message.class);
            if (channels.containsKey(message.channel)) {
                Iterator<WebSocket> iterator = channels.get(message.channel).iterator();
                while (iterator.hasNext()) {
                    WebSocket conn = iterator.next();
                    if (conn.isOpen()) {
                        conn.send(requestBody);
                        log("sent: " + requestBody);
                    } else {
                        iterator.remove();
                    }
                }
            }
            log("channel: " + message.channel);
            String response = "This is the response";
            request.sendResponseHeaders(200, response.length());
            OutputStream os = request.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}

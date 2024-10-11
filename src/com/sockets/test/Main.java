package com.sockets.test;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        try {
            SimpleFtpServer ftpServer = new SimpleFtpServer(21, 50000, 50100);
            ftpServer.start();
            SimpleHttpServer httpServer = new SimpleHttpServer(8002);
            httpServer.start();
            SimpleHttpsRedirector httpsServer = new SimpleHttpsRedirector(8443);
            httpsServer.start();
            WebSocketsSecure webSocketsSecure = new WebSocketsSecure(8887);
            webSocketsSecure.start();
            InfiniteTimer infiniteTimer = new InfiniteTimer();
            infiniteTimer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        new Scanner(System.in).nextLine();
    }
}

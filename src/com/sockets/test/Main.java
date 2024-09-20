package com.sockets.test;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        try {
            SimpleFtpServer ftpServer = new SimpleFtpServer();
            ftpServer.start();
            SimpleHttpServer httpServer = new SimpleHttpServer();
            httpServer.start();
            WebSocketsSecure webSocketsSecure = new WebSocketsSecure(8887);
            webSocketsSecure.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        new Scanner(System.in).nextLine();
    }
}

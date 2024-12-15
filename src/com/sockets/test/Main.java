package com.sockets.test;

import com.mfm_wallet.Server;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        try {
            /*new SimpleFtpServer(21, 50000, 50100).start();
            new SimpleHttpServer(8002).start();
            WebSocketsSecure.create("localhost").start();
            WebSocketsSecure.create("hatosh.com").start();
            WebSocketsSecure.create("mytoken.space").start();
            new SimpleHttpsRedirector(8443).start();
            new InfiniteTimer("/mfm-mail/job.php", 1000 * 60).start();*/

            new Server(8003);
            System.out.println("Token server started on port 8003");

        } catch (Exception e) {
            e.printStackTrace();
        }
        new Scanner(System.in).nextLine();
    }
}

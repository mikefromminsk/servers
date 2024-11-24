package com.sockets.test;

import com.sockets.test.benchmark.TPS;
import com.sockets.test.utils.Success;

import java.util.HashMap;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        try {
            new SimpleFtpServer(21, 50000, 50100).start();
            new SimpleHttpServer(8002).start();
            new WebSocketsSecure(8887).start();
            new SimpleHttpsRedirector(8443).start();
            //new InfiniteTimer("/mfm-mail/templates/test_invite/job.php", 1000 * 60).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        new Scanner(System.in).nextLine();
    }
}

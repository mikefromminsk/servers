package com.sockets.test;

import com.mfm_wallet.Node;

import java.util.Scanner;

public class StartNodeFull {

    public static void main(String[] args) {
        try {
            new Node("mytoken.space", "hatosh.com").start();
            new FtpServer(21, 50000, 50100).start();
            new TelegramRedirector(8443).start();
            //new InfiniteTimer("/mfm-mail/job.php", 1000 * 60).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        new Scanner(System.in).nextLine();
    }
}

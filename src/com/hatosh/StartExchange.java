package com.hatosh;

import com.hatosh.exchange.ExchangeServer;
import com.hatosh.servers.FtpServer;
import com.hatosh.telegram.TelegramRedirector;

import java.io.IOException;
import java.util.Scanner;

import static com.hatosh.wallet.Utils.disableCertificateValidation;

public class StartExchange {

    public static void main(String[] args) throws IOException {
        try {
            disableCertificateValidation();
            new ExchangeServer("mytoken.space", "hatosh.com").start();
            new FtpServer(21, 50000, 50100).start();
            new TelegramRedirector(8443).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        new Scanner(System.in).nextLine();
    }
}

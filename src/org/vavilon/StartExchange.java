package org.vavilon;

import org.vavilon.exchange.ExchangeServer;
import org.vavilon.servers.FtpServer;
import org.vavilon.telegram.TelegramRedirector;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import static org.vavilon.wallet.Utils.disableCertificateValidation;

public class StartExchange {

    public static void main(String[] args) throws IOException {
        try {
            disableCertificateValidation();
            String site_domain = "localhost";
            String masterNode = null;
            if (args.length >= 1)
                site_domain = args[0];
            if (args.length >= 2)
                masterNode = args[1];

            if (site_domain.equals("localhost")) {
                try {
                    for(File file: new File(".").listFiles())
                        if (!file.isDirectory())
                            file.delete();
                } catch (Exception e) {
                }
            }
            new ExchangeServer(site_domain, masterNode).start();
            new FtpServer(21, 50000, 50100).start();
            new TelegramRedirector(8443).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        new Scanner(System.in).nextLine();
    }
}

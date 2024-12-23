package com.hatosh;

import com.hatosh.wallet.Node;

import java.io.IOException;
import java.util.Scanner;

public class StartNode {
    public static void main(String[] args) throws IOException {
        String site_domain = "localhost";
        String masterNode = null;
        if (args.length >= 1)
            site_domain = args[0];
        if (args.length >= 2)
            masterNode = args[1];
        new Node(site_domain, masterNode).start();
        new Scanner(System.in).nextLine();
    }
}

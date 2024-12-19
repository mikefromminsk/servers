package com.sockets.test;

import com.mfm_wallet.Node;

public class StartNode {
    public static void main(String[] args) {
        new Node("localhost", null).start();
    }
}

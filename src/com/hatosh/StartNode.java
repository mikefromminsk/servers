package com.hatosh;

import com.hatosh.wallet.Node;

import java.io.IOException;

public class StartNode {
    public static void main(String[] args) throws IOException {
        new Node("localhost", null).start();
    }
}

package com.sockets.test;

import com.sockets.test.utils.MD5;

import java.util.Scanner;

public class Test {
    public static void main(String[] args) {
        /*InfiniteTimer infiniteTimer = new InfiniteTimer("ext/email_finder.php");
        infiniteTimer.start();*/

        System.out.println(MD5.hash("admin"));
        new Scanner(System.in).nextLine();
    }
}

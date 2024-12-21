package com.hatosh.wallet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hatosh.utils.MD5;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;

import static com.hatosh.wallet.data.mining.Miner.hashMod;

public class Utils {
    public static Random random = new Random();
    public static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static String md5(String input) {
        return MD5.hash(input);
    }

    public static Double round(Double value, int precision) {
        return Math.round(value * Math.pow(10, precision)) / Math.pow(10, precision);
    }

    public static Long time() {
        return System.currentTimeMillis() / 1000;
    }

    static String readFile(String path) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded);
    }

    public static void writeFile(String path, Object object) throws IOException {
        Files.write(Paths.get(path), gson.toJson(object).getBytes());
    }

    public static int getPortOffset(String domain) {
        return Math.toIntExact(hashMod(domain, 16L));
    }
}

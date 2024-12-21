package com.hatosh.wallet.token;

public class Tran extends TokenUtils {
    @Override
    public void run() {
        String next_hash = getRequired("next_hash");
        response.put("tran", getTran(next_hash));
    }
}

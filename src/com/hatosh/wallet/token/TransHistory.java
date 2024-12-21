package com.hatosh.wallet.token;

public class TransHistory extends TokenUtils {
    @Override
    public void run() {
        response.put("trans", transHistory);
    }
}

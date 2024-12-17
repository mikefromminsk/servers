package com.mfm_wallet.mfm_mining;

import com.mfm_wallet.mfm_data.DataContract;

import java.math.BigInteger;

public class Miner extends DataContract {

    public Long calcNonce(String domain, String lastHash, Long difficulty) {
        for (int i = 0; i < 1000000; i++) {
            long nonce = (long) (Math.random() * 100000000);
            String str = lastHash + domain + nonce;
            String hash = md5(str);
            BigInteger hashNumber = new BigInteger(hash, 16);
            if (hashNumber.mod(BigInteger.valueOf(difficulty)).equals(BigInteger.ZERO)) {
                return nonce;
            }
        }
        return null;
    }

    @Override
    protected void run() {
        String domain = getRequired("domain");
        String lastHash = dataGet("last_hash");
        Long difficulty = dataGetLong("mining/" + domain + "/difficulty", 1L);
        Long nonce = calcNonce(domain, lastHash, difficulty);
        if (nonce == null) error("Nonce not found");
        response.put("nonce", nonce);
    }
}

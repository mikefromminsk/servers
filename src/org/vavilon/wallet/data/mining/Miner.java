package org.vavilon.wallet.data.mining;

import org.vavilon.wallet.data.Contract;

import java.math.BigInteger;

public class Miner extends Contract {

    public static Long hashMod(String str, Long mod) {
        String hash = md5(str);
        BigInteger hashNumber = new BigInteger(hash, 16);
        return hashNumber.mod(BigInteger.valueOf(mod)).longValue();
    }

    public Long nonceBruteForce(String domain, String lastHash, Long difficulty) {
        for (int i = 0; i < 1000000; i++) {
            long nonce = (long) (Math.random() * 100000000);
            Long hashMod = hashMod(lastHash + domain + nonce, difficulty);
            if (hashMod == 0) {
                return nonce;
            }
        }
        return null;
    }

    @Override
    public void run() {
        String domain = getRequired("domain");
        String lastHash = dataGet("last_hash");
        Long difficulty = dataGetLong("mining/" + domain + "/difficulty", 1L);
        Long nonce = nonceBruteForce(domain, lastHash, difficulty);
        if (nonce == null) error("Nonce not found");
        response.put("nonce", nonce);
    }
}

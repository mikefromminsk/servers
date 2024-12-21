package com.hatosh.wallet.data.mining;

import com.hatosh.wallet.data.Contract;

import static com.hatosh.wallet.data.mining.Mint.REWARD_MULTIPLIER;

public class Info extends Contract {
    @Override
    public void run() {
        String domain = getRequired("domain");
        response.put("last_hash", dataGet("mining/" + domain + "/last_hash"));
        response.put("difficulty", dataGet("mining/" + domain + "/difficulty"));
        response.put("bank", tokenBalance(domain, "mining"));
        response.put("last_reward", round(tokenBalance(domain, "mining") * REWARD_MULTIPLIER, 2));
    }
}

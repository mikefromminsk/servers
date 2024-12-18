package com.mfm_wallet.mfm_mining;

import com.mfm_wallet.mfm_data.DataContract;

import java.math.BigInteger;
import java.util.List;

import static com.mfm_wallet.Node.broadcast;
import static com.sockets.test.utils.Params.map;

public class Mint extends DataContract {

    Double getReward(String domain) {
        Double token_balance = tokenBalance(domain, "mining");
        return round(token_balance * 0.001, 2);
    }

    @Override
    protected void run() {
        String gasAddress = getRequired("gas_address");
        Long nonce = getLongRequired("nonce");
        String domain = getRequired("domain");

        tokenRegScript(domain, "mining", "mfm-mining/mint.php");

        String lastHash = dataGet("mining/" + domain + "/last_hash");
        if (lastHash == null) lastHash = "";
        Long difficulty = dataGetLong("mining/" + domain + "/difficulty");
        if (difficulty == null) difficulty = 1L;

        String str = lastHash + domain + nonce;
        String newHash = md5(str);

        response.put("newHash", str);
        response.put("newHash", newHash);
        response.put("lastHash", lastHash);

        BigInteger hashNumber = new BigInteger(newHash, 16);
        hashNumber = hashNumber.mod(BigInteger.valueOf(difficulty));

        if (hashNumber.equals(BigInteger.ZERO)) {
            double reward = getReward(domain);
            tokenSend(scriptPath, domain, "mining", gasAddress, reward, tokenPass(domain, "mining", "mining"), null);
            long interval = time() - dataFindPath("mining/" + domain + "/last_hash", true).time;
            int needInterval = 60;
            long timeDiff = interval - needInterval;

            int axelerate = 0;
            List<Long> difficultyHistory = getHistoryLong("mining/" + domain + "/difficulty", 20);
            for (int i = 1; i < difficultyHistory.size(); i++) {
                if (difficultyHistory.get(i) > difficultyHistory.get(i - 1))
                    axelerate += 1;
                if (difficultyHistory.get(i) < difficultyHistory.get(i - 1))
                    axelerate -= 1;
            }
            if (timeDiff > 0) {
                axelerate += 1;
            } else if (timeDiff < 0) {
                axelerate -= 1;
            }

            int difficultyDiff = (int) Math.pow(2, Math.abs(axelerate));

            if (timeDiff == 0) {
                difficultyDiff = 0;
            } else if (timeDiff > 0) {
                difficultyDiff = -difficultyDiff;
            }
            difficulty += difficultyDiff;
            if (difficulty < 1) {
                difficulty = 1L;
            }
            dataSet("mining/" + domain + "/difficulty", "" + difficulty);
            dataSet("mining" + domain + "/last_hash", newHash);

            broadcast("mining", map(
                    "domain", domain,
                    "difficulty", "" + difficulty,
                    "lastHash", lastHash,
                    "reward", "" + reward,
                    "gas_address", gasAddress));
            response.put("minted", reward);
        } else {
            error("Invalid nonce");
        }
    }
}

package org.vavilon.data.mining;

import org.vavilon.data.Contract;

import java.math.BigInteger;
import java.util.List;

import static org.vavilon.Node.broadcast;
import static org.vavilon.utils.Params.map;

public class Mint extends Contract {

    public static double REWARD_MULTIPLIER = 0.0001;

    @Override
    public void run() {
        String gasAddress = getRequired("gas_address");
        Long nonce = getLongRequired("nonce");
        String domain = getRequired("domain");

        tokenRegScript(domain, "mining", "mfm-mining/mint");

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
            double reward = round(tokenBalance(domain, "mining") * REWARD_MULTIPLIER);
            tokenSend(scriptPath, domain, "mining", gasAddress, reward, tokenPass(domain, "mining", "mining"), null);
            DataRow lastHashRow = dataFindPath("mining/" + domain + "/last_hash", false);
            long interval = time() - (lastHashRow == null ? 0 : lastHashRow.time);
            int needInterval = 60;
            long timeDiff = interval - needInterval;

            int axelerate = 0;
            List<Long> difficultyHistory = getHistoryLong("mining/" + domain + "/difficulty", 20);
            for (int i = 0; i < difficultyHistory.size() - 1; i++) {
                if (difficultyHistory.get(i) > difficultyHistory.get(i + 1))
                    axelerate += 1;
                if (difficultyHistory.get(i) < difficultyHistory.get(i + 1))
                    axelerate -= 1;
            }
            /*if (timeDiff > 0) {
                axelerate += 1;
            } else if (timeDiff < 0) {
                axelerate -= 1;
            }*/

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
            dataSet("mining/" + domain + "/last_hash", newHash);

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

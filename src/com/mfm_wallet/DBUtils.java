package com.mfm_wallet;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

class DBUtils extends Utils {



/*    Map<String, String> tokenSecondTran(String domain) {
        return selectRow("SELECT * FROM trans WHERE domain = '" + domain + "' AND `from` = '" + GENESIS_ADDRESS + "' ORDER BY time LIMIT 1, 1");
    }

    Map<String, String> tokenTran(String nextHash) {
        return selectRow("SELECT * FROM trans WHERE next_hash = '" + nextHash + "'");
    }


    Map<String, String> tokenLastTran(String domain, String fromAddress, String toAddress) {
        List<Transaction> trans = tokenTrans(domain, fromAddress, toAddress, 0, 1);
        return trans.isEmpty() ? null : trans.get(0);
    }*/

    static void trackAccumulate(String key) {
        // Implement tracking logic here
    }

    static void trackAccumulate(String key, int value) {
        // Implement tracking logic here
    }

}

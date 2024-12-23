package com.hatosh.wallet.token;

import com.hatosh.wallet.token.model.Transaction;

import java.util.ArrayList;
import java.util.List;

public class TransHistory extends TokenUtils {
    @Override
    public void run() {
        List<Transaction> trans = new ArrayList<>();
        for (int i = 0; i < transHistory.size(); i++)
            trans.add(transHistory.get(i));
        response.put("trans", trans);
    }
}

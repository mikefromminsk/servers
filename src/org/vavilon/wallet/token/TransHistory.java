package org.vavilon.wallet.token;

import org.vavilon.wallet.token.model.Tran;

import java.util.ArrayList;
import java.util.List;

public class TransHistory extends TokenUtils {
    @Override
    public void run() {
        Long page = getLong("page", 0L);
        Long size = getLong("size", 1000L);
        List<Tran> trans = new ArrayList<>();
        for (int i = 0; i < size && i < transHistory.size(); i++)
            trans.add(transHistory.get(page * size + i));
        response.put("trans", trans);
        response.put("trans_count", transHistory.size());
    }
}

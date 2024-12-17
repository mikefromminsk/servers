package com.mfm_wallet.mfm_token;

import com.mfm_wallet.Contract;
import com.mfm_wallet.model.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class Search extends Contract {
    @Override
    protected void run() {
        String search_text = getRequired("search_text");
        List<Token> tokens = new ArrayList<>();
        tokens.add(getToken(search_text));
        response.put("tokens", tokens);
    }
}

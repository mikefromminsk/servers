package org.vavilon.wallet.token;

import org.vavilon.wallet.token.model.Token;

import java.util.ArrayList;
import java.util.List;


public class Search extends TokenUtils {
    @Override
    public void run() {
        String search_text = getRequired("search_text");
        List<Token> tokens = new ArrayList<>();
        Token token = getToken(search_text);
        if (token != null)
            tokens.add(token);
        response.put("tokens", tokens);
    }
}

package org.vavilon.token;

public class Search extends TokenUtils {
    @Override
    public void run() {
        String search_text = getRequired("search_text");
        response.put("tokens", searchTokens(search_text));
    }
}

package org.vavilon.utils;

import java.util.HashMap;
import java.util.Map;

public class Params {

    public static Map<String, String> map(String... params) {
        Map<String, String> paramMap = new HashMap<>();
        if (params.length % 2 != 0) {
            throw new IllegalArgumentException("Invalid number of parameters. Parameters should be in key-value pairs.");
        }
        for (int i = 0; i < params.length; i += 2) {
            if (!(params[i] instanceof String)) {
                throw new IllegalArgumentException("Keys should be of type String.");
            }
            paramMap.put(params[i], params[i + 1]);
        }
        return paramMap;
    }
}

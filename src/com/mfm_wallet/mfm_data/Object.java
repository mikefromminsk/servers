package com.mfm_wallet.mfm_data;

import com.mfm_wallet.Contract;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Object extends Contract {


    @Override
    protected void run() {

        String path = getRequired("path");
        Long limit = getLongRequired("limit", 100L);

        DataRow row = dataFindPath(path, false);
        if (row == null) error("Path not found");
        Map<String, Object> object = new LinkedHashMap<>();
        /*while (row != null && limit-- > 0) {
            object.put(row.getKey(), row.getValue());
            row = row.getNext();
        }*/

        response.put("object", object);
    }
}

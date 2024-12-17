package com.mfm_wallet.mfm_data;

import com.mfm_wallet.mfm_token.TokenRequests;

import java.util.*;

public class DataUtils extends TokenRequests {
    public static final int MAX_VALUE_SIZE = 256;

    private static final Random random = new Random();
    private static Map<Integer, Map<String, DataRow>> allData = new HashMap<>();
    private Map<Integer, Map<String, DataRow>> data = new HashMap<>();
    private Map<Integer, List<DataRow>> history = new HashMap<>();

    public class DataRow {
        public int id;
        public String value;
        public long time;

        public DataRow(int currentId, String key) {
            this.id = currentId;
            this.value = key;
            time = System.currentTimeMillis();
        }
    }

    public DataRow dataFindPath(String path, boolean create) {
        int currentId = 0;
        DataRow current = null;
        for (String key : path.split("/")) {
            current = allData.getOrDefault(currentId, new HashMap<>()).get(key);
            if (current == null && create) {
                current = new DataRow(currentId, key);
                data.computeIfAbsent(random.nextInt(), k -> new HashMap<>()).put(key, current);
            } else {
                return null;
            }
        }
        return current;
    }

    public void dataSet(String path, String value) {
        if (value.length() > MAX_VALUE_SIZE) error("Value too long");
        dataFindPath(path, true).value = value;
    }

    public String dataGet(String path) {
        DataRow row = dataFindPath(path, false);
        return row == null ? null : row.value;
    }

    public Long dataGetLong(String path) {
        String dataGet = dataGet(path);
        return dataGet == null ? null : Long.parseLong(dataGet);
    }

    public List<String> getHistory(String path, int size) {
        List<String> response = new ArrayList<>();
        DataRow row = dataFindPath(path, false);
        if (row != null) {
            for (DataRow item : history.get(row.id)) {
                response.add(item.value);
                if (response.size() >= size) break;
            }
        }
        return response.reversed();
    }

    public List<Long> getHistoryLong(String path, int size) {
        List<String> strings = getHistory(path, size);
        List<Long> response = new ArrayList<>();
        for (String string : strings) {
            response.add(Long.parseLong(string));
        }
        return response;
    }

    public void commitData() {
        allData.putAll(data);
        data.clear();
    }
}
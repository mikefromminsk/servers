package com.mfm_wallet.mfm_data;

import com.mfm_wallet.mfm_token.TokenRequests;

import java.util.*;

public abstract class DataContract extends TokenRequests {
    public static final int MAX_VALUE_SIZE = 256;
    public static final String GAS_DOMAIN = "usdt";
    public static final String GAS_OWNER = "admin";

    private static final Random random = new Random();
    private static Map<Integer, Map<String, DataRow>> allData = new HashMap<>();
    private Map<Integer, Map<String, DataRow>> data = new HashMap<>();
    private static final Map<Integer, List<DataRow>> history = new HashMap<>();

    public class DataRow {
        public int id;
        public String key;
        public String value;
        public long time;

        public DataRow(int id, String key, String value) {
            this.id = id;
            this.key = key;
            this.value = value;
            time = time();
        }

        public DataRow clone() {
            return new DataRow(id, key, value);
        }
    }

    public DataRow dataFindPath(String path, boolean create) {
        int currentId = 0;
        int parentId = 0;
        DataRow current = null;
        boolean fromDB = false;
        for (String key : path.split("/")) {
            current = allData.getOrDefault(currentId, new HashMap<>()).get(key);
            fromDB = true;
            if (current == null) {
                if (create) {
                    fromDB = false;
                    current = new DataRow(random.nextInt(), key, null);
                    data.computeIfAbsent(currentId, k -> new HashMap<>()).put(key, current);
                } else {
                    return null;
                }
            }
            parentId = currentId;
            currentId = current.id;
        }
        if (current != null && fromDB) {
            current = current.clone();
            if (create)
                data.computeIfAbsent(parentId, k -> new HashMap<>()).put(current.key, current);
        }
        return current;
    }

    public void dataSet(String path, String value) {
        if (value.length() > MAX_VALUE_SIZE) error("Value too long");
        DataRow row = dataFindPath(path, true);
        row.value = value;
    }

    public String dataGet(String path) {
        DataRow row = dataFindPath(path, false);
        return row == null ? null : row.value;
    }

    public Long dataGetLong(String path, Long defaultValue) {
        String data = dataGet(path);
        return data == null ? defaultValue : Long.parseLong(data);
    }

    public Long dataGetLong(String path) {
        String data = dataGet(path);
        return data == null ? null : Long.parseLong(data);
    }

    public List<String> getHistory(String path, int size) {
        List<String> response = new ArrayList<>();
        DataRow row = dataFindPath(path, false);
        if (row != null) {
            List<DataRow> pathHistory = history.get(row.id);
            for (DataRow item : pathHistory) {
                response.add(item.value);
                if (response.size() >= size) break;
            }
        }
        Collections.reverse(response);
        return response;
    }

    public List<Long> getHistoryLong(String path, int size) {
        List<String> strings = getHistory(path, size);
        List<Long> response = new ArrayList<>();
        for (String string : strings) {
            response.add(Long.parseLong(string));
        }
        return response;
    }

    public void commit() {
        Double gas_commission = 0.01 * data.size();
        if (gas_commission > 0) {
            String gas_address = getRequired("gas_address");
            String gas_pass = getRequired("gas_pass");
            if (tokenBalance(GAS_DOMAIN, gas_address) < gas_commission) error("Not enough gas");
            tokenSend(scriptPath, GAS_DOMAIN, gas_address, GAS_OWNER, gas_commission, gas_pass, null);
        }
        for (Integer parentId: data.keySet()) {
            Map<String, DataRow> node = data.get(parentId);
            for (String key : node.keySet()) {
                DataRow row = node.get(key);
                allData.computeIfAbsent(parentId, k -> new HashMap<>()).put(key, row);
                history.computeIfAbsent(row.id, k -> new ArrayList<>()).add(row);
            }
        }
        data.clear();
        super.commit();
    }
}
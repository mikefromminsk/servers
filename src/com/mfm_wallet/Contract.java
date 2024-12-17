package com.mfm_wallet;

import com.mfm_wallet.mfm_data.DataUtils;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class Contract extends DataUtils {
    public String scriptPath;
    protected Map<String, Object> response = new LinkedHashMap<>();
    Map<String, String> params;
    protected abstract void run();

    public String getString(String key) {
        return params.get(key);
    }

    public String getRequired(String key) {
        String value = getString(key);
        if (value == null) {
            error(key + " is empty");
        }
        return value;
    }

    public Double getDouble(String key) {
        return getDouble(key, null);
    }

    public Double getDouble(String key, Double defaultValue) {
        String value = getString(key);
        return value == null ? defaultValue : Double.parseDouble(value);
    }

    public Long getLong(String key, Long defaultValue) {
        String value = getString(key);
        return value == null ? defaultValue : Long.parseLong(value);
    }

    public Long getLongRequired(String key) {
        return getLongRequired(key, null);
    }

    public Long getLongRequired(String key, Long defaultValue) {
        String value = getRequired(key);
        return value == null ? defaultValue : Long.parseLong(value);
    }

    public Double getDoubleRequired(String key) {
        String value = getRequired(key);
        return Double.parseDouble(value);
    }
}

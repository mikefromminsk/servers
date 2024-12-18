package com.mfm_wallet;

import com.mfm_wallet.mfm_token.TokenRequests;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class Contract extends TokenRequests {
    protected Map<String, Object> response = new LinkedHashMap<>();
    protected abstract void run();

    public String scriptPath;
    public Map<String, String> params = new HashMap<>();

    public Contract run(String scriptPath, Map<String, String> params) {
        this.scriptPath = scriptPath;
        this.params = params;
        run();
        return this;
    }

    public String getString(String key, String defaultValue) {
        return params.getOrDefault(key, defaultValue);
    }

    public String getString(String key) {
        return getString(key, null);
    }

    public Double getDouble(String key, Double defaultValue) {
        String value = getString(key);
        return value == null ? defaultValue : Double.parseDouble(value);
    }

    public Double getDouble(String key) {
        return getDouble(key, null);
    }

    public Long getLong(String key, Long defaultValue) {
        String value = getString(key);
        return value == null ? defaultValue : Long.parseLong(value);
    }

    public Long getLong(String key) {
        return getLong(key, null);
    }

    public String getRequired(String key) {
        String value = getString(key);
        if (value == null) {
            error(key + " is empty");
        }
        return value;
    }

    public Long getLongRequired(String key, Long defaultValue) {
        String value = getRequired(key);
        return value == null ? defaultValue : Long.parseLong(value);
    }

    public Long getLongRequired(String key) {
        return getLongRequired(key, null);
    }

    public Double getDoubleRequired(String key, Double defaultValue) {
        String value = getRequired(key);
        return value == null ? defaultValue : Double.parseDouble(value);
    }

    public Double getDoubleRequired(String key) {
        return getDoubleRequired(key, null);
    }

    public String getScriptName() {
        return scriptPath.substring(scriptPath.lastIndexOf('/') + 1, scriptPath.lastIndexOf('.'));
    }
}

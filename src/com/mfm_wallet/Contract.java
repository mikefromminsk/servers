package com.mfm_wallet;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class Contract extends TokenUtils {
    protected Map<String, Object> response = new LinkedHashMap<>();
    protected abstract void run(Map<String, String> params);
}

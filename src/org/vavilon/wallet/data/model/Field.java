package org.vavilon.wallet.data.model;

public class Field {
    public Integer parent;
    public String key;
    public String value;
    public long time;

    public Field(Integer parent, String key, String value, long time) {
        this.parent = parent;
        this.key = key;
        this.value = value;
        this.time = time;
    }
}

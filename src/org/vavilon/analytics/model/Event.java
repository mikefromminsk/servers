package org.vavilon.analytics.model;

import com.metabrain.gdb.model.BigArrayCell;
import com.metabrain.gdb.model.String32;
import com.metabrain.gdb.utils.Bytes;

import java.io.ByteArrayOutputStream;

public class Event implements BigArrayCell {
    public Long time;
    public String name;
    public String value;
    public String user_id;
    public String session;

    @Override
    public byte[] build() {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            bos.write(Bytes.fromLong(time));
            bos.write(Bytes.fromString(name, String32.BYTES));
            bos.write(Bytes.fromString(value, String32.BYTES));
            bos.write(Bytes.fromString(user_id, String32.BYTES));
            bos.write(Bytes.fromString(session, String32.BYTES));
            return bos.toByteArray();
        } catch (Exception e) {
            return new byte[0];
        }
    }

    @Override
    public void parse(Bytes data) {
        time = data.readLong();
        name = data.readString32();
        value = data.readString32();
        user_id = data.readString32();
        session = data.readString32();
    }
}
package org.vavilon.analytics.model;

import com.metabrain.gdb.model.BigArrayCell;
import com.metabrain.gdb.utils.Bytes;

import java.io.ByteArrayOutputStream;

public class EventLink implements BigArrayCell {

    public Long id;
    public Long time;

    public EventLink(Long id, Long time) {
        this.id = id;
        this.time = time;
    }

    @Override
    public byte[] build() {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            bos.write(Bytes.fromLong(id));
            bos.write(Bytes.fromLong(time));
            return bos.toByteArray();
        } catch (Exception e) {
            return new byte[0];
        }
    }

    @Override
    public void parse(Bytes data) {
        id = data.readLong();
        time = data.readLong();
    }
}

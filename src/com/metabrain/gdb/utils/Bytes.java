package com.metabrain.gdb.utils;

import com.metabrain.gdb.model.String32;

import java.nio.*;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class Bytes {

    private byte[] bytes;
    private int position;

    public Bytes() {
        this.bytes = new byte[0];
        this.position = 0;
    }

    public Bytes(byte[] bytes) {
        this.bytes = bytes;
        this.position = 0;
    }

    public byte[] read(int length) {
        byte[] result = new byte[length];
        System.arraycopy(bytes, position, result, 0, length);
        position += length;
        return result;
    }

    public String readString32() {
        return toString(read(String32.BYTES));
    }

    public String readString(int length) {
        return toString(read(length));
    }

    public Double readDouble() {
        return toDouble(read(Double.BYTES));
    }

    public Long readLong() {
        return toLong(read(Long.BYTES));
    }

    public static byte[] fromInt(int value) {
        return ByteBuffer.allocate(4).putInt(value).array();
    }

    public static int toInt(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getInt();
    }

    public static byte[] fromLong(Long value) {
        return ByteBuffer.allocate(Long.BYTES).putLong(value == null ? 0L : value).array();
    }

    public static byte[] fromDouble(Double value) {
        return ByteBuffer.allocate(Double.BYTES).putDouble(value == null ? 0d : value).array();
    }

    public static long toLong(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getLong();
    }

    public static byte[] fromChar(char value) {
        byte[] result = new byte[2];
        result[0] = (byte) ((value & 0xFF00) >> 8);
        result[1] = (byte) (value & 0x00FF);
        return result;
    }

    public static char toChar(byte[] bytes) {
        return (char) (((bytes[0] & 0x00FF) << 8) + (bytes[1] & 0x00FF));
    }

    public static int[] toIntArray(byte[] bytes) {
        IntBuffer intBuffer = ByteBuffer.wrap(bytes).asIntBuffer();
        int result[] = new int[intBuffer.capacity()];
        intBuffer.get(result);
        return result;
    }

    public static byte[] fromIntArray(int[] value) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(value.length * 4);
        IntBuffer intBuffer = byteBuffer.asIntBuffer();
        intBuffer.put(value);
        return byteBuffer.array();
    }

    public static long[] toLongArray(byte[] bytes) {
        LongBuffer longBuffer = ByteBuffer.wrap(bytes).asLongBuffer();
        long result[] = new long[longBuffer.capacity()];
        longBuffer.get(result);
        return result;
    }

    public static byte[] fromLongArray(long[] arr) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(arr.length * Long.BYTES);
        LongBuffer longBuffer = byteBuffer.asLongBuffer();
        longBuffer.put(arr);
        return byteBuffer.array();
    }

    public static byte[] fromLongList(ArrayList<Long> arr) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(arr.size() * Long.BYTES);
        LongBuffer longBuffer = byteBuffer.asLongBuffer();
        for (int i = 0; i < arr.size(); i++)
            longBuffer.put(i, arr.get(i));
        return byteBuffer.array();
    }

    public static byte[] concat(byte[] a, byte[] b) {
        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

    public static byte[] fromCharArray(char[] chars) {
        return Charset.forName("UTF-8").encode(CharBuffer.wrap(chars)).array();
    }

    public static char[] toCharArray(byte[] bytes) {
        String str = new String(bytes);
        return str.toCharArray();
    }

    public static byte[] append(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    public static byte[] fromString(String mask) {
        return mask.getBytes();
    }

    public static byte[] fromString(String string, int minSize) {
        if (string == null) string = "";
        byte[] bytes = new byte[minSize];
        byte[] stringBytes = string.getBytes();
        System.arraycopy(stringBytes, 0, bytes, 0, stringBytes.length);
        return bytes;
    }

    public static String toString(byte[] bytes) {
        return toString(bytes, 0, bytes.length);
    }

    public static String toString(byte[] data, int offset, int size) {
        int firstZeroIndex = size;
        for (int i = 0; i < size; i++) {
            if (data[offset + i] == 0) {
                firstZeroIndex = i;
                break;
            }
        }
        return firstZeroIndex == 0 ? null : new String(data, offset, firstZeroIndex);
    }

    public static Double toDouble(byte[] data, int offset) {
        return ByteBuffer.wrap(data, offset, Double.BYTES).getDouble();
    }

    public static Double toDouble(byte[] data) {
        return ByteBuffer.wrap(data).getDouble();
    }

    public static Long toLong(byte[] data, int offset) {
        return ByteBuffer.wrap(data, offset, Long.BYTES).getLong();
    }

    public long[] readLongArray() {
        return toLongArray(bytes);
    }

    public void addBytes(byte[] bytes) {
        this.bytes = Bytes.concat(this.bytes, bytes);
    }

    public void addString32(String string) {
        addBytes(Bytes.fromString(string, String32.BYTES));
    }

    public void addDouble(Double value) {
        addBytes(Bytes.fromDouble(value));
    }

    public void addLong(Long value) {
        addBytes(Bytes.fromLong(value));
    }

    public byte[] toByteArray() {
        return bytes;
    }
}

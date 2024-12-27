package com.metabrain.gdb;

import com.metabrain.gdb.utils.Bytes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import static com.hatosh.wallet.Utils.time;

public class BigFile {

    final long BLOCK_SIZE = 4096 * 1024;
    String fileName;
    public long fileSize = 0;
    public ArrayList<RandomAccessFile> blocks = new ArrayList<>();

    public BigFile(String fileName) {
        this.fileName = fileName + time();
        getFile(0);
    }

    RandomAccessFile getFile(int index) {
        if (index >= blocks.size()) {
            try {
                File block = new File(fileName + index + ".bin");
                RandomAccessFile partRandomAccessFile = new RandomAccessFile(block, "rw");
                blocks.add(partRandomAccessFile);
                fileSize += block.length();
                return partRandomAccessFile;
            } catch (FileNotFoundException e) {
                return null;
            }
        }
        return blocks.get(index);
    }

    public byte[] read(long start, long length) {
        long end = start + length;
        if (end > fileSize)
            return null;

        int startFileIndex = (int) (start / BLOCK_SIZE);
        int endFileIndex = (int) (end / BLOCK_SIZE);
        if (startFileIndex == endFileIndex) {
            RandomAccessFile readingFile = blocks.get(startFileIndex);
            int startInFile = (int) (start % BLOCK_SIZE);
            return readFromFile(readingFile, startInFile, (int) length);
        } else {
            RandomAccessFile firstFile = blocks.get(startFileIndex);
            RandomAccessFile secondFile = blocks.get(endFileIndex);
            int lengthInSecondFile = (int) (end % BLOCK_SIZE);
            int lengthInFirstFile = (int) (length - lengthInSecondFile);
            int startInFirstFile = (int) (start % BLOCK_SIZE);
            int startInSecondFile = 0;
            byte[] dataFromFirstFile = readFromFile(firstFile, startInFirstFile, lengthInFirstFile);
            byte[] dataFromSecondFile = readFromFile(secondFile, startInSecondFile, lengthInSecondFile);
            return Bytes.concat(dataFromFirstFile, dataFromSecondFile);
        }
    }

    public void write(long start, byte[] data) {
        long length = data.length;
        long end = start + length;
        if (start > fileSize)
            return;

        int startFileIndex = (int) (start / BLOCK_SIZE);
        int endFileIndex = (int) (end / BLOCK_SIZE);

        RandomAccessFile firstWriteFile = getFile(startFileIndex);
        RandomAccessFile secondWriteFile = getFile(endFileIndex);

        if (start == fileSize)
            fileSize += data.length;

        if (startFileIndex == endFileIndex) {
            int startInFile = (int) (start - startFileIndex * BLOCK_SIZE);
            whiteToFile(firstWriteFile, startInFile, data);
        } else {
            int lengthInSecondFile = (int) (end % BLOCK_SIZE);
            int lengthInFirstFile = (int) (length - lengthInSecondFile);
            int startInFirstFile = (int) (start % BLOCK_SIZE);
            int startInSecondFile = 0;
            byte[] dataToFirstFile = new byte[lengthInFirstFile];
            byte[] dataToSecondFile = new byte[lengthInSecondFile];
            System.arraycopy(data, 0, dataToFirstFile, 0, lengthInFirstFile);
            System.arraycopy(data, lengthInFirstFile, dataToSecondFile, 0, lengthInSecondFile);
            whiteToFile(firstWriteFile, startInFirstFile, dataToFirstFile);
            whiteToFile(secondWriteFile, startInSecondFile, dataToSecondFile);
        }
    }

    void whiteToFile(RandomAccessFile file, long offset, byte[] data) {
        try {
            file.seek(offset);
            file.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    byte[] readFromFile(RandomAccessFile file, long offset, int length) {
        try {
            file.seek(offset);
            byte[] data = new byte[length];
            file.read(data);
            return data;
        } catch (IOException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    public long add(byte[] data) {
        write(fileSize, data);
        return fileSize;
    }
}

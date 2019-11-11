package com.davidsoft.io;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public final class SimpleIO {

    public static void writeByte(OutputStream out, byte value) throws IOException {
        out.write(value & 0xFF);
    }

    public static byte readByte(InputStream in) throws IOException {
        int ch1 = in.read();
        if (ch1 < 0) {
            throw new EOFException();
        }
        return (byte)ch1;
    }

    public static int readByteForInt(InputStream in) throws IOException {
        int ch1 = in.read();
        if (ch1 < 0) {
            throw new EOFException();
        }
        return ch1;
    }

    public static void writeBoolean(OutputStream out, boolean value) throws IOException {
        writeByte(out, value ? (byte)1 : (byte)0);
    }

    public static boolean readBoolean(InputStream in) throws IOException {
        return readByte(in) != 0;
    }

    public static void writeShort(OutputStream out, short value) throws IOException {
        out.write(value & 0xFF);
        out.write((value >>> 8) & 0xFF);
    }

    public static short readShort(InputStream in) throws IOException {
        int ch1 = in.read();
        int ch2 = in.read();
        if ((ch1 | ch2) < 0) {
            throw new EOFException();
        }
        return (short) ((ch2 << 8) + ch1);
    }

    public static void writeInt(OutputStream out, int value) throws IOException {
        out.write(value & 0xFF);
        out.write((value >>>  8) & 0xFF);
        out.write((value >>> 16) & 0xFF);
        out.write((value >>> 24) & 0xFF);
    }

    public static int readInt(InputStream in) throws IOException {
        int ch1 = in.read();
        int ch2 = in.read();
        int ch3 = in.read();
        int ch4 = in.read();
        if ((ch1 | ch2 | ch3 | ch4) < 0) {
            throw new EOFException();
        }
        return ((ch4 << 24) + (ch3 << 16) + (ch2 << 8) + ch1);
    }

    public static void writeLong(OutputStream out, long value) throws IOException {
        out.write((int) (value & 0xFF));
        out.write((int) ((value >>>  8) & 0xFF));
        out.write((int) ((value >>> 16) & 0xFF));
        out.write((int) ((value >>> 24) & 0xFF));
        out.write((int) ((value >>> 32) & 0xFF));
        out.write((int) ((value >>> 40) & 0xFF));
        out.write((int) ((value >>> 48) & 0xFF));
        out.write((int) ((value >>> 56) & 0xFF));
    }

    public static long readLong(InputStream in) throws IOException {
        long ch1 = in.read();
        long ch2 = in.read();
        long ch3 = in.read();
        long ch4 = in.read();
        long ch5 = in.read();
        long ch6 = in.read();
        long ch7 = in.read();
        long ch8 = in.read();
        if ((ch1 | ch2 | ch3 | ch4 | ch5 | ch6 | ch7 | ch8) < 0) {
            throw new EOFException();
        }
        return ((ch8 << 56) + (ch7 << 48) + (ch6 << 40) + (ch5 << 32) + (ch4 << 24) + (ch3 << 16) + (ch2 << 8) + ch1);
    }

    public static void writeString(OutputStream out, String string) throws IOException {
        out.write(string.getBytes(StandardCharsets.UTF_8));
    }

    public static void writeStringWithLength(OutputStream out, String string) throws IOException {
        byte[] stringBytes = string.getBytes(StandardCharsets.UTF_8);
        writeInt(out, stringBytes.length);
        out.write(string.getBytes(StandardCharsets.UTF_8));
    }

    public static String readString(InputStream in, int length) throws IOException {
        if (length == 0) {
            return "";
        }
        byte[] buffer = new byte[length];
        if (in.read(buffer) != length) {
            throw new EOFException();
        }
        return new String(buffer, 0, length, StandardCharsets.UTF_8);
    }

    public static String readStringWithLength(InputStream in) throws IOException {
        int length = readInt(in);
        if (length == 0) {
            return "";
        }
        byte[] buffer = new byte[length];
        length = 0;
        int c;
        while (length < buffer.length) {
            c = in.read(buffer, length, buffer.length - length);
            if (c == -1) {
                return null;
            }
            length += c;
        }
        return new String(buffer, StandardCharsets.UTF_8);
    }

    public static void readAtLength(InputStream in, byte[] data, int offset, int len) throws IOException {
        int readCount = 0;
        int read;
        while (readCount < len) {
            read = in.read(data, offset + readCount, len - readCount);
            if (read == -1) {
                throw new EOFException();
            }
            readCount += read;
        }
    }

    public static void writeCString(OutputStream out, String string) throws IOException {
        out.write(string.getBytes());
        out.write(0);
    }

    public static String readCString(InputStream in) throws IOException {
        return readCString(in, new ByteArrayOutputStream());
    }

    public static String readCString(InputStream in, ByteArrayOutputStream buffer) throws IOException {
        int b;
        buffer.reset();
        while ((b = in.read()) != 0) {
            if (b == -1) {
                return null;
            }
            buffer.write(b);
        }
        return new String(buffer.toByteArray(), 0, buffer.size());
    }
}
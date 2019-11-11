package com.davidsoft.io;

import java.io.IOException;
import java.io.OutputStream;

public class CombinedOutputStream extends OutputStream {

    //有一个抛异常则抛异常，不继续执行
    public static final int EXCEPTION_THROW_MODE_ONE = 0;
    //全部执行，抛出产生的第一个异常
    public static final int EXCEPTION_THROW_MODE_ALL_THROW_FIRST = 1;
    //全部执行，抛出产生的最后一个异常
    public static final int EXCEPTION_THROW_MODE_ALL_THROW_LAST = 2;
    //全部执行，不抛出任何异常，即使产生了异常
    public static final int EXCEPTION_THROW_MODE_DISABLED = 3;

    private OutputStream[] dests;
    private IOException[] exceptions;
    private int exceptionCount;

    private int exceptionThrowMode;

    public CombinedOutputStream(OutputStream[] dests) {
        this(dests, EXCEPTION_THROW_MODE_ONE);
    }

    public CombinedOutputStream(OutputStream[] dests, int exceptionThrowMode) {
        this.dests = dests;
        this.exceptionThrowMode = exceptionThrowMode;
        exceptions = new IOException[dests.length];
    }

    @Override
    public void write(int b) throws IOException {
        int i;
        int exceptionPosition = 0;
        clearExceptions();
        for (i = 0; i < dests.length; i++) {
            try {
                dests[i].write(b);
            }
            catch (IOException e) {
                exceptions[i] = e;
                exceptionCount++;
                switch (exceptionThrowMode) {
                    case EXCEPTION_THROW_MODE_ONE:
                        throw new IOException("无法向此流中写入数据", e);
                    case EXCEPTION_THROW_MODE_ALL_THROW_FIRST:
                        if (exceptionCount == 1) {
                            exceptionPosition = i;
                        }
                        break;
                    case EXCEPTION_THROW_MODE_ALL_THROW_LAST:
                        exceptionPosition = i;
                        break;
                }
            }
        }
        if (exceptionCount > 0) {
            switch (exceptionThrowMode) {
                case EXCEPTION_THROW_MODE_ALL_THROW_FIRST:
                case EXCEPTION_THROW_MODE_ALL_THROW_LAST:
                    throw new IOException("无法向此流中写入数据", exceptions[exceptionPosition]);
            }
        }
    }

    @Override
    public void write(byte b[]) throws IOException {
        int i;
        int exceptionPosition = 0;
        clearExceptions();
        for (i = 0; i < dests.length; i++) {
            try {
                dests[i].write(b);
            }
            catch (IOException e) {
                exceptions[i] = e;
                exceptionCount++;
                switch (exceptionThrowMode) {
                    case EXCEPTION_THROW_MODE_ONE:
                        throw new IOException("无法向此流中写入数据", e);
                    case EXCEPTION_THROW_MODE_ALL_THROW_FIRST:
                        if (exceptionCount == 1) {
                            exceptionPosition = i;
                        }
                        break;
                    case EXCEPTION_THROW_MODE_ALL_THROW_LAST:
                        exceptionPosition = i;
                        break;
                }
            }
        }
        if (exceptionCount > 0) {
            switch (exceptionThrowMode) {
                case EXCEPTION_THROW_MODE_ALL_THROW_FIRST:
                case EXCEPTION_THROW_MODE_ALL_THROW_LAST:
                    throw new IOException("无法向此流中写入数据", exceptions[exceptionPosition]);
            }
        }
    }

    @Override
    public void write(byte b[], int off, int len) throws IOException {
        int i;
        int exceptionPosition = 0;
        clearExceptions();
        for (i = 0; i < dests.length; i++) {
            try {
                dests[i].write(b, off, len);
            }
            catch (IOException e) {
                exceptions[i] = e;
                exceptionCount++;
                switch (exceptionThrowMode) {
                    case EXCEPTION_THROW_MODE_ONE:
                        throw new IOException("无法向此流中写入数据", e);
                    case EXCEPTION_THROW_MODE_ALL_THROW_FIRST:
                        if (exceptionCount == 1) {
                            exceptionPosition = i;
                        }
                        break;
                    case EXCEPTION_THROW_MODE_ALL_THROW_LAST:
                        exceptionPosition = i;
                        break;
                }
            }
        }
        if (exceptionCount > 0) {
            switch (exceptionThrowMode) {
                case EXCEPTION_THROW_MODE_ALL_THROW_FIRST:
                case EXCEPTION_THROW_MODE_ALL_THROW_LAST:
                    throw new IOException("无法向此流中写入数据", exceptions[exceptionPosition]);
            }
        }
    }

    @Override
    public void flush() throws IOException {
        int i;
        int exceptionPosition = 0;
        clearExceptions();
        for (i = 0; i < dests.length; i++) {
            try {
                dests[i].flush();
            }
            catch (IOException e) {
                exceptions[i] = e;
                exceptionCount++;
                switch (exceptionThrowMode) {
                    case EXCEPTION_THROW_MODE_ONE:
                        throw new IOException("无法向此流中写入数据", e);
                    case EXCEPTION_THROW_MODE_ALL_THROW_FIRST:
                        if (exceptionCount == 1) {
                            exceptionPosition = i;
                        }
                        break;
                    case EXCEPTION_THROW_MODE_ALL_THROW_LAST:
                        exceptionPosition = i;
                        break;
                }
            }
        }
        if (exceptionCount > 0) {
            switch (exceptionThrowMode) {
                case EXCEPTION_THROW_MODE_ALL_THROW_FIRST:
                case EXCEPTION_THROW_MODE_ALL_THROW_LAST:
                    throw new IOException("无法向此流中写入数据", exceptions[exceptionPosition]);
            }
        }
    }

    @Override
    public void close() throws IOException {
        int i;
        int exceptionPosition = 0;
        clearExceptions();
        for (i = 0; i < dests.length; i++) {
            try {
                dests[i].close();
            }
            catch (IOException e) {
                exceptions[i] = e;
                exceptionCount++;
                switch (exceptionThrowMode) {
                    case EXCEPTION_THROW_MODE_ONE:
                        throw new IOException("无法向此流中写入数据", e);
                    case EXCEPTION_THROW_MODE_ALL_THROW_FIRST:
                        if (exceptionCount == 1) {
                            exceptionPosition = i;
                        }
                        break;
                    case EXCEPTION_THROW_MODE_ALL_THROW_LAST:
                        exceptionPosition = i;
                        break;
                }
            }
        }
        if (exceptionCount > 0) {
            switch (exceptionThrowMode) {
                case EXCEPTION_THROW_MODE_ALL_THROW_FIRST:
                case EXCEPTION_THROW_MODE_ALL_THROW_LAST:
                    throw new IOException("无法向此流中写入数据", exceptions[exceptionPosition]);
            }
        }
    }

    private void clearExceptions() {
        if (exceptionCount == 0) {
            return;
        }
        for (int i = 0; i < exceptions.length; i++) {
            exceptions[i] = null;
        }
        exceptionCount = 0;
    }

    public IOException getException(int position) {
        return exceptions[position];
    }

    public int getExceptionCount() {
        return exceptionCount;
    }

    public int getExceptionThrowMode() {
        return exceptionThrowMode;
    }

    public void setExceptionThrowMode(int exceptionThrowMode) {
        this.exceptionThrowMode = exceptionThrowMode;
    }
}
package com.davidsoft.natural.chinese;

/**
 * 整数对
 */
public final class PairInt implements Comparable<PairInt> {

    public int a;
    public int b;

    public PairInt() {};

    public PairInt(int a, int b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public String toString() {
        return "(" + a + ", " + b + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PairInt) {
            PairInt another = (PairInt) obj;
            return another.a == a && another.b == b;
        }
        return false;
    }

    @Override
    public int compareTo(PairInt o) {
        if (a > o.a) {
            return 1;
        }
        if (a < o.a) {
            return -1;
        }
        if (b > o.b) {
            return 1;
        }
        if (b < o.b) {
            return -1;
        }
        return 0;
    }
}

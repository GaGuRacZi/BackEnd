package com.gaguraczi.paw.domain.rag.support;

public final class PgVectorLiteral {

    private PgVectorLiteral() {
    }

    public static String of(float[] values) {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("embedding must not be empty");
        }
        StringBuilder sb = new StringBuilder(values.length * 8);
        sb.append('[');
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(values[i]);
        }
        sb.append(']');
        return sb.toString();
    }
}

package com.suibe.suibe_mma.domain.able;

public interface NotSetNullable<T> {
    T notSetNull(String column) throws IllegalAccessException;
    T notSetNull(String[] columns) throws IllegalAccessException;
}

package com.exasol.adapter.sql;

public abstract class SqlPredicate extends SqlNode {
    private final Predicate function;

    public SqlPredicate(final Predicate function) {
        this.function = function;
    }

    public Predicate getFunction() {
        return function;
    }
}

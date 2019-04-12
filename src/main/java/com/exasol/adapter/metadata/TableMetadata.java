package com.exasol.adapter.metadata;

import java.util.List;
import java.util.Objects;

import com.google.common.base.MoreObjects;

/**
 * Represents the metadata of an EXASOL table.
 */
public class TableMetadata {
    private final String name;
    private final String adapterNotes;
    private final List<ColumnMetadata> columns;
    private final String comment;

    public TableMetadata(final String name, final String adapterNotes, final List<ColumnMetadata> columns,
            final String comment) {
        this.name = name;
        this.adapterNotes = adapterNotes;
        this.columns = columns;
        this.comment = comment;
        if (this.columns.isEmpty()) {
            throw new IllegalArgumentException(
                    "A table without columns was encountered: " + this.name + ". This is not supported."
                            + " Please check if this table has columns. If the table does have columns, the dialect "
                            + "probably does not properly handle the data types of the columns.");
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }
        final TableMetadata that = (TableMetadata) o;
        return Objects.equals(this.name, that.name) && Objects.equals(this.adapterNotes, that.adapterNotes)
                && Objects.equals(this.columns, that.columns) && Objects.equals(this.comment, that.comment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.adapterNotes, this.columns, this.comment);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("name", this.name).add("adapterNotes", this.adapterNotes)
                .add("columns", this.columns).add("comment", this.comment).toString();
    }

    public String getName() {
        return this.name;
    }

    public String getAdapterNotes() {
        return this.adapterNotes;
    }

    public List<ColumnMetadata> getColumns() {
        return this.columns;
    }

    public boolean hasComment() {
        return !this.comment.isEmpty();
    }

    public String getComment() {
        return this.comment;
    }

    /**
     * Create a human-readable short description of the table
     *
     * @return short description
     */
    public String describe() {
        boolean first = true;
        final StringBuilder builder = new StringBuilder(this.name);
        builder.append(" (");
        for (final ColumnMetadata column : this.columns) {
            if (!first) {
                builder.append(", ");
            }
            builder.append(column.getName());
            builder.append(" ");
            builder.append(column.getType().toString());
            first = false;
        }
        builder.append(")");
        return builder.toString();
    }
}

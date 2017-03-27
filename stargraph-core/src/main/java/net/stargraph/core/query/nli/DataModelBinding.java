package net.stargraph.core.query.nli;

import java.util.Objects;

public final class DataModelBinding {
    private DataModelType modelType;
    private String term;
    private String placeHolder;

    public DataModelBinding(DataModelType modelType, String term, String placeHolder) {
        this.modelType = Objects.requireNonNull(modelType);
        this.term = Objects.requireNonNull(term);
        this.placeHolder = Objects.requireNonNull(placeHolder);
    }

    public DataModelType getModelType() {
        return modelType;
    }

    public String getTerm() {
        return term;
    }

    public String getPlaceHolder() {
        return placeHolder;
    }

    @Override
    public String toString() {
        return "DataModelBinding{" +
                "term='" + term + '\'' +
                ", placeHolder='" + placeHolder + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataModelBinding that = (DataModelBinding) o;
        return modelType == that.modelType &&
                Objects.equals(term, that.term) &&
                Objects.equals(placeHolder, that.placeHolder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(modelType, term, placeHolder);
    }
}

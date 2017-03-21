package net.stargraph.core.qa.nli;

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
}

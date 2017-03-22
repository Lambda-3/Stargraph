package net.stargraph.core.query.nli;

import java.util.Objects;

public final class DataModelTypePattern {

	private String pattern;
	private DataModelType dataModelType;

	public DataModelTypePattern(String pattern, DataModelType dataModelType) {
		this.pattern = Objects.requireNonNull(pattern);
		this.dataModelType = Objects.requireNonNull(dataModelType);
	}

	public String getPattern() {
		return pattern;
	}

	public DataModelType getDataModelType() {
		return dataModelType;
	}

	public boolean isLexical() {
        return dataModelType == DataModelType.TYPE
                || dataModelType == DataModelType.OPERATION || dataModelType == DataModelType.STOP;
    }

    @Override
    public String toString() {
        return "DataModelTypePattern{" +
                "pattern='" + pattern + '\'' +
                ", dataModelType=" + dataModelType +
                '}';
    }
}

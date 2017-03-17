package net.stargraph.core.qa.nli;

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

    @Override
    public String toString() {
        return "DataModelTypePattern{" +
                "pattern='" + pattern + '\'' +
                ", dataModelType=" + dataModelType +
                '}';
    }
}

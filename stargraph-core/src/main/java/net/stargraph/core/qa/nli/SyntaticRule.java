package net.stargraph.core.qa.nli;

import java.util.Objects;

public final class SyntaticRule {

	private String syntacticPattern;
	private DataModelType dataModelType;

	public SyntaticRule(String syntacticPattern, DataModelType dataModelType) {
		this.syntacticPattern = Objects.requireNonNull(syntacticPattern);
		this.dataModelType = Objects.requireNonNull(dataModelType);
	}

	public String getSyntacticPattern() {
		return syntacticPattern;
	}

	public void setSyntacticPattern(String syntacticPattern) {
		this.syntacticPattern = syntacticPattern;
	}

	public DataModelType getDataModelType() {
		return dataModelType;
	}

	public void setDataModelType(DataModelType dataModelType) {
		this.dataModelType = dataModelType;
	}
}

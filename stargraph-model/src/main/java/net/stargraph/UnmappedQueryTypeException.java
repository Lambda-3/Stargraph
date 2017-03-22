package net.stargraph;

public final class UnmappedQueryTypeException extends StarGraphException {
    public UnmappedQueryTypeException(String query) {
        super("Unmapped Query Type: '" + query + "'");
    }
}

package net.stargraph;

public final class UnmappedQueryTypeExcpetion extends StarGraphException {
    public UnmappedQueryTypeExcpetion(String query) {
        super("Unmapped Query Type: '" + query + "'");
    }
}

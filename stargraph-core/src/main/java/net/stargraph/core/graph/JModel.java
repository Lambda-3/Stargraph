package net.stargraph.core.graph;

import net.stargraph.model.GraphModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.shared.AddDeniedException;

/**
 * A simple wrapper for Jena's graph model
 */
public class JModel implements GraphModel {
    private Model model;

    public JModel() {
        this(ModelFactory.createDefaultModel());
    }

    public JModel(Model model) {
        this.model = model;
    }

    public void add(JModel graphModel) {
        try {
            model.add(graphModel.model);
        } catch (AddDeniedException e) {
            // currently, this is the case for HDT graph models
            throw new UnsupportedOperationException("Failed to extend graph model.");
        }
    }

    public long size() {
        return model.size();
    }

    public Model getModel() {
        return model;
    }
}

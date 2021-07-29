package com.linkedpipes.etl.storage.template.reference;

import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;

import java.util.ArrayList;
import java.util.List;

/**
 * Full definition of a reference template data structure.
 */
public class ReferenceDefinition {

    public Resource resource;

    public Resource template;

    public Literal prefLabel;

    public Literal description;

    public Literal note;

    public Value color;

    public List<Literal> tags = new ArrayList<>(4);

    /**
     * First IRI assigned to this template, used to map template between
     * instances.
     */
    public Resource knownAs;

    /**
     * IRI of the template root.
     */
    public Resource root;

    public Resource configurationGraph;

    public Literal version;

    public static ReferenceDefinition copy(ReferenceDefinition other) {
        ReferenceDefinition result = new ReferenceDefinition();
        result.resource = other.resource;
        result.template = other.template;
        result.prefLabel = other.prefLabel;
        result.description = other.description;
        result.note = other.note;
        result.color = other.color;
        result.tags.addAll(other.tags);
        result.knownAs = other.knownAs;
        result.root = other.root;
        result.configurationGraph = other.configurationGraph;
        result.version = other.version;
        return result;
    }

}
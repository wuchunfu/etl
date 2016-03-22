package com.linkedpipes.etl.executor.event;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LINKEDPIPES;
import com.linkedpipes.etl.executor.api.v1.rdf.StatementWriter;

/**
 *
 * @author Škoda Petr
 */
class ComponentFinished extends AbstractEvent
        implements com.linkedpipes.etl.executor.api.v1.event.ComponentFinished {

    private final String componentIri;



    ComponentFinished(String componentIri) {
        super(LINKEDPIPES.EVENTS.COMPONENT_END, "Component completed.");
        this.componentIri = componentIri;
    }

    @Override
    public void write(StatementWriter writer) {
        super.write(writer);
        writer.addUri(iri, LINKEDPIPES.HAS_COMPONENT, componentIri);
    }

    @Override
    public String getComponentUri() {
        return componentIri;
    }

}

package com.linkedpipes.etl.storage.pipeline;

import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.Configuration;
import com.linkedpipes.etl.storage.pipeline.transformation.TransformationFacade;
import com.linkedpipes.etl.storage.rdf.PojoLoader;
import com.linkedpipes.etl.storage.rdf.RdfUtils;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PipelineRepository {

    @FunctionalInterface
    public interface OnLoad {

        void action(PipelineRef pipeline, Collection<Statement> rdf);

    }

    private static final Logger LOG =
            LoggerFactory.getLogger(PipelineRepository.class);

    private final Configuration configuration;

    private final TransformationFacade transformation;

    private final Map<String, PipelineRef> pipelines = new HashMap<>();

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    @Autowired
    public PipelineRepository(
            Configuration configuration, TransformationFacade transformation) {
        this.configuration = configuration;
        this.transformation = transformation;
    }

    public void loadPipelines(OnLoad onLoad) {
        File pipelineDirectory = configuration.getPipelinesDirectory();
        if (!pipelineDirectory.exists()) {
            pipelineDirectory.mkdirs();
        }
        File[] files = pipelineDirectory.listFiles();
        if (files == null) {
            LOG.warn("Pipeline directory does not exist.");
            return;
        }
        for (File file : files) {
            if (!file.isFile() || isBackupFile(file) || isSwapFile(file)) {
                continue;
            }
            try {
                loadPipeline(file, onLoad);
            } catch (Exception ex) {
                LOG.error("Can't load pipeline: {}", file, ex);
            }
        }
    }

    private boolean isBackupFile(File file) {
        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(".backup");
    }

    private boolean isSwapFile(File file) {
        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(".swp");
    }

    /**
     * Check version and if necessary perform migration.
     */
    private void loadPipeline(File file, OnLoad onLoad) throws OperationFailed {
        PipelineLoader loader = new PipelineLoader(transformation);
        PipelineRef pipeline = loader.load(file);
        onLoad.action(pipeline, loader.getPipelineRdf());
        createPipelineReference(pipeline);
        pipelines.put(pipeline.getIri(), pipeline);
    }

    /**
     * Create pipeline reference object and set it to pipeline.
     */
    private void createPipelineReference(PipelineRef pipeline) {
        List<Statement> referenceRdf = new ArrayList<>();
        IRI pipelineIri = valueFactory.createIRI(pipeline.getIri());
        referenceRdf.add(valueFactory.createStatement(
                pipelineIri, RDF.TYPE, PipelineRef.TYPE, pipelineIri));
        for (Value label : pipeline.getInfo().getLabels()) {
            referenceRdf.add(valueFactory.createStatement(
                    pipelineIri, SKOS.PREF_LABEL, label, pipelineIri));
        }
        IRI tagIri = valueFactory.createIRI(LP_PIPELINE.HAS_TAG);
        for (Value tag : pipeline.getInfo().getTags()) {
            referenceRdf.add(valueFactory.createStatement(
                    pipelineIri, tagIri, tag, pipelineIri));
        }
        pipeline.setReferenceRdf(referenceRdf);
    }

    public Map<String, PipelineRef> getPipelines() {
        return Collections.unmodifiableMap(pipelines);
    }

    public void update(PipelineRef pipeline, Collection<Statement> rdf)
            throws OperationFailed {
        updatePipelineInfo(pipeline, rdf);
        createPipelineReference(pipeline);
        writePipelineToDisk(pipeline, rdf);
    }

    private void updatePipelineInfo(
            PipelineRef pipeline, Collection<Statement> rdf)
            throws OperationFailed {
        PipelineInfo info = new PipelineInfo();
        try {
            PojoLoader.loadOfType(rdf, PipelineRef.TYPE, info);
            pipeline.setInfo(info);
        } catch (PojoLoader.CantLoadException ex) {
            throw new OperationFailed("Can't update pipeline.", ex);
        }
    }

    private void writePipelineToDisk(
            PipelineRef pipeline, Collection<Statement> rdf)
            throws OperationFailed {
        try {
            RdfUtils.atomicWrite(pipeline.getFile(), RDFFormat.TRIG, rdf);
        } catch (RdfUtils.RdfException ex) {
            throw new OperationFailed(
                    "Can't save pipeline: {}", pipeline.getFile(), ex);
        }
    }

    public void delete(PipelineRef pipeline) {
        pipeline.getFile().delete();
        pipelines.remove(pipeline.getIri());
    }

    public Collection<Statement> getPipelineRdf(PipelineRef pipeline)
            throws StorageException {
        try {
            return RdfUtils.read(pipeline.getFile());
        } catch (RdfUtils.RdfException ex) {
            throw new StorageException("Can't read file.", ex);
        }
    }

    public PipelineRef createPipeline(IRI iri, Collection<Statement> rdf)
            throws OperationFailed {
        File file = getPipelineFile(iri);
        PipelineRef pipeline = new PipelineRef(file, null);
        update(pipeline, rdf);
        pipelines.put(iri.stringValue(), pipeline);
        return pipeline;
    }

    private File getPipelineFile(IRI iri) {
        String fileName = iri.getLocalName() + ".trig";
        return new File(configuration.getPipelinesDirectory(), fileName);
    }

}
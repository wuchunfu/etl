package com.linkedpipes.etl.component.test;

import com.linkedpipes.etl.component.api.ExecutionFailed;
import com.linkedpipes.etl.component.api.service.ExceptionFactory;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Petr Škoda
 */
public class MockedExceptionFactory implements ExceptionFactory {

    private static final Logger LOG
            = LoggerFactory.getLogger(MockedExceptionFactory.class);

    @Override
    public ExecutionFailed failed(String message, Object... args) {
        LOG.error("Exception: " + message, args);
        return new ExecutionFailed(Arrays.asList(
                new ExecutionFailed.Message(message, "en")),
                args);
    }

    @Override
    public ExecutionFailed invalidConfigurationProperty(
            String propertyIri, String message, Object... args) {
        LOG.error("Invalid configuration property: {}", propertyIri);
        return new ExecutionFailed(Arrays.asList(
                new ExecutionFailed.Message(
                        "Invalid configuration field.", "en")),
                args);
    }

    @Override
    public ExecutionFailed missingConfigurationProperty(
            String propertyIri) {
        LOG.error("Missing configuration property: {}", propertyIri);
        return new ExecutionFailed(Arrays.asList(
                new ExecutionFailed.Message(
                        "Missing configuration field.", "en")));
    }

}

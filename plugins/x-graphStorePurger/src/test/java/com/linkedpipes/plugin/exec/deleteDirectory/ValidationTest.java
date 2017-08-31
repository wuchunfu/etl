package com.linkedpipes.plugin.exec.deleteDirectory;

import com.linkedpipes.etl.test.suite.TestConfigurationDescription;
import com.linkedpipes.plugin.exec.graphstorepurger.GraphStorePurgerConfiguration;
import org.junit.Test;

public class ValidationTest {

    @Test
    public void verifyConfigurationDescription() throws Exception {
        final TestConfigurationDescription test =
                new TestConfigurationDescription();
        test.test(GraphStorePurgerConfiguration.class);
    }

}
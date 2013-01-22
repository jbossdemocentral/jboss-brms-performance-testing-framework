package org.jboss.brms.test.util;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Properties;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Qualifier;

import org.apache.log4j.Logger;

/**
 * This class uses CDI to alias Java EE resources, such as the persistence context, to CDI beans.
 */
public class Resources {
    private static final String GUVNOR_CONFIG_FILE = "guvnor.properties";

    private final Properties guvnorConfiguration = new Properties();

    @Produces
    public Logger produceLogger(final InjectionPoint injectionPoint) {
        return Logger.getLogger(injectionPoint.getMember().getDeclaringClass());
    }

    @Produces
    @GuvnorConfig
    public Properties produceSmsConfiguration() {
        if (guvnorConfiguration.isEmpty()) {
            try {
                guvnorConfiguration.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(GUVNOR_CONFIG_FILE));
            } catch (final IOException ioEx) {
                Logger.getLogger(Resources.class).fatal("Unable to load Guvnor configuration file.", ioEx);
            }

        }
        return guvnorConfiguration;
    }

    // Custom qualifiers for different Properties producers.

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER })
    public @interface GuvnorConfig {
    }
}

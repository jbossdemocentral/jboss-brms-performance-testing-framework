package org.jboss.brms.test.service.data;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Set;
import java.util.jar.JarFile;

import org.apache.log4j.Logger;
import org.jboss.vfs.VirtualFile;
import org.reflections.Reflections;
import org.reflections.ReflectionsException;
import org.reflections.vfs.SystemDir;
import org.reflections.vfs.Vfs;
import org.reflections.vfs.ZipDir;

/**
 * Uses the <a href="https://code.google.com/p/reflections/">Reflections library</a> to find {@link ProcessInstanceDataProvider} implementations.
 */
public final class DataProviderLookup {
    private static final Logger LOGGER = Logger.getLogger(DataProviderLookup.class);

    // Search inside the "org.jboss.brms.test.service.data" package.
    private static Reflections reflections;

    static {
        Vfs.addDefaultURLTypes(new Vfs.UrlType() {
            @Override
            public boolean matches(final URL url) {
                return url.getProtocol().equals("vfs");
            }

            @Override
            public Vfs.Dir createDir(final URL url) {
                VirtualFile content;
                try {
                    content = (VirtualFile) url.openConnection().getContent();
                } catch (final Throwable e) {
                    throw new ReflectionsException("could not open url connection as VirtualFile [" + url + "]", e);
                }

                Vfs.Dir dir = null;
                try {
                    dir = createDir(new java.io.File(content.getPhysicalFile().getParentFile(), content.getName()));
                } catch (final IOException e) { /* continue */
                }
                if (dir == null) {
                    try {
                        dir = createDir(content.getPhysicalFile());
                    } catch (final IOException e) { /* continue */
                    }
                }
                return dir;
            }

            Vfs.Dir createDir(final java.io.File file) {
                try {
                    return file.exists() && file.canRead() ? file.isDirectory() ? new SystemDir(file) : new ZipDir(new JarFile(file)) : null;
                } catch (final IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        });

        // Search inside the "org.jboss.brms.test.service.data" package.
        reflections = new Reflections(ProcessInstanceDataProvider.class.getPackage().getName());
    }

    /**
     * Get an instance of a class implementing {@link ProcessInstanceDataProvider}, for whichthe given process ID matches.
     * 
     * @param processId
     *            The ID of the process for which runtime data is required.
     * @return The intended data provider, or <code>null</code> if none was available for the given process.
     */
    public static ProcessInstanceDataProvider getDataProvider(final String processId) {
        ProcessInstanceDataProvider dataProvider = null;

        final Set<Class<? extends ProcessInstanceDataProvider>> subTypes = reflections.getSubTypesOf(ProcessInstanceDataProvider.class);

        for (final Class<? extends ProcessInstanceDataProvider> subType : subTypes) {
            if (!subType.isInterface() && !Modifier.isAbstract(subType.getModifiers())) {
                try {
                    final ProcessInstanceDataProvider newInstance = subType.newInstance();
                    if (newInstance.getProcessId().equals(processId)) {
                        dataProvider = newInstance;
                        break;
                    }
                } catch (final InstantiationException instEx) {
                    LOGGER.error("Unable to instantiate data provider:", instEx);
                } catch (final IllegalAccessException iaEx) {
                    LOGGER.error("Not allowed to instantiate data provider:", iaEx);
                }
            }
        }

        return dataProvider;
    }
}

package com.exasol.adapter;

import java.util.NoSuchElementException;
import java.util.ServiceLoader;
import java.util.logging.Logger;

import com.exasol.ExaMetadata;
import com.exasol.adapter.request.AdapterRequest;
import com.exasol.adapter.request.LoggingConfiguration;
import com.exasol.adapter.request.parser.RequestParser;
import com.exasol.errorreporting.ExaError;
import com.exasol.logging.RemoteLogManager;
import com.exasol.logging.VersionCollector;

/**
 * This class is the main entry point for calls to a Virtual Schema. It sets up the application and delegate the control
 * to the {@link AdapterCallExecutor}.
 */
public final class RequestDispatcher {
    private static final Logger LOGGER = Logger.getLogger(RequestDispatcher.class.getName());

    /**
     * Main entry point for all Virtual Schema Adapter requests issued by the Exasol database.
     *
     * @param metadata   metadata for the context in which the adapter exists (e.g. the schema into which it is
     *                   installed)
     * @param rawRequest request issued in the call to the Virtual Schema Adapter
     * @return response resulting from the adapter call
     * @throws AdapterException in case the request type is not recognized
     */
    public static String adapterCall(final ExaMetadata metadata, final String rawRequest) throws AdapterException {
        logVersionInformation();
        logRawRequest(rawRequest);
        final AdapterRequest adapterRequest = parseRequest(rawRequest);
        configureAdapterLoggingAccordingToRequestSettings(adapterRequest);
        final AdapterCallExecutor adapterCallExecutor = getAdapterCallExecutor();
        return adapterCallExecutor.executeAdapterCall(adapterRequest, metadata);
    }

    private static void logVersionInformation() {
        final VersionCollector versionCollector = new VersionCollector();
        LOGGER.info("Loaded versions: virtual-schema-common-java " + versionCollector.getVersionNumber());
    }

    private static void logRawRequest(final String rawRequest) {
        LOGGER.finer(() -> "Raw JSON request:\n" + rawRequest);
    }

    private static AdapterRequest parseRequest(final String rawRequest) {
        return new RequestParser().parse(rawRequest);
    }

    private static void configureAdapterLoggingAccordingToRequestSettings(final AdapterRequest request) {
        final LoggingConfiguration configuration = LoggingConfiguration
                .parseFromProperties(request.getSchemaMetadataInfo().getProperties());
        final RemoteLogManager remoteLogManager = new RemoteLogManager();
        if (configuration.isRemoteLoggingConfigured()) {
            remoteLogManager.setupRemoteLogger(configuration.getRemoteLoggingHost(),
                    configuration.getRemoteLoggingPort(), configuration.getLogLevel());
        } else {
            remoteLogManager.setupConsoleLogger(configuration.getLogLevel());
        }
    }

    private static AdapterCallExecutor getAdapterCallExecutor() {
        return new AdapterCallExecutor(getVirtualSchemaAdapter());
    }

    private static VirtualSchemaAdapter getVirtualSchemaAdapter() {
        final ServiceLoader<VirtualSchemaAdapter> virtualSchemaAdapterLoader = ServiceLoader
                .load(VirtualSchemaAdapter.class);
        return virtualSchemaAdapterLoader.findFirst().orElseThrow(() -> new NoSuchElementException(
                ExaError.messageBuilder("E-VS-COM-JAVA-29").message("No VirtualSchemaAdapter was found.").toString()));
//        LOGGER.config(() -> "Loading Virtual Schema Adapter: " + factory.getAdapterName() + " "
//                + factory.getAdapterVersion());
    }
}
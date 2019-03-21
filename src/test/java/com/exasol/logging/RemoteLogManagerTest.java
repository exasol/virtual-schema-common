package com.exasol.logging;

import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.Assert.assertThat;

import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.itsallcode.io.Capturable;
import org.itsallcode.junit.sysextensions.SystemErrGuard;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SystemErrGuard.class)
class RemoteLogManagerTest {
    private static final String CLASS_TAG = "\\[c\\.e\\.l\\.RemoteLogManagerTest\\]";
    private static final String TIMESTAMP_PATTERN = "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}.\\d{3}";
    private static final Logger LOGGER = Logger.getLogger(RemoteLogManagerTest.class.getName());

    @AfterEach
    void afterEach() {
        RemoteLogManager.getInstance().close();
    }

    @Test
    void testSetupConsoleLogging(final Capturable stream) throws IOException {
        RemoteLogManager.getInstance().setupConsoleLogger(Level.INFO);
        stream.capture();
        LOGGER.info("Hello.");
        assertThat(stream.getCapturedData(), matchesPattern(TIMESTAMP_PATTERN + " INFO +" + CLASS_TAG + " Hello.\\n"));
    }

    @Test
    void testSetupConsoleLoggingWithMoreDetailedLogLevel(final Capturable stream) throws IOException {
        RemoteLogManager.getInstance().setupConsoleLogger(Level.ALL);
        stream.capture();
        LOGGER.finest("Hello.");
        assertThat(stream.getCapturedData(),
                matchesPattern(TIMESTAMP_PATTERN + " FINEST +" + CLASS_TAG + " Hello.\\n"));
    }

    @Test
    void testRemoteSocketLogging() throws UnknownHostException, IOException {
        final InetAddress loop = InetAddress.getLoopbackAddress();
        String received = null;
        try (final ServerSocket server = new ServerSocket(0, 1, loop)) {
            server.setSoTimeout(2000);
            final String loopBackAddress = loop.getHostName();
            final int port = server.getLocalPort();
            attachToLogServiceInParallelThread(loopBackAddress, port);
            try (final Socket client = server.accept()) {
                client.setSoTimeout(2000);
                received = readLogMessageFromSocket(client);
            }
        }
        assertThat(received,
                matchesPattern(TIMESTAMP_PATTERN + " INFO +\\[.*?\\] Attached to output service with log level ALL."));
    }

    private void attachToLogServiceInParallelThread(final String loopBackAddress, final int port) {
        new Thread(() -> {
            try {
                Thread.sleep(100);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
            RemoteLogManager.getInstance().setupRemoteLogger(loopBackAddress, port, Level.ALL);
        }).start();
    }

    private String readLogMessageFromSocket(final Socket socket) throws IOException {
        String received = null;
        try (final InputStreamReader inputReader = new InputStreamReader(socket.getInputStream())) {
            try (final BufferedReader reader = new BufferedReader(inputReader)) {
                received = reader.readLine();
            }
        }
        return received;
    }

    @Test
    void testFallBackFromRemoteLoggingToConsoleLogging(final Capturable stream) {
        stream.capture();
        RemoteLogManager.getInstance().setupRemoteLogger("this.hostname.should.not.exist.exasol.com", 3000, Level.ALL);
        assertThat(stream.getCapturedData(), matchesPattern(".*Falling back to console log.\n"));
    }
}
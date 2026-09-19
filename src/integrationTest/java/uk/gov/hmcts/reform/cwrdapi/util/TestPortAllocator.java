package uk.gov.hmcts.reform.cwrdapi.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class TestPortAllocator {

    private static final Set<Integer> ALLOCATED_PORTS =
            ConcurrentHashMap.newKeySet();

    private TestPortAllocator() {
    }

    public static int allocate() {

        while (true) {
            int port = findAvailablePort();

            if (ALLOCATED_PORTS.add(port)) {
                return port;
            }
        }
    }

    public static void release(int port) {
        ALLOCATED_PORTS.remove(port);
    }

    private static int findAvailablePort() {

        try (ServerSocket socket =
                     new ServerSocket(
                             0,
                             50,
                             InetAddress.getLoopbackAddress())) {

            return socket.getLocalPort();

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Unable to find an available test port",
                    e
            );
        }
    }
}

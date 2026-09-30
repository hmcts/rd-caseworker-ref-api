package uk.gov.hmcts.reform.cwrdapi.config;

import lombok.Getter;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

@Getter
public class TestApplicationServer
        implements ApplicationListener<WebServerInitializedEvent> {

    private final int allocatedPort;

    private volatile int actualPort;

    public TestApplicationServer() {
        this.allocatedPort = findAvailablePort();
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
                    "Unable to find an available port",
                    e
            );
        }
    }


    public String getBaseUrl() {
        return "http://127.0.0.1:" + actualPort;
    }

    public String url(String path) {
        return getBaseUrl()
                + (path.startsWith("/") ? path : "/" + path);
    }

    @Override
    public void onApplicationEvent(
            WebServerInitializedEvent event) {

        this.actualPort =
                event.getWebServer().getPort();
    }

}
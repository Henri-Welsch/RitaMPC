package lu.feschhaff.ritampc.controllers.websocket;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;

/**
 * @author Henri-Welsch
 * @sources {
 *     https://jakarta.ee/specifications/websocket/
 * }
 */
@Component
class ConnectionEstablisher {
    @Value("${home.assistant.websocket.url}")
    private String homeAssistantWebsocketUrl;

    private final ConnectionHandler connectionHandler;
    private Session session;

    public ConnectionEstablisher(ConnectionHandler connectionHandler) {
        this.connectionHandler = connectionHandler;
    }

    @PostConstruct
    public void open() throws Exception {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        session = container.connectToServer(
                connectionHandler,
                new URI(homeAssistantWebsocketUrl)
        );
    }

    @PreDestroy
    public void close() throws Exception {
        session.close();
    }
}
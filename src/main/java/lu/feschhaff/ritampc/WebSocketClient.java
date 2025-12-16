package lu.feschhaff.ritampc;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.websocket.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;

@Slf4j
@ClientEndpoint
@Component
public class WebSocketClient {
    @Value("${home.assistant.websocket.access_token}")
    String access_token;

    @OnOpen
    public void onOpen(Session session) throws IOException {
        // Get session and WebSocket connection
        log.info("Session successfully created");
    }

    @OnMessage
    public void onMessage(Session session, String message) throws IOException {
        // Handle new messages
        log.info("Received message: {}", message);

        if (message.contains("\"type\":\"auth_required\"")) {
            sendMessage(session,
                    "{\"type\":\"auth\",\"access_token\":\"" + access_token + "\"}"
            );
        }
    }

    @OnClose
    public void onClose(Session session) throws IOException {
        // WebSocket connection closes
        log.info("Session closed");
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        // Do error handling here
        log.error("Error occurred", throwable);
    }

    public void sendMessage(Session session, String message) throws IOException {
        log.info("Sending message: {}", message);
        session.getBasicRemote().sendText(message);
    }
}

@Component
class WebSocketStarter {
    @Value("${home.assistant.websocket.url}")
    private String homeAssistantWebsocketUrl;

    private final WebSocketClient webSocketClient;
    private Session session;

    public WebSocketStarter(WebSocketClient webSocketClient) {
        this.webSocketClient = webSocketClient;
    }

    @PostConstruct
    public void open() throws Exception {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        session = container.connectToServer(
                webSocketClient,
                new URI(homeAssistantWebsocketUrl)
        );
    }

    @PreDestroy
    public void close() throws Exception {
        session.close();
    }
}

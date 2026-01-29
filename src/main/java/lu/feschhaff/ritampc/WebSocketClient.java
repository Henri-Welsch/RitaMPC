package lu.feschhaff.ritampc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.websocket.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

@Slf4j
@ClientEndpoint
@Component
public class WebSocketClient {
    // https://developers.home-assistant.io/docs/api/websocket/

    ObjectMapper mapper = new ObjectMapper();

    @Value("${home.assistant.websocket.access_token}")
    String access_token;

    @OnOpen
    public void onOpen(Session session) throws IOException {
        // Get session and WebSocket connection
        log.info("Session successfully created");
    }

    @OnMessage
    public void onMessage(Session session, String message) throws IOException {
        log.info("Received message: {}", message);
        JsonNode root = mapper.readTree(message);

        String type = root.path("type").asText();

        // Authentication phase, this is the first step to connect to the websocket
        // https://developers.home-assistant.io/docs/api/websocket/#authentication-phase
        switch (type) {
            case "auth_required": {
                Message authMessage = Message.builder().type("auth").access_token(access_token).build();
                sendMessage(session, authMessage);
                break;
            }
            case "auth_ok": {
                log.info("Authentication successful");
                break;
            }
            case "auth_invalid": {
                log.info("Authentication failed");
                break;
            }
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

    public void sendMessage(Session session, Message message) throws IOException {
        log.info("Sending message: {}", message);

        String jsonMessage = new ObjectMapper().writeValueAsString(message);
        session.getBasicRemote().sendText(jsonMessage);
    }

    private void handleInitialStates(JsonNode result) {
        for (JsonNode entity : result) {
            String entityId = entity.path("entity_id").asText();
            String state = entity.path("state").asText();
            log.info("Entity {} has state {}", entityId, state);
        }
    }

    private void handleEvent(JsonNode event) {
        String eventType = event.path("event_type").asText();
        if ("state_changed".equals(eventType)) {
            JsonNode newState = event.path("data").path("new_state");
            log.info("Entity {} changed to {}", newState.path("entity_id").asText(), newState.path("state").asText());
        }
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


@JsonInclude(JsonInclude.Include.NON_NULL) // skip null fields
@RequiredArgsConstructor // constructor for required fields
@AllArgsConstructor
@Builder
@Getter
@JsonPropertyOrder({ "type", "access_token", "id", "event_type", "service_data" })
class Message {
    @NonNull
    private String type;           // must always be set
    private String access_token;   // optional
    private Integer id;            // optional
    private String event_type;     // optional
    private Object service_data;   // optional, can be Map or custom class
}


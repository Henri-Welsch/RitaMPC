package lu.feschhaff.ritampc.Controllers;

import jakarta.websocket.*;
import lombok.extern.slf4j.Slf4j;
import lu.feschhaff.ritampc.dtos.request.Request;
import lu.feschhaff.ritampc.dtos.response.Response;
import lu.feschhaff.ritampc.services.StateStoreService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * @author Joé Welsch
 * @references {
 *     https://developers.home-assistant.io/docs/api/websocket/
 *     https://www.baeldung.com/java-websockets
 * }
 */

@Slf4j @ClientEndpoint @Component
public class ConnectionHandler {

    private final StateStoreService stateStoreService;
    private int messageId = 0;

    @Value("${home.assistant.websocket.access_token}")
    String access_token;

    public ConnectionHandler(StateStoreService stateStoreService) {
        this.stateStoreService = stateStoreService;
    }

    @OnOpen
    public void onOpen(Session session) throws IOException {
        // Get session and WebSocket connection
        log.info("Session successfully created");
    }

    @OnMessage
    public void onMessage(Session session, String jsonResponse) throws IOException {
        log.info("Received message: {}", jsonResponse);

        Response response = new ObjectMapper().readValue(jsonResponse, Response.class);
        String type = response.getType();

        // Authentication phase, this is the first step to connect to the websocket
        // https://developers.home-assistant.io/docs/api/websocket/#authentication-phase
        switch (type) {
            case "auth_required": {
                Request request = Request.builder().type("auth").access_token(access_token).build();
                sendMessage(session, request);
                break;
            }
            case "auth_ok": {
                log.info("Authentication successful");

                Request request = Request.builder().id(++messageId).type("subscribe_events").event_type("state_changed").build();
                sendMessage(session, request);
                break;
            }
            case "auth_invalid": {
                log.error("Authentication failed");
                break;
            }
            case "event": {
                String entityId = response.getEvent().getData().getEntity_id();
                this.stateStoreService.getStateStore().put(entityId, response);
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

    public void sendMessage(Session session, Request request) throws IOException {
        log.info("Sending message: {}", request);

        String jsonRequest = new ObjectMapper().writeValueAsString(request);
        session.getBasicRemote().sendText(jsonRequest);
    }
}



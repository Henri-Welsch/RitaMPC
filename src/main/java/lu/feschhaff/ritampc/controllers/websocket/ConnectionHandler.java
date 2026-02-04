package lu.feschhaff.ritampc.controllers.websocket;

import jakarta.websocket.*;
import lombok.extern.slf4j.Slf4j;
import lu.feschhaff.ritampc.models.dtos.request.Request;
import lu.feschhaff.ritampc.models.dtos.response.Response;
import lu.feschhaff.ritampc.services.StateStoreService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * @author Joé Welsch
 * @references {
 *     <a href="https://developers.home-assistant.io/docs/api/websocket/">...</a>
 *     <a href="https://developers.home-assistant.io/docs/api/websocket/#authentication-phase">...</a>
 *     <a href="https://www.baeldung.com/java-websockets">...</a>
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
    public void onOpen(Session session) {
        // Get session and WebSocket connection
        log.info("Session successfully created");
    }

    @OnMessage
    public void onMessage(Session session, String jsonResponse) throws IOException {
        Response response = new ObjectMapper().readValue(jsonResponse, Response.class);
        String type = response.getType();

        switch (type) {
            case "auth_required": {
                log.info("Authentication request from Home Assistant");

                Request request = Request.builder()
                        .type("auth")
                        .access_token(access_token)
                        .build();

                sendMessage(session, request);
                break;
            }
            case "auth_ok": {
                log.info("Authentication successful, now subscribed to events!");

                Request request = Request.builder()
                        .id(++messageId)
                        .type("subscribe_events")
                        .event_type("state_changed")
                        .build();

                sendMessage(session, request);
                break;
            }
            case "event": {
                String entityId = response.getEvent().getData().getEntity_id();
                this.stateStoreService.getStateStore().put(entityId, response);
                break;
            }
            case "result": {
                log.info("Successfully subscribed to events!");
                break;
            }
            default: {
                log.error("Unrecognized event type {}", jsonResponse);
                break;
            }
        }
    }

    @OnClose
    public void onClose(Session session) {
        // WebSocket connection closes
        log.info("Session closed");
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        // Do error handling here
        log.error("Error occurred", throwable);
    }

    public void sendMessage(Session session, Request request) throws IOException {
        log.info("Sending message: {}", request.toString());

        String jsonRequest = new ObjectMapper().writeValueAsString(request);
        session.getBasicRemote().sendText(jsonRequest);
    }
}



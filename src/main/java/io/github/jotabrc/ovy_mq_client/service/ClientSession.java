package io.github.jotabrc.ovy_mq_client.service;

import io.github.jotabrc.ovy_mq_client.config.CredentialConfig;
import io.github.jotabrc.ovy_mq_client.handler.ServerSubscribeException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static java.util.Objects.isNull;

@Slf4j
@Getter
@Component
public class ClientSession {

    private final CredentialConfig credentialConfig;
    private StompSession session;

    public ClientSession(CredentialConfig credentialConfig) {
        this.credentialConfig = credentialConfig;
    }

    public void init() {
        StandardWebSocketClient webSocketClient = new StandardWebSocketClient();
        WebSocketStompClient stompClient = new WebSocketStompClient(webSocketClient);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        ClientSessionHandler sessionHandler = new ClientSessionHandler();
        AtomicLong counter = new AtomicLong(1L);
        while (counter.get() < 10) {
            WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
            String basic = Base64.getEncoder().encodeToString((credentialConfig.getBcrypt() + ":client").getBytes());
            headers.put("Authorization", List.of("Basic " + basic));
            headers.put("topic", List.of("Basic " + basic));
            stompClient.connectAsync("ws://localhost:9090/registry", headers, sessionHandler);

            try {
                this.session = sessionHandler.getFuture().get(5, TimeUnit.SECONDS);
                log.info("Session initialized {}", session.getSessionId());
                return;
            } catch (Exception e) {
                log.info("Server not available, retrying subscription... {}", counter.getAndIncrement());
                try {
                    Thread.sleep(1000 * counter.get());
                } catch (InterruptedException ex) {
                    log.error("Thread interrupted {}: {}", Thread.interrupted(), ex.getMessage());
                }
            }
        }

        if (isNull(session)) throw new ServerSubscribeException("Unable to acquire connection to server");
    }
}

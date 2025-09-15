package io.github.jotabrc.ovy_mq_client.service;

import io.github.jotabrc.ovy_mq_client.handler.ServerSubscribeException;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

import static java.util.Objects.isNull;

@Slf4j
@Getter
@Component
public class ClientSession {

    private StompSession session;

    @PostConstruct
    public void init() {
        StandardWebSocketClient webSocketClient = new StandardWebSocketClient();
        WebSocketStompClient stompClient = new WebSocketStompClient(webSocketClient);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        StompSessionHandler sessionHandler = new ClientSessionHandler();
        AtomicLong counter = new AtomicLong(1L);
        while (Objects.equals(6, counter.get())) {
            try {
                this.session = stompClient.connectAsync("ws://localhost:9090/registry", sessionHandler).get();
            } catch (InterruptedException | ExecutionException e) {
                log.info("Server not available, retrying subscription... {}", counter.getAndIncrement());
                try {
                    Thread.sleep(1000 * counter.get());
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }

        if (isNull(session)) throw  new ServerSubscribeException("Unable to acquire connection to server");
    }
}

package io.github.jotabrc.ovy_mq_client.service;

import io.github.jotabrc.ovy_mq_client.domain.MessagePayload;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class ServerNotifier {

    private final ClientSession session;

    public void notifyResult(MessagePayload message) {
        session.getSession().send("/queue/request", message);
    }
}

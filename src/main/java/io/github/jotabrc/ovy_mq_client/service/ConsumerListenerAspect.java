package io.github.jotabrc.ovy_mq_client.service;

import io.github.jotabrc.ovy_mq_client.domain.MessageMapper;
import io.github.jotabrc.ovy_mq_client.domain.MessagePayload;
import io.github.jotabrc.ovy_mq_client.handler.MessagePayloadNotFoundException;
import lombok.AllArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Aspect
@Component
public class ConsumerListenerAspect {

    private final ServerNotifier serverNotifier;

    @Around("@annotation(consumerListener)")
    public Object consumerProccesingAspect(ProceedingJoinPoint joinPoint, ConsumerListener consumerListener) throws Throwable {

        Object[] args = joinPoint.getArgs();
        if (args.length > 0 && args[0] instanceof String json) {
            Class<?> targetType = consumerListener.payloadType();
            MessagePayload message = MessageMapper.toMessagePayload(json);
            Object targetObject = MessageMapper.toTargetObject(message.getPayload(), targetType);
            args[0] = targetObject;

            try {
                Object result = joinPoint.proceed();
                message.setSuccess(true);
                return result;
            } finally {
                serverNotifier.notifyResult(message);
            }
        }
        throw new MessagePayloadNotFoundException("MessagePayload not present and no processing will be done");
    }
}

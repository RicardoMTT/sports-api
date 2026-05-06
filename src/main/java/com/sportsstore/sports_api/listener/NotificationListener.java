package com.sportsstore.sports_api.listener;


import com.sportsstore.sports_api.domain.event.OrderCompletedEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class NotificationListener {

    // Se ejecuta DESPUÉS de que la transacción principal hizo el COMMIT con éxito
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCompleted(OrderCompletedEvent event) {
        System.out.println("Enviando correo de confirmación asíncrono al usuario "
                + event.userId() + " por la orden " + event.orderId());
    }

}

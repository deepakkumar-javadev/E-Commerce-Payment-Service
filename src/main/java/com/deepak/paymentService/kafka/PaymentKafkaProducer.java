package com.deepak.paymentService.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentKafkaProducer {

	private final KafkaTemplate<String, PaymentSuccessEvent> kafkaTemplate;
	
	
	public void  publishPaymentSuccess(PaymentSuccessEvent event) {
		
		kafkaTemplate.send("payment.success",event.getOrderId().toString(),event)
		
	.whenComplete((result, ex) -> {

        if (ex != null) {
            System.out.println(
                    "❌ payment.success publish FAILED: "
                    + ex.getMessage()
            );
        } else {
            System.out.println(
                    "✅ payment.success published successfully"
            );

            System.out.println(
                    "Topic = " + result.getRecordMetadata().topic()
            );

            System.out.println(
                    "Partition = " + result.getRecordMetadata().partition()
            );

            System.out.println(
                    "Offset = " + result.getRecordMetadata().offset()
            );
        }
    });
}
	
}

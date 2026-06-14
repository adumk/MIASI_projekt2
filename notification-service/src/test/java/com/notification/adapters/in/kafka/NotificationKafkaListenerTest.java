//package com.notification.adapters.in.kafka;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//import org.apache.kafka.clients.consumer.ConsumerRecord;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import static org.assertj.core.api.Assertions.assertThatCode;
//class NotificationKafkaListenerTest {
//
//    // Teraz ObjectMapper pochodzi z pakietu "tools", więc pasuje do konstruktora
//    private final ObjectMapper objectMapper = new ObjectMapper();
//    private final NotificationKafkaListener listener = new NotificationKafkaListener(objectMapper);
//
//    @Test
//    @DisplayName("Should process valid event JSON without throwing exception")
//    void shouldProcessValidEvent() {
//        String json = "{\"eventType\": \"CarRented\", \"rentalId\": \"R1\", \"customerId\": \"C1\"}";
//        ConsumerRecord<String, String> record = new ConsumerRecord<>("test-topic", 0, 0, "key", json);
//
//        assertThatCode(() -> listener.onEvent(record))
//                .doesNotThrowAnyException();
//    }
//
//    @Test
//    @DisplayName("Should handle malformed JSON gracefully")
//    void shouldHandleMalformedJson() {
//        ConsumerRecord<String, String> record = new ConsumerRecord<>("test-topic", 0, 0, "key", "{ invalid }");
//
//        assertThatCode(() -> listener.onEvent(record))
//                .doesNotThrowAnyException();
//    }
//}
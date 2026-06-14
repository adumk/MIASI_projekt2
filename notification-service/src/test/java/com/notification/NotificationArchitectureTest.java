package com.notification;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

class NotificationArchitectureTest {

    private final JavaClasses importedClasses = new ClassFileImporter().importPackages("com.notification");

    @Test
    @DisplayName("Kafka listeners should reside in adapters.in.kafka package")
    void kafkaListenersShouldResideInKafkaPackage() {
        ArchRule rule = classes()
                .that().haveNameMatching(".*KafkaListener")
                .should().resideInAPackage("..adapters.in.kafka..");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Components and Services should be in appropriate packages")
    void componentsShouldBeInCorrectPackages() {
        ArchRule rule = classes()
                .that().areAnnotatedWith(Component.class)
                .or().areAnnotatedWith(Service.class)
                .should().resideInAnyPackage("..adapters..", "..application..", "..domain..");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("NotificationServiceApplication should be in the root package")
    void applicationClassShouldBeInRoot() {
        ArchRule rule = classes()
                .that().haveSimpleName("NotificationServiceApplication")
                .should().resideInAPackage("com.notification");

        rule.check(importedClasses);
    }
}
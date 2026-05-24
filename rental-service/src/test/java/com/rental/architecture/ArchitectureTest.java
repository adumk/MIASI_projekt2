package com.rental.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.Architectures;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

@DisplayName("Architecture guardrails — Hexagonal Architecture")
class ArchitectureTest {

    private static JavaClasses importedClasses;

    private static final String BASE_PACKAGE         = "com.rental";
    private static final String DOMAIN_PACKAGE       = BASE_PACKAGE + "..domain..";
    private static final String APPLICATION_PACKAGE  = BASE_PACKAGE + "..application..";
    private static final String PORTS_PACKAGE        = BASE_PACKAGE + "..ports..";
    private static final String PORTS_OUT_PACKAGE    = BASE_PACKAGE + "..ports.out..";
    private static final String ADAPTERS_PACKAGE     = BASE_PACKAGE + "..adapters..";
    private static final String ADAPTERS_OUT_PACKAGE = BASE_PACKAGE + "..adapters.out..";

    @BeforeAll
    static void loadClasses() {
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(BASE_PACKAGE);
    }

    // -------------------------------------------------------------------------
    // Existing tests — preserved without modification
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("should maintain hexagonal layers — domain must not depend on application, ports or adapters")
    void shouldMaintainHexagonalLayers() {
        Architectures.LayeredArchitecture layerRule = layeredArchitecture()
                .consideringOnlyDependenciesInLayers()
                .layer("domain")      .definedBy(DOMAIN_PACKAGE)
                .layer("application") .definedBy(APPLICATION_PACKAGE)
                .layer("ports")       .definedBy(PORTS_PACKAGE)
                .layer("adapters")    .definedBy(ADAPTERS_PACKAGE)
                .whereLayer("application").mayOnlyAccessLayers("domain", "ports")
                .whereLayer("ports").mayOnlyAccessLayers("domain")
                .whereLayer("adapters").mayOnlyAccessLayers("application", "ports", "domain")
                .whereLayer("domain").mayNotAccessAnyLayer();

        layerRule.check(importedClasses);

        ArchRule noDomainDependsOnSpring = classes()
                .that().resideInAPackage(DOMAIN_PACKAGE)
                .should().onlyDependOnClassesThat()
                .resideOutsideOfPackages(
                        "org.springframework..",
                        "jakarta.persistence..",
                        "javax.persistence..",
                        "org.hibernate.."
                )
                .as("Domain layer must be free of Spring and Hibernate");

        noDomainDependsOnSpring.check(importedClasses);
    }

    @Test
    @DisplayName("should respect port interfaces — adapters.out must implement interfaces from ports.out only")
    void shouldRespectPortInterfaces() {
        ArchRule adaptersOutMustImplementPortsOut = classes()
                .that().resideInAPackage(ADAPTERS_OUT_PACKAGE)
                .should().implement(
                        com.tngtech.archunit.core.domain.JavaClass.Predicates
                                .resideInAPackage(PORTS_OUT_PACKAGE)
                )
                .as("Classes in adapters.out must implement interfaces defined in ports.out");

        adaptersOutMustImplementPortsOut.check(importedClasses);
    }

    // -------------------------------------------------------------------------
    // New tests
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("should keep domain free from Spring and Hibernate annotations")
    void shouldKeepDomainFreeFromSpringAndHibernateAnnotations() {
        ArchRule rule = classes()
                .that().resideInAPackage(DOMAIN_PACKAGE)
                .should().notBeAnnotatedWith(
                        com.tngtech.archunit.core.domain.JavaAnnotation.class
                )
                .orShould().onlyDependOnClassesThat()
                .resideOutsideOfPackages(
                        "org.springframework..",
                        "jakarta.persistence..",
                        "javax.persistence..",
                        "org.hibernate..",
                        "org.springframework.stereotype..",
                        "org.springframework.web.."
                )
                .as("Domain classes must not carry Spring or Hibernate annotations");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("should prevent domain from depending on adapters")
    void shouldPreventDomainFromDependingOnAdapters() {
        ArchRule rule = noClasses()
                .that().resideInAPackage(DOMAIN_PACKAGE)
                .should().dependOnClassesThat()
                .resideInAPackage(ADAPTERS_PACKAGE)
                .as("Domain must not depend on any adapter");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("should prevent application layer from depending on outbound adapters")
    void shouldPreventApplicationFromDependingOnAdaptersOut() {
        ArchRule rule = noClasses()
                .that().resideInAPackage(APPLICATION_PACKAGE)
                .should().dependOnClassesThat()
                .resideInAPackage(ADAPTERS_OUT_PACKAGE)
                .as("Application layer must communicate with outbound adapters only through ports.out interfaces");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("should require outbound adapters to implement only outbound port interfaces")
    void shouldRequireAdaptersOutToImplementOnlyOutPorts() {
        ArchRule rule = classes()
                .that().resideInAPackage(ADAPTERS_OUT_PACKAGE)
                .should().implement(
                        com.tngtech.archunit.core.domain.JavaClass.Predicates
                                .resideInAPackage(PORTS_OUT_PACKAGE)
                )
                .as("Every class in adapters.out must implement an interface from ports.out");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("should prevent public setters in domain classes")
    void shouldPreventPublicSettersInDomain() {
        ArchRule rule = methods()
                .that().arePublic()
                .and().haveNameMatching("set[A-Z].*")
                .should().notBeDeclaredInClassesThat()
                .resideInAPackage(DOMAIN_PACKAGE)
                .as("Domain classes must not expose public setters — use behaviour methods instead");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("should require domain events to be immutable — no public setters, all fields final")
    void shouldRequireDomainEventsToBeImmutable() {
        ArchRule noSetters = methods()
                .that().arePublic()
                .and().haveNameMatching("set[A-Z].*")
                .should().notBeDeclaredInClassesThat()
                .areAssignableTo(com.rental.domain.DomainEvent.class)
                .as("Domain event classes must not expose public setters");

        ArchRule finalFields = fields()
                .that().areDeclaredInClassesThat()
                .areAssignableTo(com.rental.domain.DomainEvent.class)
                .should().beFinal()
                .as("All fields in domain event classes must be final");

        noSetters.check(importedClasses);
        finalFields.check(importedClasses);
    }

    @Test
    @DisplayName("should require value objects to be immutable — all fields final, no public setters")
    void shouldRequireValueObjectsToBeImmutable() {
        ArchRule finalFields = fields()
                .that().areDeclaredInClassesThat()
                .haveSimpleNameIn(
                        "RentalId", "VehicleId", "CustomerId",
                        "LicensePlate", "DateRange", "Money",
                        "Email", "PersonName", "DriverLicense", "DamageRecord"
                )
                .should().beFinal()
                .as("All fields in value object classes must be final");

        ArchRule noSetters = methods()
                .that().arePublic()
                .and().haveNameMatching("set[A-Z].*")
                .should().notBeDeclaredInClassesThat()
                .haveSimpleNameIn(
                        "RentalId", "VehicleId", "CustomerId",
                        "LicensePlate", "DateRange", "Money",
                        "Email", "PersonName", "DriverLicense", "DamageRecord"
                )
                .as("Value object classes must not expose public setters");

        finalFields.check(importedClasses);
        noSetters.check(importedClasses);
    }

    @Test
    @DisplayName("should keep controller layer outside of domain package")
    void shouldKeepControllerLayerOutsideDomain() {
        ArchRule rule = noClasses()
                .that().areAnnotatedWith(org.springframework.web.bind.annotation.RestController.class)
                .should().resideInAPackage(DOMAIN_PACKAGE)
                .as("REST controllers must not reside in the domain package");

        rule.check(importedClasses);
    }
}
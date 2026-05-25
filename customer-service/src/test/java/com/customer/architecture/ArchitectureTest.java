package com.customer.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.Architectures;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

@DisplayName("Architecture guardrails — Hexagonal Architecture")
class ArchitectureTest {

    private static JavaClasses importedClasses;

    private static final String BASE_PACKAGE = "com.customer";

    private static final String DOMAIN_PACKAGE = BASE_PACKAGE + "..domain..";
    private static final String APPLICATION_PACKAGE = BASE_PACKAGE + "..application..";
    private static final String PORTS_PACKAGE = BASE_PACKAGE + "..ports..";
    private static final String ADAPTERS_PACKAGE = BASE_PACKAGE + "..adapters..";

    @BeforeAll
    static void loadClasses() {
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(BASE_PACKAGE);
    }

    @Test
    @DisplayName("Should maintain hexagonal layers — domain must not depend on application, ports or adapters, nor on Spring/Hibernate")
    void shouldMaintainHexagonalLayers() {
        Architectures.LayeredArchitecture layerRule = layeredArchitecture()
                .consideringOnlyDependenciesInLayers()

                .layer("domain").definedBy(DOMAIN_PACKAGE)
                .layer("application").definedBy(APPLICATION_PACKAGE)
                .layer("ports").definedBy(PORTS_PACKAGE)
                .layer("adapters").definedBy(ADAPTERS_PACKAGE)

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
                        "org.hibernate..")
                .as("Domain layer must be free of Spring and Hibernate");

        noDomainDependsOnSpring.check(importedClasses);
    }

    @Test
    @DisplayName("Should respect port interfaces — adapters.out must implement interfaces from ports.out only")
    void shouldRespectPortInterfaces() {
        ArchRule adaptersOutMustImplementPortsOut = classes()
                .that().resideInAPackage(BASE_PACKAGE + "..adapters.out..")
                .should().implement(com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage(
                        BASE_PACKAGE + "..ports.out.."))
                .as("Classes in adapters.out must implement interfaces defined in ports.out");

        adaptersOutMustImplementPortsOut.check(importedClasses);
    }
}

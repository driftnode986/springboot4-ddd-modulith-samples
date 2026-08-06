package com.example.shop;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * モジュールの内側の層構造を検証する。
 * ApplicationModules.verify() はモジュール間だけを見るため、ここが受け持つ。
 */
class LayeringTests {

    static final JavaClasses classes = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.example.shop");

    @Test
    @DisplayName("domain は infrastructure を参照しない")
    void domainDoesNotDependOnInfrastructure() {
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAPackage("..infrastructure..")
                .check(classes);
    }

    @Test
    @DisplayName("domain は Spring に依存しない")
    void domainDoesNotDependOnSpring() {
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAPackage("org.springframework..")
                .check(classes);
    }

    @Test
    @DisplayName("application のサービスは @Service を付ける")
    void applicationServicesAreAnnotated() {
        classes()
                .that().resideInAPackage("..application..")
                .and().haveSimpleNameEndingWith("Service")
                .should().beAnnotatedWith(Service.class)
                .check(classes);
    }

    @Test
    @DisplayName("@Transactional は application だけに付ける")
    void transactionalOnlyInApplication() {
        noClasses()
                .that().resideOutsideOfPackage("..application..")
                .should().beAnnotatedWith(Transactional.class)
                .check(classes);
    }
}


package com.bammwise.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DependencyInfoTest {

    @Test
    void shouldStoreDependencyFields() {
        DependencyInfo dependency = new DependencyInfo(
                "org.springframework.boot",
                "spring-boot-starter-web",
                "3.1.0",
                "compile"
        );

        assertEquals(
                "org.springframework.boot",
                dependency.groupId()
        );
        assertEquals(
                "spring-boot-starter-web",
                dependency.artifactId()
        );
        assertEquals("3.1.0", dependency.version());
        assertEquals("compile", dependency.scope());
    }

    @Test
    void shouldFormatDependencyForDisplay() {
        DependencyInfo dependency = new DependencyInfo(
                "org.springframework.boot",
                "spring-boot-starter-web",
                "3.1.0",
                "compile"
        );

        assertEquals(
                "org.springframework.boot:spring-boot-starter-web:3.1.0"
                        + " [scope: compile]",
                dependency.toDisplayString()
        );
    }

    @Test
    void shouldHandleMissingVersionAndScope() {
        DependencyInfo dependency = new DependencyInfo(
                "com.example",
                "example-library",
                null,
                null
        );

        assertNull(dependency.version());
        assertNull(dependency.scope());

        assertEquals(
                "com.example:example-library:(version unresolved)",
                dependency.toDisplayString()
        );
    }

    @Test
    void shouldRejectBlankCoordinates() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new DependencyInfo(
                        " ",
                        "example-library",
                        "1.0.0",
                        "compile"
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new DependencyInfo(
                        "com.example",
                        " ",
                        "1.0.0",
                        "compile"
                )
        );
    }
}

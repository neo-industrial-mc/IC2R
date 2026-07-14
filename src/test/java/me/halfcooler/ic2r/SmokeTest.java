package me.halfcooler.ic2r;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Placeholder smoke test for W0.1 test infrastructure.
 * Proves {@code gradlew test} / JUnit 5 platform wiring without starting Minecraft.
 */
class SmokeTest {

    @Test
    void junit_platform_is_wired() {
        assertTrue(true);
        assertEquals(2, 1 + 1);
    }
}

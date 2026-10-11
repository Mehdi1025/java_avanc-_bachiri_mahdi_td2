package app2.ai;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AlbertApiKeyLoaderTest {

    @Test
    void loadsKeyFromEnvFile(@TempDir Path tempDir) throws Exception {
        Files.writeString(tempDir.resolve(".env"), "ALBERT_API_KEY=test-key-from-file\n");
        assertEquals("test-key-from-file", AlbertApiKeyLoader.loadOptionalFrom(tempDir).orElseThrow());
    }
}

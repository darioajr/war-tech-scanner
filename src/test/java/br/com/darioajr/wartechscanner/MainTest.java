/*
 * Copyright 2024-present Dario Alves Junior
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package br.com.darioajr.wartechscanner;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    @TempDir
    Path tmp;

    private final PrintStream realOut = System.out;
    private ByteArrayOutputStream captured;

    @BeforeEach
    void redirect() {
        captured = new ByteArrayOutputStream();
        System.setOut(new PrintStream(captured, true, StandardCharsets.UTF_8));
    }

    @AfterEach
    void restore() {
        System.setOut(realOut);
    }

    private String output() {
        return captured.toString(StandardCharsets.UTF_8);
    }

    private Path war(String name, Map<String, byte[]> entries) throws IOException {
        Path p = tmp.resolve(name);
        var bos = new ByteArrayOutputStream();
        try (var zos = new ZipOutputStream(bos)) {
            for (var e : entries.entrySet()) {
                zos.putNextEntry(new ZipEntry(e.getKey()));
                zos.write(e.getValue());
                zos.closeEntry();
            }
        }
        Files.write(p, bos.toByteArray());
        return p;
    }

    private static byte[] xml() {
        return "<?xml version=\"1.0\"?><root/>".getBytes(StandardCharsets.UTF_8);
    }

    @Test
    void jsonModeWithDetectedTechReturnsZero() throws Exception {
        var main = new Main();
        main.artifact = war("app.war", Map.of("WEB-INF/web.xml", xml())); // Servlet descriptor
        main.json = true;

        Integer code = main.call();

        assertEquals(0, code);
        assertTrue(output().contains("\"artifactType\""));
    }

    @Test
    void jsonModeWithoutTechReturnsTwo() throws Exception {
        var main = new Main();
        main.artifact = war("empty.war", Map.of("readme.txt", "hi".getBytes(StandardCharsets.UTF_8)));
        main.json = true;

        assertEquals(2, main.call());
    }

    @Test
    void richModeRunsAndReturnsZero() throws Exception {
        var main = new Main();
        main.artifact = war("rich.war", Map.of("WEB-INF/web.xml", xml()));
        main.json = false;
        main.noNested = true;

        assertEquals(0, main.call());
    }

    @Test
    void migrationHintsBranchWithTargets() throws Exception {
        var main = new Main();
        main.artifact = war("mig.war", Map.of("WEB-INF/web.xml", xml()));
        main.json = true;
        main.targetEap = "8";
        main.targetJava = 17;

        assertEquals(0, main.call());
    }

    @Test
    void mtaConfigBranchIsExercised() throws Exception {
        Path config = tmp.resolve("mta.json");
        Files.writeString(config, "{\"mtaInstallations\":[]}");

        var main = new Main();
        main.artifact = war("mta.war", Map.of("WEB-INF/web.xml", xml()));
        main.json = true;
        main.mtaConfig = config;

        assertEquals(0, main.call());
    }
}

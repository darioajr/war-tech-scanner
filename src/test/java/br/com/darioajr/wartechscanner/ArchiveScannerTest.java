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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArchiveScannerTest {

    @TempDir
    Path tmp;

    private static byte[] zipBytes(Map<String, byte[]> entries) throws IOException {
        var bos = new ByteArrayOutputStream();
        try (var zos = new ZipOutputStream(bos)) {
            for (var e : entries.entrySet()) {
                zos.putNextEntry(new ZipEntry(e.getKey()));
                zos.write(e.getValue());
                zos.closeEntry();
            }
        }
        return bos.toByteArray();
    }

    private Path writeZip(String name, Map<String, byte[]> entries) throws IOException {
        Path p = tmp.resolve(name);
        Files.write(p, zipBytes(entries));
        return p;
    }

    private static byte[] xml() {
        return "<?xml version=\"1.0\"?><root/>".getBytes(StandardCharsets.UTF_8);
    }

    private static List<String> techNames(ScanResult r) {
        return r.technologies.stream().map(t -> t.name).toList();
    }

    @Test
    void detectsDescriptorsClassesAndLibraries() throws IOException {
        var entries = new LinkedHashMap<String, byte[]>();
        entries.put("WEB-INF/web.xml", xml());                          // Servlet (descriptor)
        entries.put("META-INF/persistence.xml", xml());                 // JPA (descriptor)
        entries.put("WEB-INF/lib/hibernate-core-6.4.0.jar", new byte[]{0}); // Hibernate (library)
        entries.put("WEB-INF/classes/com/example/MyController.class",
                ClassBytesFixture.builder("com/example/MyController")
                        .superName("org/springframework/web/servlet/FrameworkServlet")
                        .build());                                      // Spring (class)

        Path war = writeZip("app.war", entries);
        var result = new ArchiveScanner(false).scan(war);

        assertEquals("WAR", result.artifactType);
        var names = techNames(result);
        assertTrue(names.contains("Servlet"), names::toString);
        assertTrue(names.contains("JPA"), names::toString);
        assertTrue(names.contains("Hibernate"), names::toString);
        assertTrue(names.contains("Spring"), names::toString);

        assertTrue(result.descriptors.stream().anyMatch(d -> d.endsWith("web.xml")));
        assertTrue(result.descriptors.stream().anyMatch(d -> d.endsWith("persistence.xml")));
        assertTrue(result.libraries.stream().anyMatch(l -> l.contains("hibernate-core")));
        assertFalse(result.classesWithEvidence.isEmpty());
    }

    @Test
    void scansNestedArchiveWhenEnabled() throws IOException {
        var innerJar = zipBytes(Map.of(
                "com/example/InnerEjb.class",
                ClassBytesFixture.builder("com/example/InnerEjb")
                        .classAnnotation("Ljakarta/ejb/Stateless;")
                        .build()));

        Path ear = writeZip("app.ear", Map.of("lib/inner.jar", innerJar));

        var enabled = new ArchiveScanner(true).scan(ear);
        assertTrue(techNames(enabled).contains("EJB"), "nested EJB must be detected when nesting enabled");
        assertTrue(enabled.libraries.stream().anyMatch(l -> l.contains("inner.jar")));
        assertEquals("EAR", enabled.artifactType);
    }

    @Test
    void skipsNestedArchiveWhenDisabled() throws IOException {
        var innerJar = zipBytes(Map.of(
                "com/example/InnerEjb.class",
                ClassBytesFixture.builder("com/example/InnerEjb")
                        .classAnnotation("Ljakarta/ejb/Stateless;")
                        .build()));

        Path ear = writeZip("app2.ear", Map.of("lib/inner.jar", innerJar));

        var disabled = new ArchiveScanner(false).scan(ear);
        assertFalse(techNames(disabled).contains("EJB"), "nested classes must be ignored when nesting disabled");
    }

    @Test
    void recordsWarningForCorruptClass() throws IOException {
        Path war = writeZip("bad.war", Map.of(
                "WEB-INF/classes/Bad.class", new byte[]{1, 2, 3, 4}));
        var result = new ArchiveScanner(false).scan(war);
        assertFalse(result.warnings.isEmpty(), "corrupt .class must produce a warning");
    }

    @Test
    void throwsWhenFileMissing() {
        Path missing = tmp.resolve("does-not-exist.war");
        assertThrows(IOException.class, () -> new ArchiveScanner(false).scan(missing));
    }

    @Test
    void throwsWhenPathIsDirectory() {
        assertThrows(IOException.class, () -> new ArchiveScanner(false).scan(tmp));
    }

    @Test
    void unknownExtensionReported() throws IOException {
        Path noExt = tmp.resolve("archive");
        Files.write(noExt, zipBytes(Map.of("a/b.txt", xml())));
        var result = new ArchiveScanner(false).scan(noExt);
        assertEquals("UNKNOWN", result.artifactType);
    }

    @Test
    void notifiesProgressListener() throws IOException {
        var counter = new int[]{0, 0, 0}; // start, progress, complete
        ScanProgressListener listener = new ScanProgressListener() {
            @Override public void onScanStart(String artifact, int total) { counter[0]++; }
            @Override public void onProgress(String entry, int processed, int total) { counter[1]++; }
            @Override public void onNestedArchive(String name) { /* not used in this test */ }
            @Override public void onScanComplete() { counter[2]++; }
        };

        Path war = writeZip("listen.war", Map.of("WEB-INF/web.xml", xml()));
        new ArchiveScanner(false, listener).scan(war);

        assertTrue(counter[0] >= 1, "onScanStart");
        assertTrue(counter[1] >= 1, "onProgress");
        assertEquals(1, counter[2], "onScanComplete");
    }
}

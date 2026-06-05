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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RichConsoleTest {

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

    /** Result populated to exercise every section and branch of the summary. */
    private static ScanResult populatedResult() {
        var result = new ScanResult();
        result.artifact = "/path/to/app.ear";
        result.artifactType = "EAR";
        result.classesWithEvidence.add("com/example/Foo");
        result.descriptors.add("WEB-INF/web.xml");
        result.libraries.add("lib/hibernate.jar");

        var ejb = new DetectedTechnology("EJB");
        ejb.score = 80;
        ejb.addEvidence("class annotation: com.example.Foo", 7);
        ejb.addEvidence("class annotation: com.example.Bar", 7);
        ejb.addEvidence("a very long evidence string ".repeat(8), 7); // forces truncate()
        result.technologies.add(ejb);

        var jpa = new DetectedTechnology("JPA");
        jpa.score = 20;
        jpa.addEvidence("descriptor: persistence.xml", 10);
        result.technologies.add(jpa);

        result.warnings.add("Could not read nested archive lib/broken.jar");
        result.migrationHints.add("EJB 2.x detected — migrate to EJB 3+");

        var s = new MtaSuggestion();
        s.mtaLabel = "MTA 7.2 (container)";
        s.installationType = MtaInstallationType.CONTAINER;
        s.note = "Image not available locally — run docker pull";
        s.resolvedSources = List.of("eap7");
        s.resolvedTargets = List.of("eap8", "cloud-readiness");
        s.command = "docker run --rm mta-cli analyze\n  --target eap8";
        result.mtaSuggestions = List.of(s);

        return result;
    }

    private static MigrationTarget target() {
        return new MigrationTarget("8", 17);
    }

    @Test
    void plainSummaryRendersAllSections() {
        var console = new RichConsole(false, false); // PLAIN
        console.printSummary(populatedResult(), 1, target());

        String out = output();
        assertTrue(out.contains("/path/to/app.ear"));
        assertTrue(out.contains("EJB"));
        assertTrue(out.contains("Warnings:"));
        assertTrue(out.contains("Migration analysis:"));
        assertTrue(out.contains("MTA command"));
        assertTrue(out.contains("eap8"));
        assertTrue(out.contains("..."), "maxEvidence=1 must collapse extra evidences");
    }

    @Test
    void plainSummaryWithoutTechnologies() {
        var console = new RichConsole(false, false);
        var empty = new ScanResult();
        empty.artifact = "/x.war";
        empty.artifactType = "WAR";

        console.printSummary(empty, 5, target());

        assertTrue(output().contains("No known technologies detected."));
    }

    // NOTE: RICH/BASIC modes call AnsiConsole.systemInstall(), which writes to the real
    // stdout file descriptor and bypasses the System.out capture. These tests therefore
    // assert the rendering path runs without error (JaCoCo still records the coverage).

    @Test
    void richSummaryDrawsBoxAndBars() {
        var console = new RichConsole(true, true); // RICH (Unicode + ANSI)
        assertDoesNotThrow(() -> console.printSummary(populatedResult(), 1, target()));
    }

    @Test
    void basicSummaryUsesAsciiChars() {
        var console = new RichConsole(true, false); // BASIC (ANSI, ASCII chars)
        assertDoesNotThrow(() -> console.printSummary(populatedResult(), 2, target()));
    }

    @Test
    void richSummaryWithoutTechnologies() {
        var console = new RichConsole(true, true);
        var empty = new ScanResult();
        empty.artifact = "/y.war";
        empty.artifactType = "WAR";

        assertDoesNotThrow(() -> console.printSummary(empty, 5, target()));
    }

    @Test
    void listenerLifecyclePlainMode() {
        var console = new RichConsole(false, false);

        console.onScanStart("/path/to/app.war", 3);
        console.onProgress("WEB-INF/classes/com/example/Foo.class", 1, 3);
        console.onNestedArchive("lib/inner.jar");
        console.onProgress("lib/inner.jar", 2, 3);
        console.onScanComplete();

        assertTrue(output().contains("Scanning: /path/to/app.war"));
    }

    @Test
    void listenerLifecycleRichModeThenSummary() throws InterruptedException {
        var console = new RichConsole(true, true);

        assertDoesNotThrow(() -> {
            console.onScanStart("/path/to/app.ear", 2);
            console.onProgress("WEB-INF/classes/A.class", 1, 2);
            Thread.sleep(120); // let the spinner render at least one frame
            console.onNestedArchive("lib/inner.jar");
            console.onScanComplete();
            // printSummary balances the AnsiConsole install/uninstall counter
            console.printSummary(populatedResult(), 1, target());
        });
    }
}

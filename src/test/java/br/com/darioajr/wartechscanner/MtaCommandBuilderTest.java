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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MtaCommandBuilderTest {

    private ScanResult result;

    @BeforeEach
    void setUp() {
        result = new ScanResult();
        result.artifact = "/path/to/app.ear";
        addTech("EJB", 50);
        addTech("JPA", 30);
        addTech("Spring", 20);
    }

    private void addTech(String name, int score) {
        var t = new DetectedTechnology(name);
        t.score = score;
        result.technologies.add(t);
    }

    private MtaInstallation bareMetal(String path) {
        var inst = new MtaInstallation();
        inst.label = "Test BARE_METAL";
        inst.type  = MtaInstallationType.BARE_METAL;
        inst.path  = path;
        return inst;
    }

    private MtaInstallation container(String image, String engine) {
        var inst = new MtaInstallation();
        inst.label           = "Test CONTAINER";
        inst.type            = MtaInstallationType.CONTAINER;
        inst.image           = image;
        inst.containerEngine = engine;
        return inst;
    }

    private MtaInstallation openshift(String ns, String channel) {
        var inst = new MtaInstallation();
        inst.label            = "Test OPENSHIFT";
        inst.type             = MtaInstallationType.OPENSHIFT;
        inst.namespace        = ns;
        inst.operatorChannel  = channel;
        inst.operatorCatalog  = "redhat-operators";
        inst.hubRoute         = "https://mta.apps.example.com";
        return inst;
    }

    // ── BARE_METAL ────────────────────────────────────────────────────────────

    @Test
    void bareMetal_commandContainsInputAndOutput() {
        var config = configWith(bareMetal("/opt/mta/mta-cli"));
        var target = new MigrationTarget("8.0", 21);
        var suggestions = MtaCommandBuilder.buildAll(result, target, config);

        assertEquals(1, suggestions.size());
        var cmd = suggestions.get(0).command;
        assertTrue(cmd.contains("--input /path/to/app.ear"));
        assertTrue(cmd.contains("--output ./mta-report"));
        assertTrue(cmd.contains("--overwrite"));
    }

    @Test
    void bareMetal_withoutCapabilities_usesEapMajorVersion() {
        var config = configWith(bareMetal("/nonexistent/mta-cli"));
        var target = new MigrationTarget("8.1", 0);
        var s = MtaCommandBuilder.buildAll(result, target, config).get(0);

        assertTrue(s.resolvedTargets.contains("eap8"),
            "Should fall back to eap8 when eap81 is unavailable");
        assertFalse(s.resolvedTargets.contains("eap81"));
    }

    @Test
    void bareMetal_withoutCapabilities_usesOpenjdkTarget() {
        var config = configWith(bareMetal("/nonexistent/mta-cli"));
        var target = new MigrationTarget(null, 21);
        var s = MtaCommandBuilder.buildAll(result, target, config).get(0);

        assertTrue(s.resolvedTargets.contains("openjdk21"));
    }

    @Test
    void bareMetal_withoutCapabilities_noSourcesEmitted() {
        var config = configWith(bareMetal("/nonexistent/mta-cli"));
        var target = new MigrationTarget("8.0", 0);
        var s = MtaCommandBuilder.buildAll(result, target, config).get(0);

        assertTrue(s.resolvedSources.isEmpty(),
            "Without confirmed discovery, --source must not be generated");
        assertFalse(s.command.contains("--source"));
    }

    @Test
    void bareMetal_withoutCapabilities_noteIsSet() {
        var config = configWith(bareMetal("/nonexistent/mta-cli"));
        var s = MtaCommandBuilder.buildAll(result, new MigrationTarget("8.0", 0), config).get(0);
        assertNotNull(s.note);
    }

    @Test
    void bareMetal_targetsUseSeparateFlags() {
        var config = configWith(bareMetal("/nonexistent/mta-cli"));
        var target = new MigrationTarget("8.0", 21);
        var cmd = MtaCommandBuilder.buildAll(result, target, config).get(0).command;

        assertFalse(cmd.contains("eap8,"), "targets must not be comma-separated");
        assertTrue(cmd.contains("--target eap8"));
        assertTrue(cmd.contains("--target openjdk21"));
    }

    // ── CONTAINER ─────────────────────────────────────────────────────────────

    @Test
    void container_docker_commandContainsDockerRun() {
        var config = configWith(container("registry.redhat.io/mta/mta-cli-rhel9:7.2", "docker"));
        var cmd = MtaCommandBuilder.buildAll(result, new MigrationTarget("8.0", 0), config).get(0).command;
        assertTrue(cmd.contains("docker run --rm"), "Expected 'docker run --rm' in: " + cmd);
    }

    @Test
    void container_podman_commandContainsPodmanRun() {
        var config = configWith(container("registry.redhat.io/mta/mta-cli-rhel9:7.2", "podman"));
        var cmd = MtaCommandBuilder.buildAll(result, new MigrationTarget("8.0", 0), config).get(0).command;
        assertTrue(cmd.contains("podman run --rm"), "Expected 'podman run --rm' in: " + cmd);
    }

    @Test
    void container_redhatRegistry_addsLoginStep() {
        var config = configWith(container("registry.redhat.io/mta/mta-cli-rhel9:7.2", "docker"));
        var cmd = MtaCommandBuilder.buildAll(result, new MigrationTarget("8.0", 0), config).get(0).command;
        assertTrue(cmd.contains("docker login registry.redhat.io"));
    }

    @Test
    void container_mountsVolumeWithFilename() {
        var config = configWith(container("registry.redhat.io/mta/mta-cli-rhel9:7.2", "docker"));
        var cmd = MtaCommandBuilder.buildAll(result, new MigrationTarget("8.0", 0), config).get(0).command;
        assertTrue(cmd.contains(":/app/input/app.ear:ro,z"));
        assertTrue(cmd.contains("/app/output"));
        assertTrue(cmd.contains("--overwrite"));
    }

    // ── OPENSHIFT ─────────────────────────────────────────────────────────────

    @Test
    void openshift_commandContainsSubscription() {
        var config = configWith(openshift("mta", "stable-v7"));
        var cmd = MtaCommandBuilder.buildAll(result, new MigrationTarget("8.0", 0), config).get(0).command;
        assertTrue(cmd.contains("kind: Subscription"));
        assertTrue(cmd.contains("channel: stable-v7"));
        assertTrue(cmd.contains("source: redhat-operators"));
    }

    @Test
    void openshift_commandContainsMtaHubApiCall() {
        var config = configWith(openshift("mta", "stable-v7"));
        var cmd = MtaCommandBuilder.buildAll(result, new MigrationTarget("8.0", 0), config).get(0).command;
        assertTrue(cmd.contains("/hub/applications"));
        assertTrue(cmd.contains("/hub/analyses"));
    }

    @Test
    void openshift_v6_usesStableV6Channel() {
        var config = configWith(openshift("mta", "stable-v6"));
        var cmd = MtaCommandBuilder.buildAll(result, new MigrationTarget("7.4", 0), config).get(0).command;
        assertTrue(cmd.contains("channel: stable-v6"));
    }

    // ── Multiple installations ────────────────────────────────────────────────

    @Test
    void buildAll_returnsOneEntryPerInstallation() {
        var config = configWith(
            bareMetal("/opt/mta/mta-cli"),
            container("registry.redhat.io/mta/mta-cli-rhel9:7.2", "docker"),
            openshift("mta", "stable-v7")
        );
        var suggestions = MtaCommandBuilder.buildAll(result, new MigrationTarget("8.0", 21), config);
        assertEquals(3, suggestions.size());
        assertEquals(MtaInstallationType.BARE_METAL,  suggestions.get(0).installationType);
        assertEquals(MtaInstallationType.CONTAINER,   suggestions.get(1).installationType);
        assertEquals(MtaInstallationType.OPENSHIFT,   suggestions.get(2).installationType);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private static MtaConfig configWith(MtaInstallation... installations) {
        var config = new MtaConfig();
        config.mtaInstallations = List.of(installations);
        return config;
    }
}

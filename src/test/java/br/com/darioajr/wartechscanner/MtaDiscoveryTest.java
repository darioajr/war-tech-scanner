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

import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class MtaDiscoveryTest {

    /** Engine name guaranteed not to resolve to a real binary, so process start fails fast. */
    private static final String BOGUS_ENGINE = "definitely-not-a-real-container-engine-xyz";

    @Test
    void openshiftReturnsStaticMappingNotice() {
        var inst = new MtaInstallation();
        inst.type = MtaInstallationType.OPENSHIFT;

        var caps = MtaDiscovery.discover(inst);

        assertTrue(caps.isEmpty());
        assertNotNull(caps.failureReason());
        assertTrue(caps.failureReason().contains("Hub API"));
    }

    @Test
    void bareMetalWithNullPathReportsBinaryNotFound() {
        var inst = new MtaInstallation();
        inst.type = MtaInstallationType.BARE_METAL;
        inst.path = null;

        var caps = MtaDiscovery.discover(inst);

        assertTrue(caps.isEmpty());
        assertTrue(caps.failureReason().contains("Binary not found"));
    }

    @Test
    void bareMetalWithMissingBinaryReportsBinaryNotFound() {
        var inst = new MtaInstallation();
        inst.type = MtaInstallationType.BARE_METAL;
        inst.path = "/no/such/mta-cli-binary-xyz";

        var caps = MtaDiscovery.discover(inst);

        assertTrue(caps.isEmpty());
        assertTrue(caps.failureReason().contains("Binary not found"));
    }

    @Test
    void containerWithNullImageReportsNoImage() {
        var inst = new MtaInstallation();
        inst.type = MtaInstallationType.CONTAINER;
        inst.image = null;

        var caps = MtaDiscovery.discover(inst);

        assertTrue(caps.isEmpty());
        assertEquals("No image configured", caps.failureReason());
    }

    @Test
    void containerWithBlankImageReportsNoImage() {
        var inst = new MtaInstallation();
        inst.type = MtaInstallationType.CONTAINER;
        inst.image = "   ";

        var caps = MtaDiscovery.discover(inst);

        assertTrue(caps.isEmpty());
        assertEquals("No image configured", caps.failureReason());
    }

    @Test
    void containerWithUnavailableImageSuggestsPull() {
        var inst = new MtaInstallation();
        inst.type = MtaInstallationType.CONTAINER;
        inst.image = "quay.io/konveyor/mta-cli:7.2";
        inst.containerEngine = BOGUS_ENGINE;

        var caps = MtaDiscovery.discover(inst);

        assertTrue(caps.isEmpty());
        assertTrue(caps.failureReason().contains("pull " + inst.image));
        assertFalse(caps.failureReason().contains("login"));
    }

    @Test
    void containerWithRedhatImageSuggestsLoginThenPull() {
        var inst = new MtaInstallation();
        inst.type = MtaInstallationType.CONTAINER;
        inst.image = "registry.redhat.io/mta/mta-cli-rhel9:7.2";
        inst.containerEngine = BOGUS_ENGINE;

        var caps = MtaDiscovery.discover(inst);

        assertTrue(caps.isEmpty());
        assertTrue(caps.failureReason().contains("login registry.redhat.io"));
        assertTrue(caps.failureReason().contains("pull " + inst.image));
    }

    @Test
    void parseLineExtractsIdentifierFromVariousFormats() {
        assertEquals("eap8", MtaDiscovery.parseLine("  eap8          JBoss EAP 8"));
        assertEquals("eap8", MtaDiscovery.parseLine("- eap8"));
        assertEquals("foo", MtaDiscovery.parseLine("* foo"));
        assertEquals("openjdk17", MtaDiscovery.parseLine("openjdk17"));
        assertEquals("AB", MtaDiscovery.parseLine("AB")); // all-caps but too short to be a header
    }

    @Test
    void parseLineRejectsNonIdentifierLines() {
        assertNull(MtaDiscovery.parseLine(""));
        assertNull(MtaDiscovery.parseLine("   "));
        assertNull(MtaDiscovery.parseLine("# comment"));
        assertNull(MtaDiscovery.parseLine("NAME")); // header (all-caps, length > 2)
        assertNull(MtaDiscovery.parseLine("ERROR: something failed")); // contains colon
        assertNull(MtaDiscovery.parseLine("path/to/thing")); // contains slash
        assertNull(MtaDiscovery.parseLine("path\\to\\thing")); // contains backslash
        assertNull(MtaDiscovery.parseLine("123abc")); // does not start with a letter
    }

    @Test
    void capabilitiesIsEmptyReflectsSourcesAndTargets() {
        var empty = new MtaDiscovery.Capabilities(
                new LinkedHashSet<>(), new LinkedHashSet<>(), new LinkedHashSet<>(), "x");
        assertTrue(empty.isEmpty());

        Set<String> sources = new LinkedHashSet<>(Set.of("java-ee"));
        var populated = new MtaDiscovery.Capabilities(
                sources, new LinkedHashSet<>(), new LinkedHashSet<>(), null);
        assertFalse(populated.isEmpty());
        assertEquals(sources, populated.sources());
        assertNull(populated.failureReason());
    }
}

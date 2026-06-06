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

import java.util.List;

import br.com.darioajr.wartechscanner.MigrationRiskAssessment.RiskLevel;

import static org.junit.jupiter.api.Assertions.*;

class MigrationRiskScorerTest {

    private static ScanResult withTechs(String... names) {
        var r = new ScanResult();
        for (var n : names) {
            var t = new DetectedTechnology(n);
            t.score = 10;
            r.technologies.add(t);
        }
        return r;
    }

    @Test
    void eap8AddsNamespaceFactor() {
        var r = withTechs();
        var a = MigrationRiskScorer.score(r, new MigrationTarget("8.0", 0),
                ComplexityClassifier.classify(r), List.of());
        assertTrue(a.factors().stream().anyMatch(f -> f.contains("namespace break")));
        assertTrue(a.score() >= 15);
    }

    @Test
    void legacyTechRaisesRiskOverModernTech() {
        var struts = withTechs("Struts");
        var servlet = withTechs("Servlet");
        var target = new MigrationTarget("8.0", 17);
        int strutsScore = MigrationRiskScorer.score(struts, target,
                ComplexityClassifier.classify(struts), List.of()).score();
        int servletScore = MigrationRiskScorer.score(servlet, target,
                ComplexityClassifier.classify(servlet), List.of()).score();
        assertTrue(strutsScore > servletScore);
    }

    @Test
    void vulnerabilitiesContributeButAreCapped() {
        var r = withTechs("Spring");
        var vulns = List.of(
                new VulnerableLibrary("a", "x", "1", "c", Severity.CRITICAL, "d", "f"),
                new VulnerableLibrary("b", "y", "1", "c", Severity.CRITICAL, "d", "f"),
                new VulnerableLibrary("c", "z", "1", "c", Severity.CRITICAL, "d", "f"),
                new VulnerableLibrary("d", "w", "1", "c", Severity.CRITICAL, "d", "f"));
        var a = MigrationRiskScorer.score(r, new MigrationTarget("8.0", 0),
                ComplexityClassifier.classify(r), vulns);
        // 4 * 10 = 40 raw, capped at 25
        assertTrue(a.factors().stream().anyMatch(f -> f.contains("+25)")));
    }

    @Test
    void javaJumpAddsFactors() {
        var r = withTechs();
        var a = MigrationRiskScorer.score(r, new MigrationTarget(null, 21),
                ComplexityClassifier.classify(r), List.of());
        assertTrue(a.factors().stream().anyMatch(f -> f.contains("Java 21")));
    }

    @Test
    void oldJavaTargetAddsNoJumpFactor() {
        var r = withTechs();
        var a = MigrationRiskScorer.score(r, new MigrationTarget(null, 11),
                ComplexityClassifier.classify(r), List.of());
        assertTrue(a.factors().stream().noneMatch(f -> f.contains("Java 11")));
    }

    @Test
    void toleratesNullComplexityAndNullVulns() {
        var r = withTechs("Servlet");
        var a = MigrationRiskScorer.score(r, new MigrationTarget("8.0", 0), null, null);
        assertNotNull(a.level());
        // no complexity factor when complexity is null
        assertTrue(a.factors().stream().noneMatch(f -> f.contains("intrinsic complexity")));
    }

    @Test
    void zeroComplexityAddsNoFactor() {
        var r = withTechs();   // empty -> complexity score 0
        var a = MigrationRiskScorer.score(r, new MigrationTarget("8.0", 0),
                ComplexityClassifier.classify(r), List.of());
        assertTrue(a.factors().stream().noneMatch(f -> f.contains("intrinsic complexity")));
    }

    @Test
    void riskLevelBuckets() {
        assertEquals(RiskLevel.LOW, RiskLevel.fromScore(10));
        assertEquals(RiskLevel.MODERATE, RiskLevel.fromScore(25));
        assertEquals(RiskLevel.HIGH, RiskLevel.fromScore(50));
        assertEquals(RiskLevel.CRITICAL, RiskLevel.fromScore(75));
    }

    @Test
    void scoreCappedAt100() {
        var r = withTechs("Struts", "EJB", "JAX-WS/SOAP", "JSF", "Hibernate", "CDI");
        var vulns = List.of(
                new VulnerableLibrary("a", "x", "1", "c", Severity.CRITICAL, "d", "f"),
                new VulnerableLibrary("b", "y", "1", "c", Severity.CRITICAL, "d", "f"),
                new VulnerableLibrary("c", "z", "1", "c", Severity.CRITICAL, "d", "f"));
        var a = MigrationRiskScorer.score(r, new MigrationTarget("8.0", 21),
                ComplexityClassifier.classify(r), vulns);
        assertTrue(a.score() <= 100);
    }
}

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

import static org.junit.jupiter.api.Assertions.*;

class ComplexityClassifierTest {

    private static void addTech(ScanResult r, String name) {
        var t = new DetectedTechnology(name);
        t.score = 10;
        r.technologies.add(t);
    }

    @Test
    void emptyArtifactIsLow() {
        var a = ComplexityClassifier.classify(new ScanResult());
        assertEquals(ComplexityLevel.LOW, a.level());
        assertEquals(0, a.score());
        assertTrue(a.factors().isEmpty());
    }

    @Test
    void scoreNeverExceeds100() {
        var r = new ScanResult();
        for (var t : new String[]{"EJB", "Struts", "JAX-WS/SOAP", "JSF", "Spring", "Hibernate"}) {
            addTech(r, t);
        }
        for (int i = 0; i < 200; i++) r.libraries.add("lib/x-" + i + ".jar");
        for (int i = 0; i < 600; i++) r.classesWithEvidence.add("c" + i);
        for (int i = 0; i < 20; i++) r.descriptors.add("d" + i);
        var a = ComplexityClassifier.classify(r);
        assertEquals(ComplexityLevel.VERY_HIGH, a.level());
        assertEquals(100, a.score());
    }

    @Test
    void heavyFrameworksRaiseScore() {
        var light = new ScanResult();
        addTech(light, "Servlet");
        var heavy = new ScanResult();
        addTech(heavy, "Struts");
        assertTrue(ComplexityClassifier.classify(heavy).score()
                > ComplexityClassifier.classify(light).score());
        assertTrue(ComplexityClassifier.classify(heavy).factors().stream()
                .anyMatch(f -> f.contains("heavy framework")));
    }

    @Test
    void midTierBucketsAwardPartialPoints() {
        var r = new ScanResult();
        addTech(r, "Servlet");
        for (int i = 0; i < 30; i++) r.libraries.add("lib/x-" + i + ".jar");   // >=20  -> +6
        for (int i = 0; i < 150; i++) r.classesWithEvidence.add("c" + i);       // >=100 -> +10
        for (int i = 0; i < 5; i++) r.descriptors.add("d" + i);                 // >=3   -> +3
        var a = ComplexityClassifier.classify(r);
        assertTrue(a.factors().stream().anyMatch(f -> f.contains("30 dependencies (+6)")));
        assertTrue(a.factors().stream().anyMatch(f -> f.contains("150 classes with framework evidence (+10)")));
        assertTrue(a.factors().stream().anyMatch(f -> f.contains("5 deployment descriptors (+3)")));
    }

    @Test
    void levelBucketsAreMonotonic() {
        assertEquals(ComplexityLevel.LOW, ComplexityLevel.fromScore(0));
        assertEquals(ComplexityLevel.LOW, ComplexityLevel.fromScore(24));
        assertEquals(ComplexityLevel.MODERATE, ComplexityLevel.fromScore(25));
        assertEquals(ComplexityLevel.HIGH, ComplexityLevel.fromScore(50));
        assertEquals(ComplexityLevel.VERY_HIGH, ComplexityLevel.fromScore(75));
        assertEquals(ComplexityLevel.VERY_HIGH, ComplexityLevel.fromScore(100));
    }
}

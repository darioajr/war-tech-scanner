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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import br.com.darioajr.wartechscanner.MigrationRiskAssessment.RiskLevel;

/**
 * Migration risk scoring: estimates how risky moving an artifact to a given
 * {@link MigrationTarget} is, on a 0-100 scale.
 *
 * <p>Inputs combined: intrinsic complexity, the {@code javax.* -> jakarta.*}
 * namespace break (EAP 8+), per-technology migration weight, the Java-version
 * jump, and the count/severity of vulnerable dependencies. Higher score = more
 * effort and more chance of regression.
 */
public final class MigrationRiskScorer {

    // Per-technology migration weight (effort + breakage risk).
    private static final Map<String, Integer> TECH_WEIGHT = Map.of(
            "Struts", 18,
            "EJB", 10,
            "JAX-WS/SOAP", 8,
            "JSF", 6,
            "Hibernate", 4,
            "CDI", 3
    );

    private static final int VULN_CAP = 25;

    private MigrationRiskScorer() {}

    public static MigrationRiskAssessment score(ScanResult result, MigrationTarget target,
                                                ComplexityAssessment complexity,
                                                List<VulnerableLibrary> vulns) {
        var factors = new ArrayList<String>();
        int score = 0;

        score += complexityContribution(complexity, factors);
        score += namespaceContribution(target, factors);
        score += techContribution(result, factors);
        score += javaJumpContribution(target, factors);
        score += vulnContribution(vulns, factors);

        score = Math.min(100, score);
        return new MigrationRiskAssessment(RiskLevel.fromScore(score), score, factors);
    }

    private static int complexityContribution(ComplexityAssessment complexity, List<String> factors) {
        if (complexity == null) return 0;
        int pts = (int) Math.round(complexity.score() * 0.30);
        if (pts > 0) {
            factors.add("intrinsic complexity %s (+%d)".formatted(complexity.level(), pts));
        }
        return pts;
    }

    private static int namespaceContribution(MigrationTarget target, List<String> factors) {
        if (target.isEap8OrLater()) {
            factors.add("javax.* -> jakarta.* namespace break on EAP %s (+15)".formatted(target.eapVersion()));
            return 15;
        }
        return 0;
    }

    private static int techContribution(ScanResult result, List<String> factors) {
        int pts = 0;
        for (var t : result.technologies) {
            var w = TECH_WEIGHT.get(t.name);
            if (w != null) {
                pts += w;
                factors.add("%s migration effort (+%d)".formatted(t.name, w));
            }
        }
        return pts;
    }

    private static int javaJumpContribution(MigrationTarget target, List<String> factors) {
        if (!target.hasJavaVersion()) return 0;
        int pts = 0;
        if (target.javaVersion() >= 17) {
            pts += 8;
            factors.add("Java %d strong encapsulation (+8)".formatted(target.javaVersion()));
        }
        if (target.javaVersion() >= 21) {
            pts += 4;
            factors.add("Java %d API/runtime changes (+4)".formatted(target.javaVersion()));
        }
        return pts;
    }

    private static int vulnContribution(List<VulnerableLibrary> vulns, List<String> factors) {
        if (vulns == null || vulns.isEmpty()) return 0;
        int raw = vulns.stream().mapToInt(v -> v.severity().weight).sum();
        int pts = Math.min(VULN_CAP, raw);
        factors.add("%d vulnerable dependency(ies) (+%d)".formatted(vulns.size(), pts));
        return pts;
    }
}

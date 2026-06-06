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
import java.util.Set;

/**
 * Automatic complexity classification of a scanned artifact.
 *
 * <p>Combines the breadth of detected technologies, the volume of bytecode and
 * descriptors, the size of the dependency set, and the presence of historically
 * heavy frameworks into a single 0-100 index, then buckets it into a
 * {@link ComplexityLevel}. The {@code factors} explain what drove the score.
 */
public final class ComplexityClassifier {

    // Frameworks that disproportionately raise migration/maintenance effort.
    private static final Set<String> HEAVY_TECHS = Set.of("EJB", "Struts", "JAX-WS/SOAP");

    /**
     * Three-tier scoring bucket: once {@code value} reaches a threshold it awards the
     * matching points. Thresholds and points are ascending ({@code t1<t2<t3}).
     */
    private record Bucket(int t1, int t2, int t3, int p1, int p2, int p3, String label) {
        int score(int value, List<String> factors) {
            int pts;
            if (value >= t3)      pts = p3;
            else if (value >= t2) pts = p2;
            else if (value >= t1) pts = p1;
            else                  return 0;
            factors.add("%d %s (+%d)".formatted(value, label, pts));
            return pts;
        }
    }

    private static final Bucket LIBS        = new Bucket(20, 50, 100, 6, 12, 20, "dependencies");
    private static final Bucket CLASSES     = new Bucket(20, 100, 500, 4, 10, 20, "classes with framework evidence");
    private static final Bucket DESCRIPTORS = new Bucket(3, 8, 15, 3, 6, 10, "deployment descriptors");

    private ComplexityClassifier() {}

    public static ComplexityAssessment classify(ScanResult result) {
        var factors = new ArrayList<String>();
        int score = 0;

        score += scoreTechBreadth(result, factors);
        score += scoreHeavyTechs(result, factors);
        score += LIBS.score(result.libraries.size(), factors);
        score += CLASSES.score(result.classesWithEvidence.size(), factors);
        score += DESCRIPTORS.score(result.descriptors.size(), factors);

        score = Math.min(100, score);
        return new ComplexityAssessment(ComplexityLevel.fromScore(score), score, factors);
    }

    private static int scoreTechBreadth(ScanResult result, List<String> factors) {
        int n = result.technologies.size();
        if (n == 0) return 0;
        int pts = Math.min(30, n * 6);
        factors.add("%d distinct technologies (+%d)".formatted(n, pts));
        return pts;
    }

    private static int scoreHeavyTechs(ScanResult result, List<String> factors) {
        int pts = 0;
        for (var t : result.technologies) {
            if (HEAVY_TECHS.contains(t.name)) {
                pts += 8;
                factors.add("heavy framework: %s (+8)".formatted(t.name));
            }
        }
        return pts;
    }
}

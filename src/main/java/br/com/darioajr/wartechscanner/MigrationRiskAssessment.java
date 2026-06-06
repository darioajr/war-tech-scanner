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

import java.util.List;

/** Result of migration risk scoring against a {@link MigrationTarget}. */
public record MigrationRiskAssessment(RiskLevel level, int score, List<String> factors) {

    /** Coarse risk bucket derived from the numeric risk index (0-100). */
    public enum RiskLevel {
        LOW,
        MODERATE,
        HIGH,
        CRITICAL;

        static RiskLevel fromScore(int score) {
            if (score < 25) return LOW;
            if (score < 50) return MODERATE;
            if (score < 75) return HIGH;
            return CRITICAL;
        }
    }
}

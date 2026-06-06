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

/** Coarse complexity bucket derived from the numeric complexity index (0-100). */
public enum ComplexityLevel {
    LOW,
    MODERATE,
    HIGH,
    VERY_HIGH;

    static ComplexityLevel fromScore(int score) {
        if (score < 25) return LOW;
        if (score < 50) return MODERATE;
        if (score < 75) return HIGH;
        return VERY_HIGH;
    }
}

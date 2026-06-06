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

/** CVSS-aligned severity buckets used for vulnerable libraries and risk weighting. */
public enum Severity {
    LOW(2),
    MEDIUM(4),
    HIGH(7),
    CRITICAL(10);

    /** Points this severity contributes to the migration risk score. */
    final int weight;

    Severity(int weight) {
        this.weight = weight;
    }
}

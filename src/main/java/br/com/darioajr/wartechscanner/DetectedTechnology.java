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

import java.util.SequencedSet;
import java.util.LinkedHashSet;

public final class DetectedTechnology {
    public String name;
    public int score;
    public final SequencedSet<String> evidences = new LinkedHashSet<>();

    public DetectedTechnology() {}

    public DetectedTechnology(String name) {
        this.name = name;
    }

    public void addEvidence(String evidence, int points) {
        evidences.add(evidence);
        score += points;
    }
}

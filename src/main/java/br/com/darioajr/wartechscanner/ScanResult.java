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

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.SequencedCollection;
import java.util.Collections;

public final class ScanResult {
    public String artifact;
    public String artifactType;
    public Instant scannedAt = Instant.now();
    public final List<DetectedTechnology> technologies = new ArrayList<>();
    public final SequencedCollection<String> descriptors = new ArrayList<>();
    public final SequencedCollection<String> libraries = new ArrayList<>();
    public final SequencedCollection<String> classesWithEvidence = new ArrayList<>();
    public final SequencedCollection<String> warnings = new ArrayList<>();
    public final SequencedCollection<String> migrationHints = new ArrayList<>();
    public List<MtaSuggestion> mtaSuggestions;
}

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

/**
 * A library entry flagged against the built-in vulnerability catalog.
 *
 * <p>Detection is filename + version heuristic only (no checksum / SBOM lookup),
 * so results are advisory: confirm against an authoritative source such as the
 * GitHub Advisory Database or OWASP Dependency-Check before acting.
 */
public record VulnerableLibrary(
        String entry,        // archive path where the jar was found
        String artifact,     // parsed artifactId (e.g. log4j-core)
        String version,      // parsed version (e.g. 2.14.1)
        String cve,          // advisory id(s)
        Severity severity,
        String description,
        String fixedIn) {    // first fixed version, or "n/a" for EOL components
}

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

public final class CompatibilityAdvisor {

    private static final String HIBERNATE = "Hibernate";
    private static final String SPRING    = "Spring";

    /** A technology and the hint template(s) emitted when it is detected. */
    private record TechHint(String tech, List<String> templates) {
        TechHint(String tech, String template) { this(tech, List.of(template)); }
    }

    // Hints emitted for EAP 8+ (Jakarta EE 10), in priority order.
    private static final List<TechHint> EAP8_TECH_HINTS = List.of(
            new TechHint("EJB", List.of(
                    "[EAP %s] EJB: entity beans (EJB 2.x) were removed; use JPA entities. Stateful/Stateless/Singleton EJBs remain supported.",
                    "[EAP %s] EJB: replace javax.ejb.* with jakarta.ejb.*")),
            new TechHint("JPA",
                    "[EAP %s] JPA: migrate javax.persistence.* → jakarta.persistence.*; Hibernate ORM 6.x breaks compatibility with legacy mappings."),
            new TechHint(HIBERNATE,
                    "[EAP %s] Hibernate: ORM 6.x removed deprecated APIs (legacy Criteria, SessionFactory XML mappings); review .hbm.xml files."),
            new TechHint("CDI",
                    "[EAP %s] CDI: replace javax.inject.*/javax.enterprise.* with jakarta.inject.*/jakarta.enterprise.*; beans.xml is now optional (bean-discovery-mode=annotated by default)."),
            new TechHint("JSF",
                    "[EAP %s] JSF was renamed to Jakarta Faces; replace javax.faces.* with jakarta.faces.*; Facelets remains the default."),
            new TechHint("JAX-RS",
                    "[EAP %s] JAX-RS 3.1 (RESTEasy): replace javax.ws.rs.* with jakarta.ws.rs.*; Response.readEntity() throws a checked IOException."),
            new TechHint("JAX-WS/SOAP",
                    "[EAP %s] JAX-WS: replace javax.jws.*/javax.xml.ws.* with jakarta.jws.*/jakarta.xml.ws.*"),
            new TechHint("Servlet",
                    "[EAP %s] Servlet 6.0: replace javax.servlet.* with jakarta.servlet.*; HttpServletRequest.isRequestedSessionIdFromUrl() removed."),
            new TechHint(SPRING,
                    "[EAP %s] Spring on EAP 8: if deployed as a WAR, make sure to use Spring 6.x (which already uses jakarta.*); potential conflict with Weld/CDI."),
            new TechHint("Struts",
                    "[EAP %s] Struts 1.x is not compatible with Jakarta EE 10 (uses javax.servlet.*); migrate to Struts 2.5+ or replace the framework.")
    );

    // Hints emitted for EAP 7 (Java EE 8, still javax.*).
    private static final List<TechHint> EAP7_TECH_HINTS = List.of(
            new TechHint("EJB",
                    "[EAP %s] EJB: entity beans (EJB 2.x) were removed; check usage of javax.ejb.EJBObject/EJBHome."),
            new TechHint(HIBERNATE,
                    "[EAP %s] Hibernate: version 5.x included; Hibernate 4.x APIs may be deprecated."),
            new TechHint("JSF",
                    "[EAP %s] JSF 2.3 included; check compatibility of third-party components (PrimeFaces, RichFaces).")
    );

    private CompatibilityAdvisor() {}

    public static List<String> advise(ScanResult result, MigrationTarget target) {
        var hints = new ArrayList<String>();

        if (target.hasEapVersion()) {
            hintsForEap(result, target, hints);
        }
        if (target.hasJavaVersion()) {
            hintsForJava(result, target, hints);
        }
        return hints;
    }


    private static void hintsForEap(ScanResult result, MigrationTarget target, List<String> hints) {
        var detected = result.technologies.stream().map(t -> t.name).toList();
        String v = target.eapVersion();

        if (target.isEap8OrLater()) {
            hints.add("[EAP %s] Namespace javax.* → jakarta.*: all dependencies and imports must be migrated to jakarta.*"
                    .formatted(v));
            addTechHints(detected, hints, v, EAP8_TECH_HINTS);
        } else if (target.isEap7()) {
            hints.add("[EAP %s] Namespace: javax.* is still used; jakarta.* is NOT supported in this version."
                    .formatted(v));
            addTechHints(detected, hints, v, EAP7_TECH_HINTS);
        }
    }

    private static void addTechHints(List<String> detected, List<String> hints,
                                     String eapVersion, List<TechHint> rules) {
        for (var rule : rules) {
            if (detected.contains(rule.tech())) {
                for (var template : rule.templates()) {
                    hints.add(template.formatted(eapVersion));
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Java-version-specific rules
    // -------------------------------------------------------------------------

    private static void hintsForJava(ScanResult result, MigrationTarget target, List<String> hints) {
        int java = target.javaVersion();
        var detected = result.technologies.stream().map(t -> t.name).toList();

        if (java >= 17) {
            hints.add("[Java %d] strong encapsulation: reflection on internal APIs (sun.*, com.sun.*) fails without --add-opens; review ASM/CGLIB bytecode."
                    .formatted(java));

            if (detected.contains(SPRING)) {
                hints.add("[Java %d] Spring: minimum recommended version is 5.3.x (Java 17) or 6.x (Java 17+); Spring 4.x is not compatible."
                        .formatted(java));
            }
            if (detected.contains(HIBERNATE)) {
                hints.add("[Java %d] Hibernate: version 5.6+ for Java 17; Hibernate 6.x for Java 17/21."
                        .formatted(java));
            }
        }

        if (java >= 21) {
            hints.add("[Java %d] Virtual threads available; consider replacing fixed thread pools with Executors.newVirtualThreadPerTaskExecutor()."
                    .formatted(java));
            hints.add("[Java %d] Sequenced Collections: List/Set/Map now implement SequencedCollection/SequencedSet/SequencedMap."
                    .formatted(java));

            if (detected.contains(SPRING)) {
                hints.add("[Java %d] Spring: version 6.1+ recommended for virtual threads and Project Loom support."
                        .formatted(java));
            }
            if (detected.contains(HIBERNATE)) {
                hints.add("[Java %d] Hibernate 6.4+ supports virtual threads and Records as embeddables."
                        .formatted(java));
            }
        }

        if (java >= 11 && java < 17) {
            hints.add("[Java %d] Java 9+ modules may require --add-modules or --add-opens for removed APIs (JAXB, JAX-WS, Corba)."
                    .formatted(java));
            if (detected.contains("JAX-WS/SOAP")) {
                hints.add("[Java %d] JAX-WS was removed from the JDK; add an explicit dependency (jakarta.xml.ws-api or com.sun.xml.ws:jaxws-ri)."
                        .formatted(java));
            }
        }
    }
}

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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.SequencedMap;

final class TechnologyCatalog {
    private TechnologyCatalog() {}

    // ── technology names (single source of truth, avoids duplicated literals) ──
    private static final String EJB       = "EJB";
    private static final String JPA       = "JPA";
    private static final String HIBERNATE = "Hibernate";
    private static final String CDI       = "CDI";
    private static final String JSF       = "JSF";
    private static final String JAX_RS    = "JAX-RS";
    private static final String JAX_WS    = "JAX-WS/SOAP";
    private static final String SERVLET   = "Servlet";
    private static final String SPRING    = "Spring";
    private static final String STRUTS    = "Struts";

    private static final String[] ALL_TECHNOLOGIES = {
            EJB, JPA, HIBERNATE, CDI, JSF, JAX_RS, JAX_WS, SERVLET, SPRING, STRUTS
    };

    /** A technology matched when the inspected string contains any of its needles. */
    private record Rule(String tech, List<String> needles) {}

    private static final List<Rule> ANNOTATION_RULES = List.of(
            new Rule(EJB,       List.of("javax/ejb/", "jakarta/ejb/")),
            new Rule(JPA,       List.of("javax/persistence/", "jakarta/persistence/")),
            new Rule(HIBERNATE, List.of("org/hibernate/")),
            new Rule(CDI,       List.of("javax/inject/", "jakarta/inject/", "javax/enterprise/", "jakarta/enterprise/")),
            new Rule(JSF,       List.of("javax/faces/", "jakarta/faces/")),
            new Rule(JAX_RS,    List.of("javax/ws/rs/", "jakarta/ws/rs/")),
            new Rule(JAX_WS,    List.of("javax/jws/", "jakarta/jws/", "javax/xml/ws/", "jakarta/xml/ws/")),
            new Rule(SERVLET,   List.of("javax/servlet/", "jakarta/servlet/")),
            new Rule(SPRING,    List.of("org/springframework/")),
            new Rule(STRUTS,    List.of("org/apache/struts"))
    );

    private static final List<Rule> LIBRARY_RULES = List.of(
            new Rule(HIBERNATE, List.of("hibernate-core", "hibernate-entitymanager")),
            new Rule(EJB,       List.of("jakarta.ejb", "javax.ejb", "ejb-api")),
            new Rule(JPA,       List.of("jakarta.persistence", "javax.persistence", "jpa-api")),
            new Rule(CDI,       List.of("weld", "cdi-api", "jakarta.enterprise")),
            new Rule(JSF,       List.of("jsf", "faces")),
            new Rule(JAX_RS,    List.of("resteasy", "jersey", "jaxrs", "jax-rs")),
            new Rule(JAX_WS,    List.of("cxf", "jaxws", "jax-ws", "axis")),
            new Rule(SERVLET,   List.of("servlet-api", "jakarta.servlet")),
            new Rule(SPRING,    List.of("spring-")),
            new Rule(STRUTS,    List.of("struts"))
    );

    static SequencedMap<String, DetectedTechnology> create() {
        var map = new LinkedHashMap<String, DetectedTechnology>();
        for (var name : ALL_TECHNOLOGIES) {
            map.put(name, new DetectedTechnology(name));
        }
        return map;
    }

    static String techByAnnotation(String descOrType) {
        return firstMatch(descOrType.replace('.', '/'), ANNOTATION_RULES);
    }

    static String techByLibrary(String fileName) {
        return firstMatch(fileName, LIBRARY_RULES);
    }

    private static String firstMatch(String haystack, List<Rule> rules) {
        for (var rule : rules) {
            for (var needle : rule.needles()) {
                if (haystack.contains(needle)) {
                    return rule.tech();
                }
            }
        }
        return null;
    }

    static String techByPath(String path) {
        if (path.endsWith("ejb-jar.xml") || path.endsWith("jboss-ejb3.xml")) return EJB;
        if (path.endsWith("persistence.xml")) return JPA;
        if (path.endsWith("hibernate.cfg.xml") || path.endsWith(".hbm.xml")) return HIBERNATE;
        if (path.endsWith("beans.xml")) return CDI;
        if (path.endsWith("faces-config.xml") || path.endsWith(".xhtml")) return JSF;
        if (path.endsWith("web.xml")) return SERVLET;
        if (path.endsWith("applicationcontext.xml") || path.contains("spring")) return SPRING;
        if (path.endsWith("struts.xml")) return STRUTS;
        return null;
    }
}

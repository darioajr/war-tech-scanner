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
import java.util.Map;
import java.util.SequencedMap;

final class TechnologyCatalog {
    private TechnologyCatalog() {}

    static SequencedMap<String, DetectedTechnology> create() {
        var map = new LinkedHashMap<String, DetectedTechnology>();
        for (var name : new String[]{
                "EJB", "JPA", "Hibernate", "CDI", "JSF", "JAX-RS", "JAX-WS/SOAP", "Servlet", "Spring", "Struts"
        }) {
            map.put(name, new DetectedTechnology(name));
        }
        return map;
    }

    static String techByAnnotation(String descOrType) {
        var v = descOrType.replace('.', '/');
        if (v.contains("javax/ejb/") || v.contains("jakarta/ejb/")) return "EJB";
        if (v.contains("javax/persistence/") || v.contains("jakarta/persistence/")) return "JPA";
        if (v.contains("org/hibernate/")) return "Hibernate";
        if (v.contains("javax/inject/") || v.contains("jakarta/inject/")
                || v.contains("javax/enterprise/") || v.contains("jakarta/enterprise/")) return "CDI";
        if (v.contains("javax/faces/") || v.contains("jakarta/faces/")) return "JSF";
        if (v.contains("javax/ws/rs/") || v.contains("jakarta/ws/rs/")) return "JAX-RS";
        if (v.contains("javax/jws/") || v.contains("jakarta/jws/")
                || v.contains("javax/xml/ws/") || v.contains("jakarta/xml/ws/")) return "JAX-WS/SOAP";
        if (v.contains("javax/servlet/") || v.contains("jakarta/servlet/")) return "Servlet";
        if (v.contains("org/springframework/")) return "Spring";
        if (v.contains("org/apache/struts")) return "Struts";
        return null;
    }

    static String techByPath(String path) {
        if (path.endsWith("ejb-jar.xml") || path.endsWith("jboss-ejb3.xml")) return "EJB";
        if (path.endsWith("persistence.xml")) return "JPA";
        if (path.endsWith("hibernate.cfg.xml") || path.endsWith(".hbm.xml")) return "Hibernate";
        if (path.endsWith("beans.xml")) return "CDI";
        if (path.endsWith("faces-config.xml") || path.endsWith(".xhtml")) return "JSF";
        if (path.endsWith("web.xml")) return "Servlet";
        if (path.endsWith("applicationcontext.xml") || path.contains("spring")) return "Spring";
        if (path.endsWith("struts.xml")) return "Struts";
        return null;
    }

    static String techByLibrary(String fileName) {
        if (fileName.contains("hibernate-core") || fileName.contains("hibernate-entitymanager")) return "Hibernate";
        if (fileName.contains("jakarta.ejb") || fileName.contains("javax.ejb") || fileName.contains("ejb-api")) return "EJB";
        if (fileName.contains("jakarta.persistence") || fileName.contains("javax.persistence") || fileName.contains("jpa-api")) return "JPA";
        if (fileName.contains("weld") || fileName.contains("cdi-api") || fileName.contains("jakarta.enterprise")) return "CDI";
        if (fileName.contains("jsf") || fileName.contains("faces")) return "JSF";
        if (fileName.contains("resteasy") || fileName.contains("jersey") || fileName.contains("jaxrs") || fileName.contains("jax-rs")) return "JAX-RS";
        if (fileName.contains("cxf") || fileName.contains("jaxws") || fileName.contains("jax-ws") || fileName.contains("axis")) return "JAX-WS/SOAP";
        if (fileName.contains("servlet-api") || fileName.contains("jakarta.servlet")) return "Servlet";
        if (fileName.contains("spring-")) return "Spring";
        if (fileName.contains("struts")) return "Struts";
        return null;
    }
}

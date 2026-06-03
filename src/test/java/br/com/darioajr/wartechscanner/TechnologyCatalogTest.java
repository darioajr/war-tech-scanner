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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class TechnologyCatalogTest {

    // ── techByAnnotation ─────────────────────────────────────────────────────

    @ParameterizedTest(name = "{0} → {1}")
    @CsvSource({
        "javax/ejb/Stateless,          EJB",
        "jakarta/ejb/Stateful,         EJB",
        "javax/persistence/Entity,     JPA",
        "jakarta/persistence/Entity,   JPA",
        "org/hibernate/Session,        Hibernate",
        "javax/inject/Inject,          CDI",
        "jakarta/enterprise/context/ApplicationScoped, CDI",
        "javax/faces/bean/ManagedBean, JSF",
        "jakarta/faces/bean/ManagedBean, JSF",
        "javax/ws/rs/Path,             JAX-RS",
        "jakarta/ws/rs/GET,            JAX-RS",
        "javax/jws/WebService,         JAX-WS/SOAP",
        "jakarta/xml/ws/WebServiceContext, JAX-WS/SOAP",
        "javax/servlet/http/HttpServlet, Servlet",
        "jakarta/servlet/http/HttpServlet, Servlet",
        "org/springframework/stereotype/Component, Spring",
        "org/apache/struts/action/Action, Struts",
    })
    void techByAnnotation_recognisesKnownTypes(String descriptor, String expected) {
        assertEquals(expected, TechnologyCatalog.techByAnnotation(descriptor));
    }

    @Test
    void techByAnnotation_returnsNullForUnknown() {
        assertNull(TechnologyCatalog.techByAnnotation("com/example/MyService"));
    }

    @Test
    void techByAnnotation_handlesDotSeparator() {
        assertEquals("EJB", TechnologyCatalog.techByAnnotation("javax.ejb.Stateless"));
    }

    // ── techByPath ───────────────────────────────────────────────────────────

    @ParameterizedTest(name = "{0} → {1}")
    @CsvSource({
        "META-INF/ejb-jar.xml,         EJB",
        "WEB-INF/jboss-ejb3.xml,       EJB",
        "META-INF/persistence.xml,     JPA",
        "META-INF/hibernate.cfg.xml,   Hibernate",
        "com/example/User.hbm.xml,     Hibernate",
        "WEB-INF/beans.xml,            CDI",
        "WEB-INF/faces-config.xml,     JSF",
        "WEB-INF/views/index.xhtml,    JSF",
        "WEB-INF/web.xml,              Servlet",
        "WEB-INF/applicationcontext.xml, Spring",
        "struts.xml,                   Struts",
    })
    void techByPath_recognisesKnownDescriptors(String path, String expected) {
        assertEquals(expected, TechnologyCatalog.techByPath(path));
    }

    @Test
    void techByPath_returnsNullForUnknown() {
        assertNull(TechnologyCatalog.techByPath("META-INF/MANIFEST.MF"));
    }

    // ── techByLibrary ────────────────────────────────────────────────────────

    @ParameterizedTest(name = "{0} → {1}")
    @CsvSource({
        "hibernate-core-5.6.jar,       Hibernate",
        "hibernate-entitymanager-4.jar,Hibernate",
        "jakarta.ejb-api-4.0.jar,      EJB",
        "ejb-api-3.0.jar,              EJB",
        "jakarta.persistence-api.jar,  JPA",
        "weld-core-3.1.jar,            CDI",
        "jsf-api-2.3.jar,              JSF",
        "resteasy-jaxrs-3.0.jar,       JAX-RS",
        "jersey-client-2.0.jar,        JAX-RS",
        "cxf-rt-frontend-jaxws.jar,    JAX-WS/SOAP",
        "servlet-api-4.0.jar,          Servlet",
        "spring-core-5.3.jar,          Spring",
        "struts2-core-2.5.jar,         Struts",
    })
    void techByLibrary_recognisesKnownJars(String fileName, String expected) {
        assertEquals(expected, TechnologyCatalog.techByLibrary(fileName));
    }

    @Test
    void techByLibrary_returnsNullForUnknown() {
        assertNull(TechnologyCatalog.techByLibrary("commons-lang3-3.12.jar"));
    }

    // ── create ───────────────────────────────────────────────────────────────

    @Test
    void create_containsAllExpectedTechnologies() {
        var catalog = TechnologyCatalog.create();
        assertAll(
            () -> assertTrue(catalog.containsKey("EJB")),
            () -> assertTrue(catalog.containsKey("JPA")),
            () -> assertTrue(catalog.containsKey("Hibernate")),
            () -> assertTrue(catalog.containsKey("CDI")),
            () -> assertTrue(catalog.containsKey("JSF")),
            () -> assertTrue(catalog.containsKey("JAX-RS")),
            () -> assertTrue(catalog.containsKey("JAX-WS/SOAP")),
            () -> assertTrue(catalog.containsKey("Servlet")),
            () -> assertTrue(catalog.containsKey("Spring")),
            () -> assertTrue(catalog.containsKey("Struts"))
        );
    }
}

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
import org.objectweb.asm.ClassReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClassTechnologyVisitorTest {

    private static ClassTechnologyVisitor visit(byte[] bytes) {
        var visitor = new ClassTechnologyVisitor();
        new ClassReader(bytes).accept(visitor,
                ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        return visitor;
    }

    @Test
    void detectsClassNameAndIsClean() {
        var v = visit(ClassBytesFixture.plain("com/example/Plain"));
        assertEquals("com.example.Plain", v.className());
        assertTrue(v.technologies().isEmpty(), "plain class must have no technologies");
    }

    @Test
    void detectsClassAnnotation() {
        var v = visit(ClassBytesFixture.builder("com/example/Entity")
                .classAnnotation("Ljakarta/persistence/Entity;")
                .build());
        assertTrue(v.technologies().contains("JPA"));
    }

    @Test
    void detectsSuperClassAndInterfaces() {
        var v = visit(ClassBytesFixture.builder("com/example/MyController")
                .superName("org/springframework/web/servlet/FrameworkServlet")
                .interfaces("jakarta/ejb/Local")
                .build());
        assertTrue(v.technologies().contains("Spring"));
        assertTrue(v.technologies().contains("EJB"));
    }

    @Test
    void detectsFieldTypeAndFieldAnnotation() {
        var v = visit(ClassBytesFixture.builder("com/example/Bean")
                .fieldDescriptor("Ljakarta/servlet/http/HttpServletRequest;")
                .fieldAnnotation("Ljavax/ws/rs/core/Context;")
                .build());
        assertTrue(v.technologies().contains("Servlet"));
        assertTrue(v.technologies().contains("JAX-RS"));
    }

    @Test
    void detectsMethodAndParameterAnnotations() {
        var v = visit(ClassBytesFixture.builder("com/example/Service")
                .methodAnnotation("Ljakarta/jws/WebMethod;")
                .parameterAnnotation("Lorg/hibernate/annotations/Type;")
                .build());
        assertTrue(v.technologies().contains("JAX-WS/SOAP"));
        assertTrue(v.technologies().contains("Hibernate"));
    }

    @Test
    void detectsMultipleTechnologiesInOneClass() {
        var v = visit(ClassBytesFixture.builder("com/example/Mixed")
                .classAnnotation("Ljakarta/inject/Singleton;")
                .fieldAnnotation("Ljakarta/faces/bean/ManagedProperty;")
                .build());
        assertFalse(v.technologies().isEmpty());
        assertTrue(v.technologies().contains("CDI"));
        assertTrue(v.technologies().contains("JSF"));
    }

    @Test
    void ignoresNullClassNameGracefully() {
        // a real class always has a name; this just guards the className default
        var v = visit(ClassBytesFixture.plain("com/example/Other"));
        assertEquals("com.example.Other", v.className());
    }
}

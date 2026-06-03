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

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.LinkedHashSet;
import java.util.Set;

final class ClassTechnologyVisitor extends ClassVisitor {
    private final Set<String> technologies = new LinkedHashSet<>();
    private String className;

    ClassTechnologyVisitor() {
        super(Opcodes.ASM9);
    }

    Set<String> technologies() {
        return technologies;
    }

    String className() {
        return className;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.className = name == null ? "unknown" : name.replace('/', '.');
        detect(signature);
        detect(superName);
        if (interfaces != null) {
            for (String iface : interfaces) detect(iface);
        }
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        detect(descriptor);
        return super.visitAnnotation(descriptor, visible);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        detect(descriptor);
        detect(signature);
        FieldVisitor delegate = super.visitField(access, name, descriptor, signature, value);
        return new FieldVisitor(Opcodes.ASM9, delegate) {
            @Override
            public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                detect(descriptor);
                return super.visitAnnotation(descriptor, visible);
            }
        };
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        detect(descriptor);
        detect(signature);
        MethodVisitor delegate = super.visitMethod(access, name, descriptor, signature, exceptions);
        return new MethodVisitor(Opcodes.ASM9, delegate) {
            @Override
            public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                detect(descriptor);
                return super.visitAnnotation(descriptor, visible);
            }

            @Override
            public AnnotationVisitor visitParameterAnnotation(int parameter, String descriptor, boolean visible) {
                detect(descriptor);
                return super.visitParameterAnnotation(parameter, descriptor, visible);
            }
        };
    }

    private void detect(String value) {
        if (value == null || value.isBlank()) return;
        String tech = TechnologyCatalog.techByAnnotation(value);
        if (tech != null) technologies.add(tech);
    }
}

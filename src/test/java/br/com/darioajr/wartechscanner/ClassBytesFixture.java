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

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Generates valid {@code .class} bytecode on the fly (via ASM) so detection
 * logic can be tested without shipping pre-compiled fixture classes.
 */
final class ClassBytesFixture {

    private ClassBytesFixture() {}

    static byte[] plain(String internalName) {
        return builder(internalName).build();
    }

    static Builder builder(String internalName) {
        return new Builder(internalName);
    }

    static final class Builder {
        private final String internalName;
        private String superName = "java/lang/Object";
        private String[] interfaces = null;
        private String classAnnotation;
        private String fieldDescriptor = "I";
        private String fieldAnnotation;
        private String methodAnnotation;
        private String parameterAnnotation;

        private Builder(String internalName) {
            this.internalName = internalName;
        }

        Builder superName(String s) { this.superName = s; return this; }
        Builder interfaces(String... s) { this.interfaces = s; return this; }
        Builder classAnnotation(String s) { this.classAnnotation = s; return this; }
        Builder fieldDescriptor(String s) { this.fieldDescriptor = s; return this; }
        Builder fieldAnnotation(String s) { this.fieldAnnotation = s; return this; }
        Builder methodAnnotation(String s) { this.methodAnnotation = s; return this; }
        Builder parameterAnnotation(String s) { this.parameterAnnotation = s; return this; }

        byte[] build() {
            var cw = new ClassWriter(0);
            cw.visit(Opcodes.V17, Opcodes.ACC_PUBLIC, internalName, null, superName, interfaces);

            if (classAnnotation != null) {
                cw.visitAnnotation(classAnnotation, true).visitEnd();
            }

            FieldVisitor fv = cw.visitField(Opcodes.ACC_PRIVATE, "field", fieldDescriptor, null, null);
            if (fieldAnnotation != null) {
                fv.visitAnnotation(fieldAnnotation, true).visitEnd();
            }
            fv.visitEnd();

            MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "method", "(I)V", null, null);
            if (methodAnnotation != null) {
                mv.visitAnnotation(methodAnnotation, true).visitEnd();
            }
            if (parameterAnnotation != null) {
                mv.visitParameterAnnotation(0, parameterAnnotation, true).visitEnd();
            }
            mv.visitCode();
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();

            cw.visitEnd();
            return cw.toByteArray();
        }
    }
}

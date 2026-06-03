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

        if (target.isEap8OrLater()) {
            hints.add("[EAP %s] Namespace javax.* → jakarta.*: todas as dependências e imports devem ser migrados para jakarta.*"
                    .formatted(target.eapVersion()));

            if (detected.contains("EJB")) {
                hints.add("[EAP %s] EJB: entity beans (EJB 2.x) foram removidos; use JPA entities. Stateful/Stateless/Singleton EJBs continuam suportados."
                        .formatted(target.eapVersion()));
                hints.add("[EAP %s] EJB: substituir javax.ejb.* por jakarta.ejb.*"
                        .formatted(target.eapVersion()));
            }
            if (detected.contains("JPA")) {
                hints.add("[EAP %s] JPA: migrar javax.persistence.* → jakarta.persistence.*; Hibernate ORM 6.x quebra compatibilidade com mapeamentos legados."
                        .formatted(target.eapVersion()));
            }
            if (detected.contains("Hibernate")) {
                hints.add("[EAP %s] Hibernate: ORM 6.x removeu APIs depreciadas (Criteria legada, SessionFactory XML mappings); revisar arquivos .hbm.xml."
                        .formatted(target.eapVersion()));
            }
            if (detected.contains("CDI")) {
                hints.add("[EAP %s] CDI: substituir javax.inject.*/javax.enterprise.* por jakarta.inject.*/jakarta.enterprise.*; beans.xml agora é opcional (bean-discovery-mode=annotated por padrão)."
                        .formatted(target.eapVersion()));
            }
            if (detected.contains("JSF")) {
                hints.add("[EAP %s] JSF foi renomeado para Jakarta Faces; substituir javax.faces.* por jakarta.faces.*; Facelets continua padrão."
                        .formatted(target.eapVersion()));
            }
            if (detected.contains("JAX-RS")) {
                hints.add("[EAP %s] JAX-RS 3.1 (RESTEasy): substituir javax.ws.rs.* por jakarta.ws.rs.*; Response.readEntity() retorna IOException verificada."
                        .formatted(target.eapVersion()));
            }
            if (detected.contains("JAX-WS/SOAP")) {
                hints.add("[EAP %s] JAX-WS: substituir javax.jws.*/javax.xml.ws.* por jakarta.jws.*/jakarta.xml.ws.*"
                        .formatted(target.eapVersion()));
            }
            if (detected.contains("Servlet")) {
                hints.add("[EAP %s] Servlet 6.0: substituir javax.servlet.* por jakarta.servlet.*; HttpServletRequest.isRequestedSessionIdFromUrl() removido."
                        .formatted(target.eapVersion()));
            }
            if (detected.contains("Spring")) {
                hints.add("[EAP %s] Spring no EAP 8: se deployado como WAR, certifique-se de usar Spring 6.x (que já usa jakarta.*); conflito potencial com Weld/CDI."
                        .formatted(target.eapVersion()));
            }
            if (detected.contains("Struts")) {
                hints.add("[EAP %s] Struts 1.x não é compatível com Jakarta EE 10 (usa javax.servlet.*); migrar para Struts 2.5+ ou substituir o framework."
                        .formatted(target.eapVersion()));
            }
        } else if (target.isEap7()) {
            hints.add("[EAP %s] Namespace: javax.* ainda é usado; jakarta.* NÃO é suportado nesta versão."
                    .formatted(target.eapVersion()));

            if (detected.contains("EJB")) {
                hints.add("[EAP %s] EJB: entity beans (EJB 2.x) foram removidos; verificar uso de javax.ejb.EJBObject/EJBHome."
                        .formatted(target.eapVersion()));
            }
            if (detected.contains("Hibernate")) {
                hints.add("[EAP %s] Hibernate: versão 5.x incluída; APIs do Hibernate 4.x podem ter depreciações."
                        .formatted(target.eapVersion()));
            }
            if (detected.contains("JSF")) {
                hints.add("[EAP %s] JSF 2.3 incluído; verificar compatibilidade de componentes de terceiros (PrimeFaces, RichFaces)."
                        .formatted(target.eapVersion()));
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
            hints.add("[Java %d] strong encapsulation: uso de reflection em APIs internas (sun.*, com.sun.*) falha sem --add-opens; revisar bytecode ASM/CGLIB."
                    .formatted(java));

            if (detected.contains("Spring")) {
                hints.add("[Java %d] Spring: versão mínima recomendada é 5.3.x (Java 17) ou 6.x (Java 17+); Spring 4.x não é compatível."
                        .formatted(java));
            }
            if (detected.contains("Hibernate")) {
                hints.add("[Java %d] Hibernate: versão 5.6+ para Java 17; Hibernate 6.x para Java 17/21."
                        .formatted(java));
            }
        }

        if (java >= 21) {
            hints.add("[Java %d] Virtual threads disponíveis; considerar substituir pools de threads fixos por Executors.newVirtualThreadPerTaskExecutor()."
                    .formatted(java));
            hints.add("[Java %d] Sequenced Collections: List/Set/Map agora implementam SequencedCollection/SequencedSet/SequencedMap."
                    .formatted(java));

            if (detected.contains("Spring")) {
                hints.add("[Java %d] Spring: versão 6.1+ recomendada para suporte a virtual threads e Project Loom."
                        .formatted(java));
            }
            if (detected.contains("Hibernate")) {
                hints.add("[Java %d] Hibernate 6.4+ suporta virtual threads e Records como embeddables."
                        .formatted(java));
            }
        }

        if (java >= 11 && java < 17) {
            hints.add("[Java %d] Módulos do Java 9+ podem exigir --add-modules ou --add-opens para APIs removidas (JAXB, JAX-WS, Corba)."
                    .formatted(java));
            if (detected.contains("JAX-WS/SOAP")) {
                hints.add("[Java %d] JAX-WS foi removido do JDK; adicionar dependência explícita (jakarta.xml.ws-api ou com.sun.xml.ws:jaxws-ri)."
                        .formatted(java));
            }
        }
    }
}

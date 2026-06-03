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

public final class MtaInstallation {

    /** Human-readable label shown in the output. */
    public String label;

    /** Installation type. Drives how the command is built and how discovery is performed. */
    public MtaInstallationType type = MtaInstallationType.BARE_METAL;

    // ── BARE_METAL ────────────────────────────────────────────────────────────
    /** Absolute path to the mta-cli binary. Required for BARE_METAL. */
    public String path;

    // ── CONTAINER ─────────────────────────────────────────────────────────────
    /** Container image (e.g. quay.io/konveyor/mta-cli:7.2). Required for CONTAINER. */
    public String image;

    /** Container engine to use. Default: docker. Accepts: docker, podman. */
    public String containerEngine = "docker";

    // ── OPENSHIFT ─────────────────────────────────────────────────────────────
    /** OpenShift namespace where the MTA operator is installed. Default: mta. */
    public String namespace = "mta";

    /**
     * Base URL of the MTA Hub route exposed by the operator
     * (e.g. https://mta-mta.apps.cluster.example.com).
     */
    public String hubRoute;

    /**
     * OLM subscription channel for the MTA operator.
     * MTA 7.x → stable-v7   MTA 6.x → stable-v6   (default: stable-v7)
     */
    public String operatorChannel = "stable-v7";

    /**
     * OLM CatalogSource that provides the MTA operator.
     * Red Hat official: redhat-operators  (default)
     */
    public String operatorCatalog = "redhat-operators";
}

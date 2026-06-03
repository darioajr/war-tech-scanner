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

public record MigrationTarget(String eapVersion, int javaVersion) {

    public boolean isEap8OrLater() {
        if (eapVersion == null) return false;
        try {
            return Double.parseDouble(eapVersion) >= 8.0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public boolean isEap7() {
        if (eapVersion == null) return false;
        try {
            double v = Double.parseDouble(eapVersion);
            return v >= 7.0 && v < 8.0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public boolean hasEapVersion()  { return eapVersion != null; }
    public boolean hasJavaVersion() { return javaVersion > 0; }
}

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

import static org.junit.jupiter.api.Assertions.*;

class VersionTest {

    @Test
    void ordersNumericComponents() {
        assertTrue(Version.parse("2.14.1").isBefore(Version.parse("2.17.1")));
        assertTrue(Version.parse("2.9.9").isBefore(Version.parse("2.9.10")));
        assertFalse(Version.parse("2.17.1").isBefore(Version.parse("2.17.1")));
        assertFalse(Version.parse("3.0").isBefore(Version.parse("2.17.1")));
    }

    @Test
    void treatsMissingComponentsAsZero() {
        assertEquals(0, Version.parse("2.5").compareTo(Version.parse("2.5.0")));
        assertEquals(Version.parse("2.5"), Version.parse("2.5.0.0"));
        assertEquals(Version.parse("2.5").hashCode(), Version.parse("2.5.0").hashCode());
    }

    @Test
    void ignoresTrailingQualifiers() {
        assertEquals(0, Version.parse("1.4.18-RELEASE").compareTo(Version.parse("1.4.18")));
        assertEquals(0, Version.parse("2.5.30.GA").compareTo(Version.parse("2.5.30")));
        assertTrue(Version.parse("1.4.17.Final").isBefore(Version.parse("1.4.18")));
    }

    @Test
    void exposesMajor() {
        assertEquals(1, Version.parse("1.2.17").major());
        assertEquals(2, Version.parse("2.0").major());
        assertEquals(0, Version.parse("nonsense").major());
    }

    @Test
    void equalsAndHashCodeContract() {
        var v = Version.parse("2.5.0");
        assertEquals(v, v);
        assertNotEquals(v, Version.parse("2.6"));
        assertNotEquals(v, "2.5.0");          // different type
        assertNotEquals(null, v);
        // trailing zeros normalized in hashCode
        assertEquals(Version.parse("3").hashCode(), Version.parse("3.0.0").hashCode());
    }

    @Test
    void toStringReturnsRaw() {
        assertEquals("1.4.18-RELEASE", Version.parse("1.4.18-RELEASE").toString());
    }

    @Test
    void leadingSeparatorEndsParsing() {
        // '-' with no pending digits stops the numeric prefix
        assertEquals(0, Version.parse("-1.2").major());
        // a stray '.' with no pending digits is skipped, parsing continues
        assertEquals(5, Version.parse(".5").major());
        assertEquals(1, Version.parse("1..2").parts().length == 0 ? 0 : Version.parse("1..2").major());
    }

    @Test
    void comparesWhenOtherIsShorter() {
        assertTrue(Version.parse("2.5.1").compareTo(Version.parse("2.5")) > 0);
        assertTrue(Version.parse("2.5").compareTo(Version.parse("2.5.1")) < 0);
    }

    @Test
    void underscoreSeparatorAndAllZeroHash() {
        assertEquals(0, Version.parse("1_2_3").compareTo(Version.parse("1.2.3")));
        // all-zero version: hashCode trailing-zero loop consumes every component
        assertEquals(Version.parse("0").hashCode(), Version.parse("0.0").hashCode());
    }
}

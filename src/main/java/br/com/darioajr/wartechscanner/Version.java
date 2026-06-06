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

import java.util.Arrays;

/**
 * Lenient dotted-numeric version, tolerant of qualifiers (e.g. {@code 2.9.10.7},
 * {@code 1.4.18-RELEASE}, {@code 2.5.30.GA}). Only the leading numeric components
 * are compared; trailing qualifiers are ignored. Missing components compare as 0,
 * so {@code 2.5} equals {@code 2.5.0}.
 */
public record Version(int[] parts, String raw) implements Comparable<Version> {

    /** Parses the leading numeric segments of a version string. */
    public static Version parse(String s) {
        var nums = new java.util.ArrayList<Integer>();
        var token = new StringBuilder();
        boolean stop = false;
        for (int i = 0; i < s.length() && !stop; i++) {
            char c = s.charAt(i);
            boolean isSep = c == '.' || c == '-' || c == '_';
            if (Character.isDigit(c)) {
                token.append(c);
            } else if (isSep && !token.isEmpty()) {
                nums.add(Integer.parseInt(token.toString()));
                token.setLength(0);
            } else {
                // '.' with no pending digits is harmless; any other non-digit
                // ('-', '_', or a qualifier letter) ends the numeric prefix.
                stop = c != '.';
            }
        }
        if (!token.isEmpty()) {
            nums.add(Integer.parseInt(token.toString()));
        }
        return new Version(nums.stream().mapToInt(Integer::intValue).toArray(), s);
    }

    public int major() {
        return parts.length > 0 ? parts[0] : 0;
    }

    public boolean isBefore(Version other) {
        return compareTo(other) < 0;
    }

    @Override
    public int compareTo(Version o) {
        int len = Math.max(parts.length, o.parts.length);
        for (int i = 0; i < len; i++) {
            int a = i < parts.length ? parts[i] : 0;
            int b = i < o.parts.length ? o.parts[i] : 0;
            if (a != b) {
                return Integer.compare(a, b);
            }
        }
        return 0;
    }

    // record auto-generated equals/hashCode would compare the raw String and the
    // array identity; override so value-equal versions (2.5 vs 2.5.0) match.
    @Override
    public boolean equals(Object o) {
        return o instanceof Version v && compareTo(v) == 0;
    }

    @Override
    public int hashCode() {
        // normalize trailing zeros so equal versions hash equally
        int last = parts.length;
        while (last > 0 && parts[last - 1] == 0) {
            last--;
        }
        return Arrays.hashCode(Arrays.copyOf(parts, last));
    }

    @Override
    public String toString() {
        return raw;
    }
}

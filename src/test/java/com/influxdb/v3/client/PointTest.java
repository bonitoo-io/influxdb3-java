/*
 * The MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.influxdb.v3.client;

import java.math.BigInteger;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import com.influxdb.v3.client.write.WritePrecision;

public class PointTest {
    @Test
    void setMeasurement() {
        Point point = Point.measurement("measurement");
        Assertions.assertThat("measurement").isEqualTo(point.getMeasurement());

        point.setMeasurement("newMeasurement");
        Assertions.assertThat("newMeasurement").isEqualTo(point.getMeasurement());
    }

    @Test
    void setTimestamp() {
        Point point = Point.measurement("measurement");

        Instant timestamp = Instant.parse("2023-11-08T12:00:00Z");
        point.setTimestamp(timestamp);
        Assertions.assertThat(BigInteger.valueOf(timestamp.getEpochSecond())
                .multiply(BigInteger.valueOf(1_000_000_000)))
            .isEqualTo(point.getTimestamp());
    }

    @Test
    void setTags() {
        Point point = Point.measurement("measurement");

        Map<String, String> tags = new HashMap<>();
        tags.put("tag1", "value1");
        tags.put("tag2", "value2");

        point.setTags(tags);

        Assertions.assertThat("value1").isEqualTo(point.getTag("tag1"));
        Assertions.assertThat("value2").isEqualTo(point.getTag("tag2"));
    }

    @Test
    void setFields() {
        Point point = Point.measurement("measurement");

        point.setField("field1", 42);
        point.setField("field2", "value");
        point.setField("field3", 3.14);

        Assertions.assertThat(42L).isEqualTo(point.getField("field1"));
        Assertions.assertThat("value").isEqualTo(point.getField("field2"));
        Assertions.assertThat(3.14).isEqualTo(point.getField("field3"));
    }

    @Test
    void toLineProtocol() {
        Point point = Point.measurement("measurement")
                .setTag("tag1", "value1")
                .setField("field1", 42);

        String lineProtocol = point.toLineProtocol(WritePrecision.NS);
        Assertions.assertThat("measurement,tag1=value1 field1=42i").isEqualTo(lineProtocol);
    }

    @Test
    void copy() {
        Point point = Point.measurement("measurement")
                .setTag("tag1", "value1")
                .setField("field1", 42);

        Point copy = point.copy();

        // Ensure the copy is not the same object
        Assertions.assertThat(point).isNotSameAs(copy);
        // Ensure the values are equal
        Assertions.assertThat(point.getMeasurement()).isEqualTo(copy.getMeasurement());
        Assertions.assertThat(point.getTag("tag1")).isEqualTo(copy.getTag("tag1"));
        Assertions.assertThat(point.getField("field1")).isEqualTo(copy.getField("field1"));
    }
}

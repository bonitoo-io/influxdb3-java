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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class InfluxDBClientTest {

    @Test
    void requiredHostUrl() {

        //noinspection DataFlowIssue
        Assertions.assertThatThrownBy(() -> InfluxDBClient.getInstance(null, "my-token", "my-database"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The hostname or IP address of the InfluxDB server has to be defined.");
    }

    @Test
    public void autoCloseable() throws Exception {

        try (InfluxDBClient client = InfluxDBClient.getInstance("http://localhost:8086", "my-token", "my-database")) {

            Assertions.assertThat(client).isNotNull();
        }
    }
}

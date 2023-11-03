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
package com.influxdb.v3.client.write;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.HashMap;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import com.influxdb.v3.client.Point;

/**
 * @author Jakub Bednar (bednar@github) (11/10/2018 12:57)
 */
class PointTest {

    @Test
    void measurementEscape() {

        Point point = Point.measurement("h2 o")
                .setTag("location", "europe")
                .setTag("", "warn")
                .setField("level", 2);

        Assertions.assertThat(point.toLineProtocol()).isEqualTo("h2\\ o,location=europe level=2i");

        point = Point.measurement("h2=o")
                .setTag("location", "europe")
                .setTag("", "warn")
                .setField("level", 2);

        Assertions.assertThat(point.toLineProtocol()).isEqualTo("h2=o,location=europe level=2i");

        point = Point.measurement("h2,o")
                .setTag("location", "europe")
                .setTag("", "warn")
                .setField("level", 2);

        Assertions.assertThat(point.toLineProtocol()).isEqualTo("h2\\,o,location=europe level=2i");
    }

    @Test
    public void createByConstructor() {
        Point point = new Point("h2o")
                .setTag("location", "europe")
                .setField("level", 2);

        Assertions.assertThat(point.toLineProtocol()).isEqualTo("h2o,location=europe level=2i");
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void createByConstructorMeasurementRequired() {
        Assertions.assertThatThrownBy(() -> new Point(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Expecting a not null reference for measurement");
    }

    @Test
    void tagEmptyKey() {

        Point point = Point.measurement("h2o")
                .setTag("location", "europe")
                .setTag("", "warn")
                .setField("level", 2);

        Assertions.assertThat(point.toLineProtocol()).isEqualTo("h2o,location=europe level=2i");
    }

    @Test
    void tagEmptyValue() {

        Point point = Point.measurement("h2o")
                .setTag("location", "europe")
                .setTag("log", "")
                .setField("level", 2);

        Assertions.assertThat(point.toLineProtocol()).isEqualTo("h2o,location=europe level=2i");
    }

    @Test
    void tagNullValue() {

        Point point = Point.measurement("h2o")
                .setTag("location", "europe")
                .setTag("log", null)
                .setField("level", 2);

        Assertions.assertThat(point.toLineProtocol()).isEqualTo("h2o,location=europe level=2i");
    }

    @Test
    public void tagEscapingKeyAndValue() {

        Point point = Point.measurement("h\n2\ro\t_data")
                .setTag("new\nline", "new\nline")
                .setTag("carriage\rreturn", "carriage\rreturn")
                .setTag("t\tab", "t\tab")
                .setField("level", 2);

        Assertions.assertThat(point.toLineProtocol())
                .isEqualTo("h\\n2\\ro\\t_data,carriage\\rreturn=carriage\\rreturn,new\\nline=new\\nline,"
                        + "t\\tab=t\\tab level=2i");
    }

    @Test
    public void equalSignEscaping() {

        Point point = Point.measurement("h=2o")
                .setTag("l=ocation", "e=urope")
                .setField("l=evel", 2);

        Assertions.assertThat(point.toLineProtocol())
                .isEqualTo("h=2o,l\\=ocation=e\\=urope l\\=evel=2i");
    }

    @Test
    void fieldTypes() {

        Point point = Point.measurement("h2o").setTag("location", "europe")
                .setField("long", 1L)
                .setField("double", 2D)
                .setField("float", 3F)
                .setField("longObject", Long.valueOf("4"))
                .setField("doubleObject", Double.valueOf("5"))
                .setField("floatObject", Float.valueOf("6"))
                .setField("bigDecimal", new BigDecimal("33.45"))
                .setField("integer", 7)
                .setField("integerObject", Integer.valueOf("8"))
                .setField("boolean", false)
                .setField("booleanObject", Boolean.TRUE)
                .setField("string", "string value");

        String expected = "h2o,location=europe bigDecimal=33.45,boolean=false,booleanObject=true,double=2.0,"
                + "doubleObject=5.0,float=3.0,floatObject=6.0,integer=7i,integerObject=8i,long=1i,longObject=4i,"
                + "string=\"string value\"";
        Assertions.assertThat(point.toLineProtocol()).isEqualTo(expected);
    }

    @Test
    void fieldNullValue() {

        Point point = Point.measurement("h2o").setTag("location", "europe").setField("level", 2)
                .setField("warning", (String) null);

        Assertions.assertThat(point.toLineProtocol()).isEqualTo("h2o,location=europe level=2i");
    }

    @Test
    void fieldEscape() {

        Point point = Point.measurement("h2o")
                .setTag("location", "europe")
                .setField("level", "string esc\\ape value");

        Assertions.assertThat(point.toLineProtocol()).isEqualTo("h2o,location=europe "
                + "level=\"string esc\\\\ape value\"");

        point = Point.measurement("h2o")
                .setTag("location", "europe")
                .setField("level", "string esc\"ape value");

        Assertions.assertThat(point.toLineProtocol()).isEqualTo("h2o,location=europe "
                + "level=\"string esc\\\"ape value\"");
    }

    @Test
    void time() {

        Point point = Point.measurement("h2o")
                .setTag("location", "europe")
                .setField("level", 2)
                .setTimestamp(123L, WritePrecision.S);

        Assertions.assertThat(point.toLineProtocol(WritePrecision.S)).isEqualTo("h2o,location=europe level=2i 123");
    }

    @Test
    void timeBigInteger() {

        Point point = Point.measurement("h2o")
                .setTag("location", "europe")
                .setField("level", 2)
                .setTimestamp(new BigInteger("123"), WritePrecision.S);

        Assertions.assertThat(point.toLineProtocol(WritePrecision.S)).isEqualTo("h2o,location=europe level=2i 123");

        // Friday, June 22, 3353
        point = Point.measurement("h2o")
                .setTag("location", "europe")
                .setField("level", 2)
                .setTimestamp(new BigInteger("43658216763800123456"), WritePrecision.NS);

        Assertions.assertThat(point.toLineProtocol()).isEqualTo("h2o,location=europe level=2i 43658216763800123456");
    }

    @Test
    void timeBigDecimal() {

        Point point = Point.measurement("h2o")
                .setTag("location", "europe")
                .setField("level", 2)
                .setTimestamp(new BigDecimal("123"), WritePrecision.S);

        Assertions.assertThat(point.toLineProtocol(WritePrecision.S)).isEqualTo("h2o,location=europe level=2i 123");

        point = Point.measurement("h2o")
                .setTag("location", "europe")
                .setField("level", 2)
                .setTimestamp(new BigDecimal("1.23E+02"), WritePrecision.NS);

        Assertions.assertThat(point.toLineProtocol()).isEqualTo("h2o,location=europe level=2i 123");

        // Friday, June 22, 3353
        point = Point.measurement("h2o")
                .setTag("location", "europe")
                .setField("level", 2)
                .setTimestamp(new BigDecimal("43658216763800123456"), WritePrecision.NS);

        Assertions.assertThat(point.toLineProtocol()).isEqualTo("h2o,location=europe level=2i 43658216763800123456");
    }

    @Test
    void timeFloat() {

        Point point = Point.measurement("h2o")
                .setTag("location", "europe")
                .setField("level", 2)
                .setTimestamp(Float.valueOf("123"), WritePrecision.S);

        Assertions.assertThat(point.toLineProtocol(WritePrecision.S)).isEqualTo("h2o,location=europe level=2i 123");

        point = Point.measurement("h2o")
                .setTag("location", "europe")
                .setField("level", 2)
                .setTimestamp(Float.valueOf("1.23"), WritePrecision.NS);

        Assertions.assertThat(point.toLineProtocol()).isEqualTo("h2o,location=europe level=2i 1");
    }

    @Test
    void timeInstantOver2262() {

        Instant time = Instant.parse("3353-06-22T10:26:03.800123456Z");

        Point point = Point.measurement("h2o")
                .setTag("location", "europe")
                .setField("level", 2)
                .setTimestamp(time);

        Assertions.assertThat(point.toLineProtocol()).isEqualTo("h2o,location=europe level=2i 43658216763800123456");
    }

    @Test
    void timeInstantNull() {

        Point point = Point.measurement("h2o")
                .setTag("location", "europe")
                .setField("level", 2)
                .setTimestamp(null);

        Assertions.assertThat(point.toLineProtocol()).isEqualTo("h2o,location=europe level=2i");
    }

    @Test
    void timeGetTime() {

        Instant time = Instant.parse("2022-06-12T10:26:03.800123456Z");

        Point point = Point.measurement("h2o")
                .setTag("location", "europe")
                .setField("level", 2)
                .setTimestamp(time);

        Assertions.assertThat(point.toLineProtocol()).isEqualTo("h2o,location=europe level=2i 1655029563800123456");
    }

    @Test
    public void infinityValues() {
        Point point = Point.measurement("h2o")
                .setTag("location", "europe")
                .setField("double-infinity-positive", Double.POSITIVE_INFINITY)
                .setField("double-infinity-negative", Double.NEGATIVE_INFINITY)
                .setField("double-nan", Double.NaN)
                .setField("flout-infinity-positive", Float.POSITIVE_INFINITY)
                .setField("flout-infinity-negative", Float.NEGATIVE_INFINITY)
                .setField("flout-nan", Float.NaN)
                .setField("level", 2);

        Assertions.assertThat(point.toLineProtocol()).isEqualTo("h2o,location=europe level=2i");
    }

    @Test
    public void onlyInfinityValues() {
        Point point = Point.measurement("h2o")
                .setTag("location", "europe")
                .setField("double-infinity-positive", Double.POSITIVE_INFINITY)
                .setField("double-infinity-negative", Double.NEGATIVE_INFINITY)
                .setField("double-nan", Double.NaN)
                .setField("flout-infinity-positive", Float.POSITIVE_INFINITY)
                .setField("flout-infinity-negative", Float.NEGATIVE_INFINITY)
                .setField("flout-nan", Float.NaN);

        Assertions.assertThat(point.toLineProtocol()).isEqualTo("");
    }

    @Test
    void hasFields() {

        Assertions.assertThat(Point.measurement("h2o").hasFields()).isFalse();
        Assertions.assertThat(Point.measurement("h2o").setTag("location", "europe").hasFields()).isFalse();
        Assertions.assertThat(Point.measurement("h2o").setField("level", 2).hasFields()).isTrue();
        Assertions.assertThat(
                        Point
                                .measurement("h2o")
                                .setTag("location", "europe")
                                .setField("level", 3)
                                .hasFields())
                .isTrue();
    }

    @Test
    void setTags() {

        HashMap<String, String> tags = new HashMap<>();
        tags.put("type", "production");
        tags.put("location", "europe");
        tags.put("expensive", "");

        Point point = Point.measurement("h2o")
                .setField("level", 2)
                .setTags(tags);

        Assertions.assertThat(point.toLineProtocol()).isEqualTo("h2o,location=europe,type=production level=2i");
    }

    @Test
    void setFields() {

        HashMap<String, Object> fields = new HashMap<>();
        fields.put("level", 2);
        fields.put("accepted", true);
        fields.put("power", 2.56);
        fields.put("clean", null);

        Point point = Point
                .measurement("h2o")
                .setTag("location", "europe")
                .setFields(fields);

        Assertions
                .assertThat(point.toLineProtocol())
                .isEqualTo("h2o,location=europe accepted=true,level=2i,power=2.56");
    }
}

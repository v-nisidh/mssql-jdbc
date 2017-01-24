// ---------------------------------------------------------------------------------------------------------------------------------
// File: SqlDateTime2.java
//
//
// Microsoft JDBC Driver for SQL Server
// Copyright(c) Microsoft Corporation
// All rights reserved.
// MIT License
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files(the "Software"),
// to deal in the Software without restriction, including without limitation the
// rights to use, copy, modify, merge, publish, distribute, sublicense,
// and / or sell copies of the Software, and to permit persons to whom the
// Software is furnished to do so, subject to the following conditions :
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
// THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
// IN THE SOFTWARE.
// ---------------------------------------------------------------------------------------------------------------------------------

package com.microsoft.sqlserver.testframework.sqlType;

import static org.junit.jupiter.api.Assertions.fail;

import java.sql.JDBCType;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang3.RandomStringUtils;

public class SqlDateTime2 extends SqlDateTime {

    static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HH:mm:ss.SSSSSSS");
    static String basePattern = "yyyy-MM-dd HH:mm:ss";
    static DateTimeFormatter formatter;

    public SqlDateTime2() {
        super("datetime2", JDBCType.TIMESTAMP, null, null);
        try {
            minvalue = new Timestamp(
                    dateFormat.parse((String) SqlTypeValue.DATETIME2.minValue).getTime());
            maxvalue = new Timestamp(
                    dateFormat.parse((String) SqlTypeValue.DATETIME2.maxValue).getTime());
        }
        catch (ParseException ex) {
            fail(ex.getMessage());
        }
        this.precision = 7;
        this.variableLengthType = VariableLengthType.Precision;
        generatePrecision();
        formatter = new DateTimeFormatterBuilder().appendPattern(basePattern)
                .appendFraction(ChronoField.NANO_OF_SECOND, 0, this.precision, true).toFormatter();

    }

    public Object createdata() {
        Timestamp temp = new Timestamp(ThreadLocalRandom.current()
                .nextLong(((Timestamp) minvalue).getTime(), ((Timestamp) maxvalue).getTime()));
        temp.setNanos(0);
        String timeNano = temp.toString().substring(0, temp.toString().length() - 1)
                + RandomStringUtils.randomNumeric(this.precision);
        // can pass string rather than converting to LocalDateTime, but leaving
        // it unchanged for now to handle prepared statements
        return LocalDateTime.parse(timeNano, formatter);
    }
}
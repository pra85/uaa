/*
 * *****************************************************************************
 *      Cloud Foundry
 *      Copyright (c) [2009-2015] Pivotal Software, Inc. All Rights Reserved.
 *      This product is licensed to you under the Apache License, Version 2.0 (the "License").
 *      You may not use this product except in compliance with the License.
 *
 *      This product includes a number of subcomponents with
 *      separate copyright notices and license terms. Your use of these
 *      subcomponents is subject to the terms and conditions of the
 *      subcomponent's license, as noted in the LICENSE file.
 * *****************************************************************************
 */

package org.cloudfoundry.identity.client.expr;


import org.junit.Test;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Josh Ghiloni
 *
 */
public class FilterRequestBuilderTest {

    @Test
    public void testComparisonOperators() throws Exception {
        FilterRequestBuilder builder = new FilterRequestBuilder();
        builder.equals("test", true);

        FilterRequest request = builder.build();
        assertEquals("test eq true", request.getFilter());

        builder = new FilterRequestBuilder();
        builder.greaterThan("test", 4.2);

        request = builder.build();
        assertEquals("test gt 4.2", request.getFilter());

        builder = new FilterRequestBuilder();
        builder.greaterThanOrEquals("test", -10);

        request = builder.build();
        assertEquals("test ge -10", request.getFilter());

        builder = new FilterRequestBuilder();
        builder.lessThan("test", 0);

        request = builder.build();
        assertEquals("test lt 0", request.getFilter());

        Calendar testDate = Calendar.getInstance();
        testDate.setTimeInMillis(0);
        testDate.setTimeZone(TimeZone.getTimeZone("UTC"));

        builder = new FilterRequestBuilder();
        builder.lessThanOrEquals("test", testDate);

        request = builder.build();
        assertEquals("test le \"1970-01-01T00:00:00Z\"", request.getFilter());

        builder = new FilterRequestBuilder();
        builder.startsWith("test", "test");

        request = builder.build();
        assertEquals("test sw \"test\"", request.getFilter());

        builder = new FilterRequestBuilder();
        builder.contains("test", "val");

        request = builder.build();
        assertEquals("test co \"val\"", request.getFilter());
    }

    @Test
    public void testUnaryOperators() throws Exception {
        FilterRequestBuilder builder = new FilterRequestBuilder();
        builder.present("test");

        FilterRequest request = builder.build();
        assertEquals("test pr", request.getFilter());

        builder = new FilterRequestBuilder();
        builder.equals("test", false).precedence();

        request = builder.build();
        assertEquals("(test eq false)", request.getFilter());
    }

    @Test
    public void testJoinOperators() throws Exception {
        FilterRequestBuilder builder = new FilterRequestBuilder();
        builder.equals("foo", "bar").present("test").and();

        FilterRequest request = builder.build();
        assertEquals("foo eq \"bar\" and test pr", request.getFilter());

        builder = new FilterRequestBuilder();
        builder.equals("foo", "bar").present("test").or();

        request = builder.build();
        assertEquals("foo eq \"bar\" or test pr", request.getFilter());

        builder = new FilterRequestBuilder();
        builder.equals("foo", "bar").present("test").contains("test", "val").and().or();

        request = builder.build();
        assertEquals("foo eq \"bar\" or test pr and test co \"val\"", request.getFilter());

        builder = new FilterRequestBuilder();
        builder.equals("foo", true).present("test").startsWith("test", "val").and().precedence().or();

        request = builder.build();
        assertEquals("foo eq true or (test pr and test sw \"val\")", request.getFilter());
    }

    @Test
    public void testDefaultJoin() throws Exception {
        FilterRequestBuilder builder = new FilterRequestBuilder();
        builder.equals("foo", "bar").present("test").greaterThan("test", 4.3);

        FilterRequest request = builder.build();
        assertEquals("foo eq \"bar\" and test pr and test gt 4.3", request.getFilter());

        builder = new FilterRequestBuilder(false);
        builder.equals("foo", "bar").present("test").greaterThan("test", 4.3);

        request = builder.build();
        assertEquals("foo eq \"bar\" or test pr or test gt 4.3", request.getFilter());
    }

    @Test
    public void testAttributes() throws Exception {
        FilterRequestBuilder builder = new FilterRequestBuilder();
        builder.attributes("id", "name", "address");

        FilterRequest request = builder.build();

        List<String> expectedAttributes = Arrays.asList("id", "name", "address");

        assertEquals(expectedAttributes, request.getAttributes());
    }

    @Test
    public void testCount() throws Exception {
        FilterRequestBuilder builder = new FilterRequestBuilder();
        FilterRequest request = builder.build();

        assertEquals(0, request.getCount());

        builder = new FilterRequestBuilder().count(10);
        request = builder.build();

        assertEquals(10, request.getCount());
    }

    @Test
    public void testStart() throws Exception {
        FilterRequestBuilder builder = new FilterRequestBuilder();
        FilterRequest request = builder.build();

        assertEquals(0, request.getStart());

        builder = new FilterRequestBuilder().start(10);
        request = builder.build();

        assertEquals(10, request.getStart());
    }

    @Test
    public void testSubsequentPrecedence() throws Exception {
        FilterRequestBuilder builder = new FilterRequestBuilder();
        builder.present("test").precedence();

        FilterRequest request = builder.build();
        assertEquals("(test pr)", request.getFilter());

        builder = new FilterRequestBuilder();
        builder.present("test").precedence().precedence().precedence();

        request = builder.build();
        assertEquals("(test pr)", request.getFilter());
    }

    @Test
    public void testBuildException() throws Exception {
        FilterRequestBuilder builder = new FilterRequestBuilder();
        builder.present("test").precedence().build();

        try {
            builder.present("should_fail");
            fail("Should have failed because the builder was built");
        } catch (IllegalStateException e) {
            System.out.println("Should see this exception");
            e.printStackTrace(System.out);
        }
    }
}

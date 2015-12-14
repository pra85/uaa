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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author Josh Ghiloni
 *
 */
abstract class BinaryOperation<L, R> extends UnaryOperation<L> {
    protected R right;

    protected String operator;

    private static final String ISO_STRING = "yyyy-MM-dd'T'HH:mm:ssXX";
    private static final SimpleDateFormat ISO = new SimpleDateFormat(ISO_STRING);

    /**
     * @param left
     * @param right
     */
    BinaryOperation(L left, R right) {
        super(left);
        this.right = right;
    }

    R getRight() {
        return right;
    }


    @Override
    public String toString() {
        StringBuilder expr = new StringBuilder();
        expr.append(left).append(' ').append(operator).append(' ');

        if (right == null) {
            expr.append("null");
        }
        else if (right instanceof String) {
            expr.append('"').append(right).append('"');
        }
        else if (right instanceof Date) {
            expr.append('"').append(ISO.format((Date) right)).append('"');
        }
        else if (right instanceof Calendar) {
            // preserve timezone
            SimpleDateFormat sdf = new SimpleDateFormat(ISO_STRING);
            sdf.setTimeZone(((Calendar)right).getTimeZone());

            expr.append('"').append(sdf.format(((Calendar) right).getTime())).append('"');
        }
        else if ((right instanceof Number) || (right instanceof Boolean) || (right instanceof Operation)) {
            expr.append(right);
        }
        else {
            throw new IllegalArgumentException(String.format("Invalid type %s for RHS", right.getClass().getName()));
        }

        return expr.toString();
    }
}

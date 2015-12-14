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


/**
 * @author Josh Ghiloni
 *
 */
abstract class UnaryOperation<L> implements Operation {
    protected L left;

    protected String operator;

    /**
     * @param left
     */
    UnaryOperation(L left) {
        this.left = left;
    }

    L getLeft() {
        return left;
    }

    String getOperator() {
        return operator;
    }

    @Override
    public String toString() {
        StringBuilder expr = new StringBuilder();
        expr.append(left).append(' ').append(operator);

        return expr.toString();
    }
}

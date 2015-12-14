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

import java.util.List;

/**
 * A class used to filter results from SCIM paged APIs. Must be constructed using {@link FilterRequestBuilder}
 *
 * @author Josh Ghiloni
 *
 */
public class FilterRequest {
    private String filter;

    private List<String> attributes;

    private int start;

    private int count;

    public FilterRequest() {

    }

    FilterRequest(String filter, List<String> attributes, int start, int count) {
        this.filter = filter;
        this.attributes = attributes;
        this.start = start;
        this.count = count;
    }

    /**
     * The SCIM filter string
     * @return
     */
    public String getFilter() {
        return filter;
    }

    /**
     * @return The 1-based starting index for the query. If &lt; 0, indicates the start of the list
     */
    public int getStart() {
        return start;
    }

    /**
     * @return The number of items to be returned by the query. If &lt; 1, no count will be passed to the API, and the
     * default UAA behavior will take place.
     */
    public int getCount() {
        return count;
    }

    /**
     * @return The attributes to be returned by the query. If the list is empty, all attributes are returned.
     */
    public List<String> getAttributes() {
        return attributes;
    }

    static final FilterRequest SHOW_ALL = new FilterRequest(null, null, 0, 0);
}

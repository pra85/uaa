/*
 * *****************************************************************************
 *      Cloud Foundry
 *      Copyright (c) [2009-2015] Pivotal Software, Inc. All Rights Reserved.
 *
 *      This product is licensed to you under the Apache License, Version 2.0 (the "License").
 *      You may not use this product except in compliance with the License.
 *
 *      This product includes a number of subcomponents with
 *      separate copyright notices and license terms. Your use of these
 *      subcomponents is subject to the terms and conditions of the
 *      subcomponent's license, as noted in the LICENSE file.
 * *****************************************************************************
 */

package org.cloudfoundry.identity.uaa.audit.event;

import org.springframework.context.ApplicationEvent;

public class EntityDeletedEvent<T> extends ApplicationEvent {

    private final  T deleted;

    public EntityDeletedEvent(T deleted) {
        super(deleted);
        this.deleted = deleted;
    }

    public T getDeleted() {
        return deleted;
    }
}

package org.cloudfoundry.identity.uaa.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudfoundry.identity.uaa.provider.IdpAlreadyExistsException;
import org.cloudfoundry.identity.uaa.zone.IdentityZoneHolder;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.util.Assert;

import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/*******************************************************************************
 * Cloud Foundry
 * Copyright (c) [2009-2015] Pivotal Software, Inc. All Rights Reserved.
 * <p>
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 * <p>
 * This product includes a number of subcomponents with
 * separate copyright notices and license terms. Your use of these
 * subcomponents is subject to the terms and conditions of the
 * subcomponent's license, as noted in the LICENSE file.
 *******************************************************************************/
public class JdbcClientUIDetailsProvisioning implements ClientUIDetailsProvisioning {

    private static final Log logger = LogFactory.getLog(JdbcClientUIDetailsProvisioning.class);

    private static final String CLIENT_UI_DETAILS_FIELDS = "id, client_id, identity_zone_id, show_on_home_page, app_launch_url, app_icon, version";
    private static final String CLIENT_UI_DETAILS_QUERY = "select " + CLIENT_UI_DETAILS_FIELDS + " from oauth_client_ui_details where client_id=? and identity_zone_id=?";
    private static final String CLIENT_UI_DETAILS_CREATE = "insert into oauth_client_ui_details(" + CLIENT_UI_DETAILS_FIELDS + ") values (?,?,?,?,?,?,?)";
    private static final String CLIENT_UI_DETAILS_UPDATE_FIELDS = "show_on_home_page, app_launch_url, app_icon, version";
    private static final String CLIENT_UI_DETAILS_UPDATE = "update oauth_client_ui_details set " + CLIENT_UI_DETAILS_UPDATE_FIELDS.replace(",", "=?,") + "=?" + " where client_id=? and identity_zone_id=? and version=?";
    private static final String CLIENT_UI_DETAILS_DELETE_QUERY = "delete from oauth_client_ui_details where client_id=? and identity_zone_id=?";

    private JdbcTemplate template;
    private final RowMapper<ClientUIDetails> mapper = new ClientUIDetailsRowMapper();

    JdbcClientUIDetailsProvisioning(JdbcTemplate template) {
        Assert.notNull(template);
        this.template = template;
    }

    public void setTemplate(JdbcTemplate template) {
        this.template = template;
    }

    @Override
    public List<ClientUIDetails> retrieveAll() {
        logger.debug("Retrieving UI details for all client");
        return template.query(CLIENT_UI_DETAILS_QUERY, mapper, IdentityZoneHolder.get().getId());
    }

    @Override
    public ClientUIDetails retrieve(String clientId) {
        logger.debug("Retrieving UI details for client: " + clientId);
        return template.queryForObject(CLIENT_UI_DETAILS_QUERY, mapper, clientId, IdentityZoneHolder.get().getId());
    }

    @Override
    public ClientUIDetails create(ClientUIDetails resource) {
        logger.debug("Creating new UI details for client: " + resource.getClientId());
        final String id = UUID.randomUUID().toString();
        try {
            template.update(CLIENT_UI_DETAILS_CREATE, new PreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps) throws SQLException {
                    int pos = 1;
                    ps.setString(pos++, id);
                    ps.setString(pos++, resource.getClientId());
                    ps.setString(pos++, IdentityZoneHolder.get().getId());
                    ps.setBoolean(pos++, resource.isShowOnHomePage());
                    ps.setString(pos++, resource.getAppLaunchUrl().toString());
                    byte[] appIcon = Base64.decode(resource.getAppIcon().getBytes());
                    ps.setBinaryStream(pos++, new ByteArrayInputStream(appIcon), (int) appIcon.length);
                    ps.setInt(pos++, 1);
                }
            });
        } catch (DuplicateKeyException e) {
            throw new IdpAlreadyExistsException(e.getMostSpecificCause().getMessage());
        }
        return retrieve(resource.getClientId());
    }

    @Override
    public ClientUIDetails update(String clientId, ClientUIDetails resource) {
        logger.debug("Updating UI details for client: " + clientId);
        int updated = template.update(CLIENT_UI_DETAILS_UPDATE, new PreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps) throws SQLException {
                int pos = 1;
                ps.setBoolean(pos++, resource.isShowOnHomePage());
                ps.setString(pos++, resource.getAppLaunchUrl().toString());
                byte[] appIcon = Base64.decode(resource.getAppIcon().getBytes());
                ps.setBinaryStream(pos++, new ByteArrayInputStream(appIcon), (int) appIcon.length);
                ps.setInt(pos++, resource.getVersion() + 1);
                ps.setString(pos++, clientId);
                ps.setString(pos++, IdentityZoneHolder.get().getId());
                ps.setInt(pos++, resource.getVersion());
            }
        });

        ClientUIDetails resultingClientUIDetails = retrieve(clientId);

        if (updated == 0) {
            throw new OptimisticLockingFailureException(String.format(
                    "Attempt to update the UI details of client (%s) failed with incorrect version: expected=%d but found=%d",
                    clientId,
                    resultingClientUIDetails.getVersion(),
                    resource.getVersion()));
        } else if (updated > 1) {
            throw new IncorrectResultSizeDataAccessException(1);
        }

        return resultingClientUIDetails;
    }

    @Override
    public ClientUIDetails delete(String clientId, int version) {
        logger.debug("Deleting UI details for client: " + clientId);
        ClientUIDetails clientUIDetails = retrieve(clientId);
        int updated;

        if (version < 0) {
            updated = template.update(CLIENT_UI_DETAILS_DELETE_QUERY, clientId, IdentityZoneHolder.get().getId());
        } else {
            updated = template.update(CLIENT_UI_DETAILS_DELETE_QUERY + " and version=?", clientId, IdentityZoneHolder.get().getId(), version);
        }

        if (updated == 0) {
            throw new OptimisticLockingFailureException(String.format(
                    "Attempt to delete the UI details of client (%s) failed with incorrect version: expected=%d but found=%d",
                    clientId,
                    clientUIDetails.getVersion(),
                    version));
        }

        return clientUIDetails;
    }


    private class ClientUIDetailsRowMapper implements RowMapper<ClientUIDetails> {

        @Override
        public ClientUIDetails mapRow(ResultSet rs, int rowNum) throws SQLException {
            ClientUIDetails clientUIDetails = new ClientUIDetails();
            int pos = 1;
            pos++; // id
            clientUIDetails.setClientId(rs.getString(pos++));
            clientUIDetails.setIdentityZoneId(rs.getString(pos++));
            clientUIDetails.setShowOnHomePage(rs.getBoolean(pos++));
            try {
                clientUIDetails.setAppLaunchUrl(new URL(rs.getString(pos++)));
            } catch (MalformedURLException mue) {
                // it is safe to ignore this as client_ui_details rows are always created from a ClientUIDetails instance whose launch url property is strongly typed to URL
            }
            clientUIDetails.setAppIcon(new String(Base64.encode(rs.getBytes(pos++))));
            clientUIDetails.setVersion(rs.getInt(pos++));
            return clientUIDetails;
        }
    }
}

/*
 * #%L
 * dropwizard-wotif-db
 * %%
 * Copyright (C) 2015 Wotif Group
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.wotifgroup.dropwizard.db;

import com.codahale.metrics.health.HealthCheck;
import io.dropwizard.db.DataSourceFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DataSourceHealthCheck extends HealthCheck {

    private final DataSource dataSource;
    private final String validationQuery;

    /**
     * If {@link io.dropwizard.db.DataSourceFactory#getCheckConnectionOnBorrow()} is enabled, the health check will use
     * {@link javax.sql.DataSource#getConnection()}.
     * Otherwise, the health check will issue {@link io.dropwizard.db.DataSourceFactory#getValidationQuery()}, if present.
     *
     */
    public DataSourceHealthCheck(DataSource dataSource, DataSourceFactory dataSourceFactory) {
        this(dataSource, dataSourceFactory.getCheckConnectionOnBorrow() ? null : dataSourceFactory.getValidationQuery());
    }

    /**
     * The health check will only use {@link javax.sql.DataSource#getConnection()}.
     * This should be fine for most use-cases, as {@link com.wotifgroup.dropwizard.db.WotifDataSourceFactory} enables
     * {@link io.dropwizard.db.DataSourceFactory#getCheckConnectionOnBorrow()} by default.
     */
    public DataSourceHealthCheck(DataSource dataSource) {
        this(dataSource, (String) null);
    }

    /**
     * The health check will issue an explicit validation query, if present.
     */
    public DataSourceHealthCheck(DataSource dataSource, String validationQuery) {
        this.dataSource = dataSource;
        this.validationQuery = validationQuery;
    }

    @Override
    protected Result check() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            if (validationQuery != null) {
                try (Statement statement = connection.createStatement()) {
                    statement.execute(validationQuery);
                }
            }
        }
        return Result.healthy();
    }

}

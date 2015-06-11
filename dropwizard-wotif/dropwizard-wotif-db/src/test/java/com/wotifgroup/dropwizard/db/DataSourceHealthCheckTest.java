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

import io.dropwizard.db.DataSourceFactory;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DataSourceHealthCheckTest {

    private static final String VALIDATION_QUERY = "am i valid?";

    @Test
    public void testHealthyWithNoValidationQuery() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);

        DataSourceHealthCheck healthCheck = new DataSourceHealthCheck(dataSource);
        assertTrue(healthCheck.check().isHealthy());

        verify(dataSource).getConnection();
        verify(connection).close();
    }

    @Test
    public void testUnhealthyWithNoValidationQuery() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        when(dataSource.getConnection()).thenThrow(new SQLException("ohnoes"));

        DataSourceHealthCheck healthCheck = new DataSourceHealthCheck(dataSource);
        try {
            healthCheck.check();
            fail("Expected an exception");
        } catch (SQLException e) {
            assertEquals("ohnoes", e.getMessage());
        }
    }

    @Test
    public void testHealthyWithValidationQuery() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);

        DataSourceHealthCheck healthCheck = new DataSourceHealthCheck(dataSource, VALIDATION_QUERY);
        assertTrue(healthCheck.check().isHealthy());

        verify(statement).execute(VALIDATION_QUERY);
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    public void testUnhealthyWithValidationQuery() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.execute(VALIDATION_QUERY)).thenThrow(new SQLException("ohnoes"));

        DataSourceHealthCheck healthCheck = new DataSourceHealthCheck(dataSource, VALIDATION_QUERY);
        try {
            healthCheck.check();
            fail("Expected an exception");
        } catch (SQLException e) {
            assertEquals("ohnoes", e.getMessage());
        }

        verify(statement).execute(VALIDATION_QUERY);
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    public void testDoesNotIssueRedundantValidationQuery() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenThrow(new RuntimeException("this shouldn't happen"));

        DataSourceFactory dataSourceFactory = new DataSourceFactory();
        dataSourceFactory.setCheckConnectionOnBorrow(true);
        DataSourceHealthCheck healthCheck = new DataSourceHealthCheck(dataSource, dataSourceFactory);
        assertTrue(healthCheck.check().isHealthy());

        verify(connection).close();
    }

    @Test
    public void testUsesValidationQueryIfRequired() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);

        DataSourceFactory dataSourceFactory = new DataSourceFactory();
        dataSourceFactory.setCheckConnectionOnBorrow(false);
        dataSourceFactory.setValidationQuery(VALIDATION_QUERY);
        DataSourceHealthCheck healthCheck = new DataSourceHealthCheck(dataSource, dataSourceFactory);
        assertTrue(healthCheck.check().isHealthy());

        verify(statement).execute(VALIDATION_QUERY);
        verify(statement).close();
        verify(connection).close();
    }

}

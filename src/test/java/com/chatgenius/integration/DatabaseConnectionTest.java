package com.chatgenius.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.test.context.TestPropertySource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:postgresql://chatgenius-db.cvmyig8ccjbp.us-east-2.rds.amazonaws.com:5432/chatgenius",
    "spring.datasource.username=malibu",
    "spring.datasource.password=G0neCr4zee1!",
    "spring.datasource.driver-class-name=org.postgresql.Driver"
})
public class DatabaseConnectionTest {

    @Autowired
    private DataSource dataSource;

    @Test
    public void testDatabaseConnection() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            assertNotNull(metaData);
            assertEquals("PostgreSQL", metaData.getDatabaseProductName());
        }
    }
} 
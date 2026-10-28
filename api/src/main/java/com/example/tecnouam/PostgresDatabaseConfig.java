package com.example.tecnouam;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PostgresDatabaseConfig {
    @Bean
    DataSource dataSource(@Value("${spring.datasource.url}") String url,
            @Value("${spring.datasource.username}") String username,
            @Value("${spring.datasource.password}") String password) {
        if (password.isBlank())
            throw new IllegalStateException("DB_PASSWORD no está configurada; defínala en api/.env o en el entorno");
        createDatabaseIfMissing(url, username, password);

        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setJdbcUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        return dataSource;
    }

    private void createDatabaseIfMissing(String url, String username, String password) {
        try (Connection ignored = DriverManager.getConnection(url, username, password)) {
            return;
        } catch (SQLException ex) {
            if (!"3D000".equals(ex.getSQLState()))
                throw new IllegalStateException("No se pudo conectar con PostgreSQL: " + ex.getMessage(), ex);
        }

        int queryStart = url.indexOf('?');
        String base = queryStart < 0 ? url : url.substring(0, queryStart);
        String query = queryStart < 0 ? "" : url.substring(queryStart);
        int lastSlash = base.lastIndexOf('/');
        if (!base.startsWith("jdbc:postgresql://") || lastSlash < "jdbc:postgresql://".length())
            throw new IllegalArgumentException("DB_URL debe contener host, puerto y nombre de base de datos");
        String database = base.substring(lastSlash + 1);
        if (!database.matches("[A-Za-z_][A-Za-z0-9_-]*"))
            throw new IllegalArgumentException("Nombre de base de datos inválido en DB_URL");
        String maintenanceUrl = base.substring(0, lastSlash + 1) + "postgres" + query;

        try (Connection connection = DriverManager.getConnection(maintenanceUrl, username, password);
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE DATABASE \"" + database + "\"");
        } catch (SQLException ex) {
            // Otro arranque pudo crear la base mientras se establecía la conexión.
            if (!"42P04".equals(ex.getSQLState()))
                throw new IllegalStateException("No se pudo crear la base PostgreSQL " + database + ": " + ex.getMessage(), ex);
        }
    }
}

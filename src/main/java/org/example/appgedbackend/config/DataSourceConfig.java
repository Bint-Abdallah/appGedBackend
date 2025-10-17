package org.example.appgedbackend.config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    @Value("${DATABASE_URL}")
    private String databaseUrl;

    @Bean
    public DataSource dataSource() {
        try {
            // Exemple : postgres://user:pass@host:port/db
            String cleanUrl = databaseUrl.replace("postgres://", "jdbc:postgresql://");
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setUrl(cleanUrl);
            dataSource.setDriverClassName("org.postgresql.Driver");
            return dataSource;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la configuration de la base de données", e);
        }
    }
}

package com.gorillaescape.backend.persistence;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;

@Configuration(proxyBeanMethods = false)
public class PersistenceConfiguration {
    @Bean(destroyMethod = "close")
    LocalStore localStore(@Value("${gorilla.storage.directory:}") String directory) throws IOException {
        return new LocalStore(directory);
    }

    @Bean(destroyMethod = "close")
    HikariDataSource dataSource(LocalStore store) {
        var sqlite = new SQLiteConfig();
        sqlite.enforceForeignKeys(true);
        sqlite.enableLoadExtension(false);
        sqlite.setBusyTimeout(3000);
        sqlite.setJournalMode(SQLiteConfig.JournalMode.DELETE);
        sqlite.setSynchronous(SQLiteConfig.SynchronousMode.FULL);
        var source = new SQLiteDataSource(sqlite);
        source.setUrl("jdbc:sqlite:" + store.database());
        var pool = new HikariConfig();
        pool.setDataSource(source);
        pool.setPoolName("players-sqlite");
        pool.setMaximumPoolSize(1);
        pool.setMinimumIdle(1);
        pool.setConnectionTimeout(3000);
        pool.setValidationTimeout(1000);
        return new HikariDataSource(pool);
    }
    @Bean
    org.springframework.jdbc.core.JdbcTemplate jdbcTemplate(HikariDataSource dataSource) {
        var template = new org.springframework.jdbc.core.JdbcTemplate(dataSource);
        template.setExceptionTranslator((task, sql, error) -> {
            // SQLite extended constraint codes retain primary code 19 in their low byte.
            if ((error.getErrorCode() & 0xff) == 19) {
                return new org.springframework.dao.DataIntegrityViolationException("DATA_CONSTRAINT", error);
            }
            return new org.springframework.jdbc.UncategorizedSQLException("SQL_OPERATION", null, error);
        });
        return template;
    }
    @Bean
    org.springframework.boot.flyway.autoconfigure.FlywayConfigurationCustomizer rejectFutureMigrations() {
        return config -> config.ignoreMigrationPatterns(new String[0]);
    }
}

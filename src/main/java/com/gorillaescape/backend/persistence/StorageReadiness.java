package com.gorillaescape.backend.persistence;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class StorageReadiness implements ApplicationListener<ApplicationReadyEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(StorageReadiness.class);
    private final JdbcTemplate jdbc;
    private final Flyway flyway;
    public StorageReadiness(JdbcTemplate jdbc, Flyway flyway) { this.jdbc = jdbc; this.flyway = flyway; }

    @Override public void onApplicationEvent(ApplicationReadyEvent event) {
        LOG.info("event=backend_ready port={} sqliteVersion={} schemaVersion={} javaVersion={}",
                event.getApplicationContext().getEnvironment().getProperty("local.server.port"),
                jdbc.queryForObject("SELECT sqlite_version()", String.class), flyway.info().current().getVersion(),
                Runtime.version());
    }
}

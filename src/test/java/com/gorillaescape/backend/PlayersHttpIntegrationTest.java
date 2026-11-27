package com.gorillaescape.backend;

import com.gorillaescape.backend.persistence.LocalStore;
import com.zaxxer.hikari.HikariDataSource;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;

class PlayersHttpIntegrationTest {
    @TempDir Path root;
    private ConfigurableApplicationContext context;
    private URI base;
    private Path directory;
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private final ObjectMapper json = new ObjectMapper();

    @BeforeEach void start() {
        directory = root.resolve("datos con espacio");
        boot();
    }
    private ConfigurableApplicationContext application(Path data) {
        var app = new SpringApplication(BackendApplication.class);
        app.setRegisterShutdownHook(false);
        return app.run("--server.port=0", "--gorilla.storage.directory=" + data,
                "--gorilla.http.allowed-origins=http://localhost:5173", "--logging.level.root=WARN");
    }
    private void boot() {
        context = application(directory);
        base = URI.create("http://127.0.0.1:" + context.getEnvironment().getProperty("local.server.port"));
    }
    @AfterEach void stop() { if (context != null) context.close(); }
    private HttpRequest postRequest(String body) {
        return HttpRequest.newBuilder(base.resolve("/api/players")).timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(body)).build();
    }
    private HttpResponse<String> post(String body) throws Exception { return client.send(postRequest(body), HttpResponse.BodyHandlers.ofString()); }
    private HttpResponse<String> get(String path) throws Exception {
        return client.send(HttpRequest.newBuilder(base.resolve(path)).timeout(Duration.ofSeconds(15)).GET().build(), HttpResponse.BodyHandlers.ofString());
    }
    private long count() throws Exception {
        try (var c = DriverManager.getConnection("jdbc:sqlite:" + directory.resolve("players.sqlite"));
             var s = c.createStatement(); var rows = s.executeQuery("SELECT count(*) FROM players")) {
            rows.next(); return rows.getLong(1);
        }
    }

    @Test void httpPostCommitsToARealFileAndGetReturnsTheSameRecord() throws Exception {
        assertEquals(0, count());
        var result = post("{\"displayName\":\"  Jose\\u0301  \"}");
        assertEquals(201, result.statusCode(), result.body());
        var player = json.readTree(result.body());
        assertEquals("José", player.path("displayName").asString());
        assertNotNull(java.util.UUID.fromString(player.path("id").asString()));
        assertNotNull(java.time.Instant.parse(player.path("createdAt").asString()));
        assertEquals(1, count());
        try (var c = DriverManager.getConnection("jdbc:sqlite:" + directory.resolve("players.sqlite"));
             var s = c.createStatement(); var rows = s.executeQuery("SELECT id,display_name FROM players")) {
            assertTrue(rows.next()); assertEquals(player.path("id").asString(), rows.getString(1)); assertEquals("José", rows.getString(2));
        }
        var listed = get("/api/players");
        assertEquals(200, listed.statusCode());
        assertEquals(player, json.readTree(listed.body()).path("items").get(0));
        assertTrue(result.headers().firstValue("Server-Timing").orElseThrow().startsWith("app;dur="));
        assertNotNull(java.util.UUID.fromString(result.headers().firstValue("X-Request-Id").orElseThrow()));
        assertEquals("no-store", result.headers().firstValue("Cache-Control").orElseThrow());
    }

    @Test void invalidAndUnknownFieldsDoNotWriteAnyRows() throws Exception {
        for (String body : List.of("{}", "{\"displayName\":null}", "{\"displayName\":\"   \"}",
                "{\"displayName\":\"" + "A".repeat(41) + "\"}", "{\"displayName\":\"A\",\"score\":99}",
                "{\"displayName\":12}", "{\"displayName\":true}", "{\"displayName\":\"A\",\"displayName\":\"B\"}", "[]", "{broken", "{\"displayName\":\"A;DROP TABLE players\"}")) {
            var result = post(body);
            assertEquals(400, result.statusCode(), body + " => " + result.body());
            var error = json.readTree(result.body());
            assertEquals(400, error.path("status").asInt());
            assertEquals(result.headers().firstValue("X-Request-Id").orElseThrow(), error.path("requestId").asString());
        }
        assertEquals(0, count());
    }

    @Test void enforcesBodyLimitForFixedAndChunkedTransfers() throws Exception {
        assertEquals(413, post("{\"displayName\":\"" + "A".repeat(5000) + "\"}").statusCode());
        var request = HttpRequest.newBuilder(base.resolve("/api/players")).header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofInputStream(() -> new java.io.ByteArrayInputStream(new byte[5000]))).build();
        var result = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(413, result.statusCode());
        assertEquals("BODY_TOO_LARGE", json.readTree(result.body()).path("code").asString());
        assertEquals(0, count());
    }

    @Test void validatesPaginationAndParameterTypes() throws Exception {
        for (String query : List.of("limit=0", "limit=101", "offset=-1", "offset=100001", "limit=abc")) {
            assertEquals(400, get("/api/players?" + query).statusCode(), query);
        }
        post("{\"displayName\":\"O'Brien\"}");
        assertEquals(0, json.readTree(get("/api/players?limit=1&offset=1").body()).path("items").size());
    }

    @Test void survivesRestartWithoutReinitializingAndMigratesOnlyOnce() throws Exception {
        var created = json.readTree(post("{\"displayName\":\"Persona Persistente\"}").body());
        context.close(); context = null;
        boot();
        assertEquals(created, json.readTree(get("/api/players").body()).path("items").get(0));
        try (var c = DriverManager.getConnection("jdbc:sqlite:" + directory.resolve("players.sqlite")); var s = c.createStatement()) {
            try (var rows = s.executeQuery("SELECT count(*) FROM flyway_schema_history WHERE success=1")) {
                rows.next(); assertEquals(1, rows.getInt(1));
            }
            try (var rows = s.executeQuery("PRAGMA integrity_check")) { rows.next(); assertEquals("ok", rows.getString(1)); }
            try (var rows = s.executeQuery("PRAGMA foreign_key_check")) { assertFalse(rows.next()); }
        }
        try (var c = context.getBean(HikariDataSource.class).getConnection(); var s = c.createStatement(); var rows = s.executeQuery("PRAGMA foreign_keys")) {
            rows.next(); assertEquals(1, rows.getInt(1));
        }
    }

    @Test void concurrentCreatesCommitOneRowPerSuccessfulRequestAndUseDifferentIdentities() throws Exception {
        List<CompletableFuture<HttpResponse<String>>> pending = new ArrayList<>();
        for (int i=0; i<8; i++) pending.add(client.sendAsync(postRequest("{\"displayName\":\"Persona " + i + "\"}"), HttpResponse.BodyHandlers.ofString()));
        var ids = new HashSet<String>();
        for (var request : pending) {
            var result = request.get(); assertEquals(201, result.statusCode(), result.body()); ids.add(json.readTree(result.body()).path("id").asString());
        }
        assertEquals(8, ids.size()); assertEquals(8, count());
    }

    @Test void unavailableStorageIs503AndCannotExposeInternalDetails() throws Exception {
        context.getBean(HikariDataSource.class).close();
        var result = post("{\"displayName\":\"Dato Privado\"}");
        assertEquals(503, result.statusCode(), result.body());
        assertEquals("STORAGE_UNAVAILABLE", json.readTree(result.body()).path("code").asString());
        assertFalse(result.body().contains("Dato Privado")); assertFalse(result.body().contains("SQLException")); assertEquals(0, count());
    }

    @Test void constraintFailureRollsBackAndReturnsSanitizedConflict() throws Exception {
        try (var c = DriverManager.getConnection("jdbc:sqlite:" + directory.resolve("players.sqlite")); var s = c.createStatement()) {
            s.execute("CREATE TRIGGER reject_test BEFORE INSERT ON players BEGIN SELECT RAISE(ABORT,'fixture private detail'); END");
        }
        var result = post("{\"displayName\":\"Persona\"}");
        assertEquals(409, result.statusCode(), result.body());
        assertFalse(result.body().contains("fixture private")); assertEquals(0, count());
    }

    @Test void refusesSecondOwnerAndPreservesAnUnreadableDatabase() throws Exception {
        assertThrows(Exception.class, () -> application(directory));
        assertEquals(200, get("/api/players").statusCode());
        Path bad = root.resolve("bad"); Files.createDirectory(bad);
        byte[] bytes = "fixture-not-a-database-and-must-not-be-erased".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        Files.write(bad.resolve("players.sqlite"), bytes);
        assertThrows(Exception.class, () -> application(bad));
        assertArrayEquals(bytes, Files.readAllBytes(bad.resolve("players.sqlite")));
        try (var released = new LocalStore(bad.toString())) { assertTrue(Files.exists(released.database())); }
    }

    @Test void corsIsExplicitAndNeverWildcard() throws Exception {
        var allowed = HttpRequest.newBuilder(base.resolve("/api/players")).method("OPTIONS", HttpRequest.BodyPublishers.noBody())
                .header("Origin", "http://localhost:5173").header("Access-Control-Request-Method", "POST").build();
        var result = client.send(allowed, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, result.statusCode());
        assertEquals("http://localhost:5173", result.headers().firstValue("Access-Control-Allow-Origin").orElseThrow());
        var denied = HttpRequest.newBuilder(base.resolve("/api/players")).GET().header("Origin", "https://untrusted.invalid").build();
        assertEquals(403, client.send(denied, HttpResponse.BodyHandlers.ofString()).statusCode());
    }

    @Test void mediaTypeAndMethodsHaveCorrectErrors() throws Exception {
        var text = HttpRequest.newBuilder(base.resolve("/api/players")).header("Content-Type", "text/plain").POST(HttpRequest.BodyPublishers.ofString("hello")).build();
        assertEquals(415, client.send(text, HttpResponse.BodyHandlers.ofString()).statusCode());
        var deleted = HttpRequest.newBuilder(base.resolve("/api/players")).DELETE().build();
        var result = client.send(deleted, HttpResponse.BodyHandlers.ofString());
        assertEquals(405, result.statusCode());
        assertTrue(result.headers().firstValue("Allow").orElseThrow().contains("POST"));
        assertEquals(404, get("/api/unknown").statusCode());
    }
    @Test void servesTheVersionedOpenApiContract() throws Exception {
        var result = get("/openapi.json");
        assertEquals(200, result.statusCode());
        var document = json.readTree(result.body());
        assertEquals("3.1.0", document.path("openapi").asString());
        assertTrue(document.path("paths").path("/api/players").has("get"));
        assertTrue(document.path("paths").path("/api/players").has("post"));
    }
    @Test void rejectsADatabaseMigratedByANewerApplicationWithoutDeletingData() throws Exception {
        post("{\"displayName\":\"Conservar Persona\"}");
        context.close(); context = null;
        try (var c = DriverManager.getConnection("jdbc:sqlite:" + directory.resolve("players.sqlite")); var statement = c.createStatement()) {
            statement.execute("INSERT INTO flyway_schema_history(installed_rank,version,description,type,script,installed_by,execution_time,success) "
                    + "VALUES(2,'2','future fixture','SQL','V2__future.sql','test',0,1)");
        }
        assertThrows(Exception.class, () -> application(directory));
        assertEquals(1, count());
        try (var released = new LocalStore(directory.toString())) { assertTrue(Files.exists(released.database())); }
    }
}

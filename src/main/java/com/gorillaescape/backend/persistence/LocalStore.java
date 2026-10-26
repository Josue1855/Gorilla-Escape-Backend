package com.gorillaescape.backend.persistence;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermissions;

/** Owns this backend's data directory only; never removes the lock file. */
public final class LocalStore implements AutoCloseable {
    private final Path database;
    private final FileChannel channel;
    private final FileLock lock;

    public LocalStore(String configuredDirectory) throws IOException {
        Path directory = (configuredDirectory == null || configuredDirectory.isBlank()
                ? defaultDirectory() : Path.of(configuredDirectory)).toAbsolutePath().normalize();
        if (Files.isSymbolicLink(directory)) throw new IOException("DATA_DIRECTORY_SYMLINK");
        boolean existed = Files.exists(directory);
        Files.createDirectories(directory);
        if (!existed && Files.getFileStore(directory).supportsFileAttributeView("posix")) {
            Files.setPosixFilePermissions(directory, PosixFilePermissions.fromString("rwx------"));
        }
        database = directory.resolve("players.sqlite");
        Path lockPath = directory.resolve("backend.lock");
        if (Files.isSymbolicLink(database) || Files.isSymbolicLink(lockPath)) {
            throw new IOException("DATA_FILE_SYMLINK");
        }
        channel = FileChannel.open(lockPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        try {
            lock = channel.tryLock();
            if (lock == null) throw new IOException("DATA_DIRECTORY_IN_USE");
        } catch (IOException | OverlappingFileLockException failure) {
            channel.close();
            throw new IOException("DATA_DIRECTORY_IN_USE", failure);
        }
    }

    public Path database() { return database; }

    private static Path defaultDirectory() {
        String home = System.getProperty("user.home");
        if (System.getProperty("os.name").toLowerCase(java.util.Locale.ROOT).contains("win")) {
            String local = System.getenv("LOCALAPPDATA");
            return (local == null || local.isBlank() ? Path.of(home, "AppData", "Local") : Path.of(local))
                    .resolve("GorillaEscape/backend");
        }
        String xdg = System.getenv("XDG_DATA_HOME");
        return (xdg == null || xdg.isBlank() || !Path.of(xdg).isAbsolute()
                ? Path.of(home, ".local", "share") : Path.of(xdg)).resolve("gorilla-escape/backend");
    }

    @Override public void close() throws IOException {
        try { if (lock.isValid()) lock.release(); }
        finally { channel.close(); }
    }
}

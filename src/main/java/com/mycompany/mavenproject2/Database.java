package com.mycompany.mavenproject2;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Opens connections to the cas_db database in the cloud (TiDB Cloud) or on
 * a local MySQL server.
 *
 * The settings come from db.properties next to the app, so the server
 * can change without rebuilding. Every other class gets its connection
 * from here.
 *
 * Opening an encrypted connection to the cloud takes a moment, so closed
 * connections are kept and reused instead of being thrown away.
 *
 * @author Luis Gabriel Santiago
 */
public class Database {

    private static final String FILE_NAME = "db.properties";
    private static final Properties SETTINGS = new Properties();

    /** Idle connections ready for reuse. */
    private static final Deque<Pooled> IDLE = new ArrayDeque<>();
    private static final int MAX_IDLE = 4;
    /** A connection unused for this long is checked before it is reused. */
    private static final long CHECK_AFTER_MS = 30_000;

    private record Pooled(Connection connection, long lastUsed) {
    }

    static {
        load();
    }

    private Database() {
    }

    /** Reads db.properties. Missing values fall back to a local server. */
    public static void load() {
        SETTINGS.setProperty("db.host", "localhost");
        SETTINGS.setProperty("db.port", "3306");
        SETTINGS.setProperty("db.name", "cas_db");
        SETTINGS.setProperty("db.user", "cas_app");
        SETTINGS.setProperty("db.password", "");
        SETTINGS.setProperty("db.sslMode", "PREFERRED");
        File file = new File(FILE_NAME);
        if (file.exists()) {
            try (InputStream in = new FileInputStream(file)) {
                SETTINGS.load(in);
            } catch (IOException e) {
                System.err.println("Could not read " + FILE_NAME + ": " + e.getMessage());
            }
        }
    }

    /** The JDBC address of the database. db.url in db.properties overrides it. */
    public static String getUrl() {
        String custom = SETTINGS.getProperty("db.url", "").trim();
        if (!custom.isEmpty()) {
            return custom;
        }
        // TiDB Cloud needs sslMode=VERIFY_IDENTITY; a local MySQL works with PREFERRED
        String sslMode = SETTINGS.getProperty("db.sslMode", "PREFERRED").trim();
        return "jdbc:mysql://" + getHost() + ":" + SETTINGS.getProperty("db.port").trim()
                + "/" + SETTINGS.getProperty("db.name").trim()
                + "?sslMode=" + sslMode + "&allowPublicKeyRetrieval=true&connectTimeout=10000";
    }

    /** Gives a connection. Close it with try-with-resources; it goes back to the pool. */
    public static Connection getConnection() throws SQLException {
        long now = System.currentTimeMillis();
        while (true) {
            Pooled pooled;
            synchronized (IDLE) {
                pooled = IDLE.pollFirst();
            }
            if (pooled == null) {
                break;
            }
            Connection real = pooled.connection();
            boolean fresh = now - pooled.lastUsed() < CHECK_AFTER_MS;
            try {
                if (!real.isClosed() && (fresh || real.isValid(3))) {
                    return wrap(real);
                }
            } catch (SQLException ignored) {
                // a dead connection is replaced below
            }
            closeQuietly(real);
        }
        Connection real = DriverManager.getConnection(getUrl(),
                SETTINGS.getProperty("db.user").trim(),
                SETTINGS.getProperty("db.password"));
        return wrap(real);
    }

    /** Wraps a real connection so that close() returns it to the pool. */
    private static Connection wrap(Connection real) {
        AtomicBoolean closed = new AtomicBoolean(false);
        return (Connection) Proxy.newProxyInstance(Database.class.getClassLoader(),
                new Class<?>[]{Connection.class}, (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "close":
                            if (closed.compareAndSet(false, true)) {
                                release(real);
                            }
                            return null;
                        case "isClosed":
                            return closed.get() || real.isClosed();
                        default:
                            if (closed.get()) {
                                throw new SQLException("Connection already closed");
                            }
                            try {
                                return method.invoke(real, args);
                            } catch (InvocationTargetException e) {
                                throw e.getCause();
                            }
                    }
                });
    }

    private static void release(Connection real) {
        try {
            if (real.isClosed()) {
                return;
            }
            if (!real.getAutoCommit()) {
                real.rollback();
                real.setAutoCommit(true);
            }
        } catch (SQLException e) {
            closeQuietly(real);
            return;
        }
        synchronized (IDLE) {
            if (IDLE.size() < MAX_IDLE) {
                IDLE.addFirst(new Pooled(real, System.currentTimeMillis()));
                return;
            }
        }
        closeQuietly(real);
    }

    /** Closes every pooled connection. Called when the app exits. */
    public static void shutdown() {
        synchronized (IDLE) {
            for (Pooled pooled : IDLE) {
                closeQuietly(pooled.connection());
            }
            IDLE.clear();
        }
    }

    private static void closeQuietly(Connection connection) {
        try {
            connection.close();
        } catch (SQLException ignored) {
            // nothing else to do
        }
    }

    /** True when the server answers. Used before showing the login screen. */
    public static boolean testConnection() {
        try (Connection connection = getConnection()) {
            return connection.isValid(5);
        } catch (SQLException e) {
            return false;
        }
    }

    public static String getHost() {
        return SETTINGS.getProperty("db.host").trim();
    }
}

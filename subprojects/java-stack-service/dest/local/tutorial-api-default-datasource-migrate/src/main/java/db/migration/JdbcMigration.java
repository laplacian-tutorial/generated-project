package db.migration;

import org.flywaydb.core.Flyway;

public class JdbcMigration {
    public static final void main(String... args) {
        String url = System.getProperty("datasource.url");
        String user = System.getProperty("datasource.username");
        String password = System.getProperty("datasource.password");

        Flyway
            .configure()
            .connectRetries(10)
            .dataSource(url, user, password)
            .load()
            .migrate();
    }
}

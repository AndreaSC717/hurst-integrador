package com.husrt.db;

import com.husrt.config.AppProperties;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public final class DataSourceManager {

    private static volatile HikariDataSource dataSource;

    private DataSourceManager() {
    }

    public static DataSource get() {
        if (dataSource == null) {
            synchronized (DataSourceManager.class) {
                if (dataSource == null) {
                    HikariConfig cfg = new HikariConfig();
                    cfg.setJdbcUrl(AppProperties.jdbcUrl());
                    cfg.setUsername(AppProperties.dbUser());
                    cfg.setPassword(AppProperties.dbPassword());
                    cfg.setMaximumPoolSize(10);
                    cfg.setPoolName("husrt-pool");
                    dataSource = new HikariDataSource(cfg);
                }
            }
        }
        return dataSource;
    }

    public static void shutdown() {
        if (dataSource != null) {
            dataSource.close();
            dataSource = null;
        }
    }
}

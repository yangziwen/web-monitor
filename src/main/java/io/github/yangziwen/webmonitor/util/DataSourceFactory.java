package io.github.yangziwen.webmonitor.util;

import javax.sql.DataSource;

import org.apache.tomcat.jdbc.pool.PoolProperties;

import io.github.yangziwen.webmonitor.GlobalConfig;

public class DataSourceFactory {

	private static final DataSource DATA_SOURCE = initDataSource();

	private DataSourceFactory() {}

    public static DataSource initDataSource() {
        PoolProperties config = new PoolProperties();
        config.setDriverClassName(GlobalConfig.database.driver_class_name);
        config.setUrl(GlobalConfig.database.url);
        config.setUsername(GlobalConfig.database.username);
        config.setPassword(GlobalConfig.database.password);
        config.setMinIdle(GlobalConfig.database.min_idle);
        config.setMaxIdle(GlobalConfig.database.max_idle);
        config.setMaxActive(GlobalConfig.database.max_active);
        return new org.apache.tomcat.jdbc.pool.DataSource(config);

    }

	public static DataSource getDataSource() {
		return DATA_SOURCE;
	}

}

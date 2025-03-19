package com.back.global.jpa.replication

import com.zaxxer.hikari.HikariDataSource
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.*
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy
import javax.sql.DataSource

@Profile("prod")
@Configuration
class CustomDataSourceConfig {
    // Write replica 정보로 만든 DataSource
    @Bean
    @ConfigurationProperties(prefix = "custom.datasource.source")
    fun sourceDataSource(): DataSource {
        val ds = DataSourceBuilder.create().type(HikariDataSource::class.java).build()

        return ds
    }

    // Read replica 정보로 만든 DataSource
    @Bean
    @ConfigurationProperties(prefix = "custom.datasource.replica1")
    fun replica1DataSource(): DataSource {
        return DataSourceBuilder.create().type(HikariDataSource::class.java).build()
    }

    @Bean
    @ConfigurationProperties(prefix = "custom.datasource.replica2")
    fun replica2DataSource(): DataSource {
        return DataSourceBuilder.create().type(HikariDataSource::class.java).build()
    }

    @Bean
    @DependsOn("sourceDataSource", "replica1DataSource", "replica2DataSource")
    fun routeDataSource(): DataSource {
        val dataSourceRouter = DataSourceRouter()
        val sourceDataSource = sourceDataSource()
        val replica1DataSource = replica1DataSource()
        val replica2DataSource = replica2DataSource()

        val dataSourceMap = HashMap<Any, Any>()
        dataSourceMap[0] = sourceDataSource
        dataSourceMap[1] = replica1DataSource
        dataSourceMap[2] = replica2DataSource
        dataSourceRouter.setTargetDataSources(dataSourceMap)
        dataSourceRouter.setDefaultTargetDataSource(sourceDataSource)

        return dataSourceRouter
    }

    @Bean
    @Primary
    @DependsOn("routeDataSource")
    fun dataSource(): DataSource {
        return LazyConnectionDataSourceProxy(routeDataSource())
    }
}
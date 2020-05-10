/**
 * This content is released under the MIT License (MIT)
 *
 * Copyright (c) 2020, canchito-dev
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * @author 		José Carlos Mendoza Prego
 * @copyright	Copyright (c) 2020, canchito-dev (http://www.canchito-dev.com)
 * @license		http://opensource.org/licenses/MIT	MIT License
 * @link		http://www.canchito-dev.com/public/blog/2020/05/10/integrate-flowable-into-your-spring-boot-application/
 * @link		https://github.com/canchito-dev/spring-flowable-integration
 **/

package com.canchitodev.example.springflowableintegration.configuration;

import com.zaxxer.hikari.HikariDataSource;
import org.flowable.common.engine.impl.persistence.StrongUuidGenerator;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
import org.flowable.spring.boot.ProcessEngineAutoConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;

import javax.sql.DataSource;

@Primary
@Configuration
@AutoConfigureOrder(Ordered.LOWEST_PRECEDENCE) // Makes sure that this configuration will be processed last by Spring Boot
@AutoConfigureAfter(ProcessEngineAutoConfiguration.class)
public class FlowableEngineConfiguration {
	
	@Primary
	@Bean
    @ConfigurationProperties("flowable.datasource")
    public DataSourceProperties flowableDataSourceProperties() {
        return new DataSourceProperties();
    }
	
	@Primary
	@Bean
	public HikariDataSource flowableDataSource(
			@Qualifier("flowableDataSourceProperties") DataSourceProperties flowableDataSourceProperties
	) {
		return flowableDataSourceProperties
				.initializeDataSourceBuilder()
				.type(HikariDataSource.class)
                .build();
	}
	
	@Bean
    EngineConfigurationConfigurer<SpringProcessEngineConfiguration> EngineConfigurationConfigurer(
			@Qualifier("flowableDataSource") DataSource dataSource
	) {
		return engineConfiguration -> {
			/**
             * Flowable DOCS (v 6.4.2)'s user guide - 19.3. UUID ID generator for high concurrency
             * http://www.flowable.org/docs/userguide/index.html#advanced.uuid.generator
             **/
			engineConfiguration.setIdGenerator(new StrongUuidGenerator());
			
			/**
             * Flowable DOCS (v 6.4.2)'s user guide - 3.3. Database configuration
             * http://www.flowable.org/docs/userguide/index.html#databaseConfiguration
			 *
			 * The data source that is constructed based on the provided JDBC properties will have the default MyBatis connection pool settings. The following attributes can
			 * optionally be set to tweak that connection pool (taken from the MyBatis documentation):
			 * - jdbcMaxActiveConnections: The number of active connections that the connection pool at maximum at any time can contain. Default is 10.
			 * - jdbcMaxIdleConnections: The number of idle connections that the connection pool at maximum at any time can contain.
			 * - jdbcMaxCheckoutTime: The amount of time in milliseconds a connection can be checked out from the connection pool before it is forcefully returned. Default is 20000 (20 seconds).
			 * - jdbcMaxWaitTime: This is a low level setting that gives the pool a chance to print a log status and re-attempt the acquisition of a connection in the case that it is taking unusually
			 * 					  long (to avoid failing silently forever if the pool is misconfigured) Default is 20000 (20 seconds).
             **/
			engineConfiguration.setDataSource(dataSource);
//			engineConfiguration.setJdbcMaxActiveConnections(jdbcMaxActiveConnections);
//			engineConfiguration.setJdbcMaxCheckoutTime(jdbcMaxCheckoutTime);
//			engineConfiguration.setJdbcMaxIdleConnections(jdbcMaxIdleConnections);
//			engineConfiguration.setJdbcMaxWaitTime(jdbcMaxWaitTime);
			
			/**
             * Flowable DOCS (v 6.4.2)'s user guide - 3.18. Event handlers
             * https://www.flowable.org/docs/userguide/index.html#eventDispatcher
             **/
//			engineConfiguration.setEventListeners(Arrays.<FlowableEventListener>asList(new DefaultEventHandler()));
		};
	}
}
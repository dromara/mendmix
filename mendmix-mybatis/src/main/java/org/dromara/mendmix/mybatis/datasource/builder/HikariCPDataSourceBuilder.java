/*
 * Copyright 2016-2020 dromara.org.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dromara.mendmix.mybatis.datasource.builder;

import javax.sql.DataSource;

import org.dromara.mendmix.common.util.BeanUtils;
import org.dromara.mendmix.mybatis.datasource.DataSourceConfig;

import com.zaxxer.hikari.HikariDataSource;

public class HikariCPDataSourceBuilder {
	public static DataSource builder(DataSourceConfig config){
		HikariDataSource dataSource = BeanUtils.copy(config, HikariDataSource.class);
		dataSource.setJdbcUrl(config.getUrl());
    	return dataSource;
	}	
}
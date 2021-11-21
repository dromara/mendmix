package com.jeesuite.mybatis.datasource;

import org.apache.commons.lang3.StringUtils;

import com.jeesuite.common.JeesuiteBaseException;
import com.jeesuite.common.GlobalConstants;
import com.jeesuite.mybatis.MybatisConfigs;

/**
 * 
 * <br>
 * Class Name   : DataSourceConfig
 *
 * @author jiangwei
 * @version 1.0.0
 * @date 2020年6月30日
 */
public class DataSourceConfig {

	public static final String SLAVE_KEY = "slave";

	public static final String MASTER_KEY = "master";

	public static String DEFAULT_GROUP_NAME = "default";
	
	private String group = DEFAULT_GROUP_NAME;
	private String tenantId;
	private String url;
	private String username;
	private String password;
	private Boolean  master;
	private Integer index;
	
	//
	private String driverClassName;
	private Boolean testWhileIdle;
	private String validationQuery;
	private Integer maxActive;
	private Integer initialSize;
	private Integer minIdle;
	private Long maxWait;
	private Long minEvictableIdleTimeMillis;
	private Long timeBetweenEvictionRunsMillis;
	private Boolean testOnBorrow;
	private Boolean testOnReturn;
	
	
	
	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Boolean getMaster() {
		return master;
	}

	public void setMaster(Boolean master) {
		this.master = master;
	}

	public Integer getIndex() {
		return index;
	}

	public void setIndex(Integer index) {
		this.index = index;
	}

	public String getDriverClassName() {
		return driverClassName;
	}

	public void setDriverClassName(String driverClassName) {
		this.driverClassName = driverClassName;
	}

	public Boolean getTestWhileIdle() {
		return testWhileIdle;
	}

	public void setTestWhileIdle(Boolean testWhileIdle) {
		this.testWhileIdle = testWhileIdle;
	}

	public String getValidationQuery() {
		return validationQuery;
	}

	public void setValidationQuery(String validationQuery) {
		this.validationQuery = validationQuery;
	}

	public Integer getMaxActive() {
		return maxActive;
	}

	public void setMaxActive(Integer maxActive) {
		this.maxActive = maxActive;
	}

	public Integer getInitialSize() {
		return initialSize;
	}

	public void setInitialSize(Integer initialSize) {
		this.initialSize = initialSize;
	}

	public Integer getMinIdle() {
		return minIdle;
	}

	public void setMinIdle(Integer minIdle) {
		this.minIdle = minIdle;
	}

	public Long getMaxWait() {
		return maxWait;
	}

	public void setMaxWait(Long maxWait) {
		this.maxWait = maxWait;
	}

	public Long getMinEvictableIdleTimeMillis() {
		return minEvictableIdleTimeMillis;
	}

	public void setMinEvictableIdleTimeMillis(Long minEvictableIdleTimeMillis) {
		this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
	}

	public Long getTimeBetweenEvictionRunsMillis() {
		return timeBetweenEvictionRunsMillis;
	}

	public void setTimeBetweenEvictionRunsMillis(Long timeBetweenEvictionRunsMillis) {
		this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
	}

	public Boolean getTestOnBorrow() {
		return testOnBorrow;
	}

	public void setTestOnBorrow(Boolean testOnBorrow) {
		this.testOnBorrow = testOnBorrow;
	}

	public Boolean getTestOnReturn() {
		return testOnReturn;
	}

	public void setTestOnReturn(Boolean testOnReturn) {
		this.testOnReturn = testOnReturn;
	}

	public String dataSourceKey() {
		return buildDataSourceKey(group, tenantId, master, index);
	}
	
	public void validate() {
		if(StringUtils.isAnyBlank(url,username,password)) {
			throw new JeesuiteBaseException("DataSourceConfig[url,username,password] is required");
		}
		//租户分库
		if(StringUtils.isBlank(tenantId) && MybatisConfigs.isSchameSharddingTenant(group)) {
			throw new JeesuiteBaseException("DataSourceConfig[tenantId] is required For SchameSharddingTenant");
		}
	}
	
	
	public static String buildDataSourceKey(String group,String tenantId,boolean master,int index) {
		StringBuilder builder = new StringBuilder(group).append(GlobalConstants.UNDER_LINE);
		if(tenantId != null) {
			builder.append(tenantId).append(GlobalConstants.UNDER_LINE);
		}
		builder.append(master ? MASTER_KEY : SLAVE_KEY).append(GlobalConstants.UNDER_LINE);
		builder.append(index);
		return builder.toString();
	}
	
	@Override
	public String toString() {
		return "DataSourceConfig [group=" + group + ", tenantId=" + tenantId + ", url=" + url + ", username=" + username
				+ ", master=" + master + ", index=" + index + ", maxActive=" + maxActive + "]";
	}
	
}

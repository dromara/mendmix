/*
 * Copyright 2016-2022 www.mendmix.com.
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
package com.mendmix.gateway;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.config.GatewayProperties;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;

import com.mendmix.common.GlobalRuntimeContext;
import com.mendmix.common.MendmixBaseException;
import com.mendmix.common.http.HostMappingHolder;
import com.mendmix.common.http.HttpRequestEntity;
import com.mendmix.common.model.ApiInfo;
import com.mendmix.common.util.ResourceUtils;
import com.mendmix.gateway.api.SystemMgtApi;
import com.mendmix.gateway.model.BizSystemModule;
import com.mendmix.gateway.model.BizSystemPortal;
import com.mendmix.spring.InstanceFactory;
import com.mendmix.springweb.exporter.AppMetadataHolder;
import com.mendmix.springweb.model.AppMetadata;

/**
 * <br>
 * Class Name : CurrentSystemHolder
 *
 * @author jiangwei
 * @version 1.0.0
 * @date 2019年12月3日
 */
public class CurrentSystemHolder {
	
	private static Logger log = LoggerFactory.getLogger("com.mendmix.gateway");


	private static AtomicReference<Map<String, BizSystemModule>> routeModuleMappings = new AtomicReference<>();

	private static List<BizSystemModule> localModules;

	private static Set<String> routeNames;
	
	private static Map<String, Map<String, ApiInfo>> moduleApiInfos = new HashMap<>();
	
	private static int fetchApiMetaRound = 0;

	public static BizSystemModule getModule(String route) {
		BizSystemModule module = routeModuleMappings.get().get(route);
		if (module == null) {
			module = routeModuleMappings.get().get(GlobalRuntimeContext.APPID);
		}
		return module;
	}

	public static Map<String, BizSystemModule> getRouteModuleMappings() {
		if (routeModuleMappings.get() == null) {
			load();
		}
		return Collections.unmodifiableMap(routeModuleMappings.get());
	}

	public static Set<String> getRouteNames() {
		if (routeNames == null) {
			if (routeModuleMappings.get() == null) {
				load();
			}
			synchronized(CurrentSystemHolder.class) {
				if(routeNames != null)return routeNames;
				routeNames = new HashSet<>(routeModuleMappings.get().keySet());
			}
		}
		return routeNames;
	}

	public static Collection<BizSystemModule> getModules() {
		if (routeModuleMappings.get() == null)
			return new ArrayList<>(0);
		return Collections.unmodifiableCollection(routeModuleMappings.get().values());
	}
	
	public static BizSystemPortal getSystemPortal(String host) {
		return null;
	}

	public static synchronized void load() {
		//
		loadLocalRouteModules();
		List<BizSystemModule> modules;
		try {
			modules = InstanceFactory.getInstance(SystemMgtApi.class).getSystemModules();
			if (!localModules.isEmpty()) {
				modules.addAll(localModules);
			}
		} catch (Exception e) {
			modules = localModules;
		}
		//网关本身
		if(!modules.stream().anyMatch(o -> GlobalRuntimeContext.APPID.equalsIgnoreCase(o.getServiceId()))) {
			BizSystemModule module = new BizSystemModule();
			module.setServiceId(GlobalRuntimeContext.APPID);
			module.setRouteName(GlobalRuntimeContext.APPID);
			module.setStripPrefix(0);
			modules.add(module);
			localModules.add(module);
		}

		Map<String, BizSystemModule> _modules = new HashMap<>(modules.size());
		for (BizSystemModule module : modules) {
			//
			if(!module.isGateway()) {
				module.format();
				if(module.getProxyUri().startsWith("http")) {
					HostMappingHolder.addProxyUrlMapping(module.getServiceId(), module.getProxyUri());
				}
			}
			//
			if(moduleApiInfos.containsKey(module.getServiceId())) {
				module.setApiInfos(moduleApiInfos.get(module.getServiceId()));
			}
			_modules.put(module.getRouteName(), module);
		}
		routeModuleMappings.set(_modules);
		//
		//查询api信息
		if(fetchApiMetaRound == 0){
			new Thread(() -> {
				while(_modules.size() > moduleApiInfos.size() && fetchApiMetaRound < 360) {
					for (BizSystemModule module : _modules.values()) {
						if(moduleApiInfos.containsKey(module.getServiceId()))continue;
						initModuleApiInfos(module);
					}
					fetchApiMetaRound++;
					try {Thread.sleep(10000);} catch (Exception e) {}
				}
			}).start();
		}
	}

	private static void loadLocalRouteModules() {
		if(localModules != null)return;
		localModules = new ArrayList<>();
		
		List<RouteDefinition> defaultRouteDefs = InstanceFactory.getInstance(GatewayProperties.class).getRoutes();
		
		Properties properties = ResourceUtils.getAllProperties("spring.cloud.gateway.routes");
		Set<Entry<Object, Object>> entrySet = properties.entrySet();
		
		BizSystemModule module;
		String prefix;
		for (Entry<Object, Object> entry : entrySet) {
			if(entry.getKey().toString().endsWith(".id")) {
				prefix = entry.getKey().toString().replace(".id", "");
				module = new BizSystemModule();
				module.setDefaultRoute(true);
				module.setServiceId(entry.getValue().toString());
				module.setProxyUri(properties.getProperty(prefix + ".uri"));
				//
				updateModuleRouteInfos(module, defaultRouteDefs);
				localModules.add(module);
			}
		}

	}
	
	/**
	 * @param module
	 * @param definition
	 */
	private static void updateModuleRouteInfos(BizSystemModule module, List<RouteDefinition> defaultRouteDefs ) {	
		RouteDefinition routeDef = defaultRouteDefs.stream().filter(def -> StringUtils.equalsIgnoreCase(module.getServiceId(), def.getId())).findFirst().orElse(null);
		if(routeDef == null)return;
		FilterDefinition stripPrefixDef = routeDef.getFilters().stream().filter(p -> "StripPrefix".equals(p.getName())).findFirst().orElse(null);
		int stripPrefix = 0;
		if(stripPrefixDef != null) {
			stripPrefix = Integer.parseInt(stripPrefixDef.getArgs().get("_genkey_0"));
		}
		module.setStripPrefix(stripPrefix);
		PredicateDefinition pathDef = routeDef.getPredicates().stream().filter(p -> "Path".equals(p.getName())).findFirst().orElse(null);
		if(pathDef != null) {
			String pathPattern = pathDef.getArgs().get("_genkey_0");
			if(!pathPattern.startsWith(GatewayConstants.PATH_PREFIX)) {
				throw new MendmixBaseException("route path must startWith:" + GatewayConstants.PATH_PREFIX);
			}
			String[] parts = StringUtils.split(pathPattern, "/");
			module.setRouteName(parts[1]);
		}
	}
	
	private static void initModuleApiInfos(BizSystemModule module) {
		try {
			String url;
			AppMetadata appMetadata;
			if(GlobalRuntimeContext.APPID.equals(module.getRouteName())) {
				appMetadata = AppMetadataHolder.getMetadata();
			}else {	
				url = module.getMetadataUri();
				appMetadata = HttpRequestEntity.get(url).backendInternalCall().execute().toObject(AppMetadata.class);
			}
			for (ApiInfo api : appMetadata.getApis()) {
				module.addApiInfo(api);
			}
			moduleApiInfos.put(module.getServiceId(), module.getApiInfos());
			log.info(">>initModuleApiInfos success -> serviceId:{},nums:{}",module.getServiceId(),module.getApiInfos().size());
		} catch (Exception e) {
			boolean ignore = e instanceof ClassCastException;
			if(!ignore && e instanceof MendmixBaseException) {
				MendmixBaseException ex = (MendmixBaseException) e;
				ignore = ex.getCode() == 404 || ex.getCode() == 401 || ex.getCode() == 403;
			}
			if(ignore) {
				module.setApiInfos(new HashMap<>(0));
				moduleApiInfos.put(module.getServiceId(), module.getApiInfos());
			}else if(fetchApiMetaRound <= 1) {
				log.error(">>initModuleApiInfos error -> serviceId:["+module.getServiceId()+"]",e);
			}
		}
	}
			
}

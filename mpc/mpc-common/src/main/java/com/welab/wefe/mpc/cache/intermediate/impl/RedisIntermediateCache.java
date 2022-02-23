/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.mpc.cache.intermediate.impl;

import org.apache.commons.lang3.StringUtils;

import com.welab.wefe.mpc.cache.intermediate.CacheOperation;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisIntermediateCache implements CacheOperation {

	public static volatile JedisPool jedisPool = null;
	private String host;
	private int port;
	private String password;

	public RedisIntermediateCache(String host, int port, String password) {
		this.host = host;
		this.port = port;
		this.password = password;
	}

	public static JedisPool getJedisPoolInstance(String host, int port, String password) {
		if (null == jedisPool) {
			synchronized (RedisIntermediateCache.class) {
				if (null == jedisPool) {
					JedisPoolConfig poolConfig = new JedisPoolConfig();
					poolConfig.setMaxIdle(32);
					if (StringUtils.isNotBlank(password)) {
						jedisPool = new JedisPool(poolConfig, host, port, 2000, password);
					} else {
						jedisPool = new JedisPool(poolConfig, host, port);
					}
				}
			}
		}
		return jedisPool;
	}

	@Override
	public void save(String key, String name, Object value) {
		if (jedisPool == null) {
			jedisPool = getJedisPoolInstance(host, port, password);
		}
		// 从连接池中获取一个连接
		Jedis jedis = jedisPool.getResource();
		jedis.set(name + "_" + key, value);
		jedis.close();
	}

	@Override
	public Object get(String key, String name) {
		return null;
	}

	@Override
	public void delete(String key) {

	}

}

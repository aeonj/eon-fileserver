/**
 * @author aeon
 */
package eon.hg.fileserver.util.cache;

import cn.hutool.crypto.SecureUtil;
import eon.hg.fileserver.config.FileServerProperties;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

/**
 * The class AbstractCacheOperator.
 * 
 * @author aeon
 * @version dns1.0, Mar 2, 2013
 */
public abstract class AbstractCacheOperator implements CacheOperator,java.io.Serializable {

	@Autowired
	FileServerProperties propertiesBean;

	@Autowired
	RedisPool redisPool;

	@Autowired
	CachePool cachePool;

	public void reset(Object... params) {
		String cacheId = getCacheId(params);
		if (cachePool.containsKey(cacheId)) {
			cachePool.get(cacheId).clear();
		}
		if ("redis".equals(propertiesBean.getCacheType())) {
			if (redisPool.hasKey(cacheId)) {
				redisPool.del(cacheId);
			}
		}
	}

	public void removeOne(Object... params) {
		try {
			String cacheId = getCacheId(params);
			String key = getKey(params);
			key = SecureUtil.md5(key);
			if (cachePool.containsKey(cacheId)) {
				cachePool.get(cacheId).remove(key);
			}
			if ("redis".equals(propertiesBean.getCacheType())) {
				if (redisPool.hasKey(cacheId)) {
					redisPool.hdel(cacheId, key);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void removeLocal(Object... params) {
		try {
			String cacheId = getCacheId(params);
			if (cachePool.containsKey(cacheId)) {
				cachePool.remove(cacheId);
			}
			if ("redis".equals(propertiesBean.getCacheType())) {
				if (redisPool.hasKey(cacheId)) {
					redisPool.del(cacheId);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * 获取 cache id.
	 *
	 * @return the cacheId
	 */
	public abstract String getCacheId(Object... params);

	/*
	 * (non-Javadoc)
	 * 
	 * @see eon.hg.fap.core.cache.CacheOperator#getKey(java.lang.Object[])
	 */
	public abstract String getKey(Object... params) throws Exception;

	/*
	 * (non-Javadoc)
	 * 
	 * @see eon.hg.fap.core.cache.CacheOperator#getObject(java.lang.Object[])
	 */
	public abstract <T> T getObject(Object... params) throws Exception;

	/*
	 * (non-Javadoc)
	 *
	 * @see eon.hg.fap.core.cache.CacheOperator#getCache(java.lang.Object[])
	 */
	@Override
	public <T> T getCache(Object... params) throws Exception {
		if ("ehcache".equals(propertiesBean.getCacheType())) {
			return getPoolCache(params);
		}
		String cacheId = getCacheId(params);

		String key = getKey(params);
		key = SecureUtil.md5(key);
		boolean haskey;
		try {
			haskey = redisPool.hHasKey(cacheId, key);
		} catch (Exception e) {
			e.printStackTrace();
			return getObject(params);
		}
		if (haskey) {
			return redisPool.hget(cacheId, key);
		} else {
			T obj = getObject(params);
			redisPool.hset(cacheId, key, obj);
			return obj;
		}
	}

	@Override
    public <T> T getPoolCache(Object... params) throws Exception {
		String cacheId = getCacheId(params);
		if (!cachePool.containsKey(cacheId)) {
			cachePool.put(cacheId,new HashMap<>());
		}
		Map<String,Object> poolMap = cachePool.get(cacheId);

		String key = getKey(params);
		key = SecureUtil.md5(key);
		if (poolMap.containsKey(key)) {
			return (T) poolMap.get(key);
		} else {
			T obj = getObject(params);
			poolMap.put(key,obj);
			return obj;
		}
	}

	@Override
	public Map<String, Object> getPool(Object... params) {
		String cacheId = getCacheId(params);
		return cachePool.get(cacheId);
	}

}

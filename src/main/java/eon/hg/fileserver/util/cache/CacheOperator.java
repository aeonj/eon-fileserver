package eon.hg.fileserver.util.cache;

import java.util.Map;

/**
 * The Interface CacheOperator.
 * @author aeon
 */
public interface CacheOperator {

	<T> T getObject(Object... params) throws Exception;

	/**
	 * Gets the cache.
	 * 
	 * @param params
	 *            the params
	 * @return the cache
	 */
	<T> T getCache(Object... params) throws Exception;

    /**
     * Gets the local cache.
     * @param params
     * @param <T>
     * @return
     * @throws Exception
     */
	<T> T getPoolCache(Object... params) throws Exception;

	Map<String, Object> getPool(Object... params);

	/**
	 * Reset.
	 */
	void reset(Object... params);

	/**
	 * delete one item
	 * @param params
	 */
	void removeOne(Object... params);

	/**
	 * delete local session cache
	 * @param params
	 */
	void removeLocal(Object... params);

}

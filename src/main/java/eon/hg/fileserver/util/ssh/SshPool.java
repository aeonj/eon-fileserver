package eon.hg.fileserver.util.ssh;

import ch.ethz.ssh2.Connection;
import cn.hutool.core.util.StrUtil;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Ganymed连接池
 * 
 * @author looly
 *
 */
public enum SshPool {
	INSTANCE;

	/** SSH连接池，key：host，value：Connectionn对象 */
	private Map<String, Connection> connectPool = new ConcurrentHashMap<String, Connection>();
	/** 锁 */
	private static final Object lock = new Object();

	/**
	 * 获取Connection，不存在返回null
	 * 
	 * @param key 键
	 * @return Connection
	 */
	public Connection get(String key) {
		return connectPool.get(key);
	}

	/**
	 * 获得一个SSH跳板机连接，重用已经使用的连接
	 *
	 * @param sshHost 跳板机主机
	 * @param sshPort 跳板机端口
	 * @param sshUser 跳板机用户名
	 * @param sshPass 跳板机密码
	 * @return SSH连接
	 */
	public Connection getConnection(String sshHost, int sshPort, String sshUser, String sshPass) throws IOException {
		final String key = StrUtil.format("{}@{}:{}", sshUser, sshHost, sshPort);
		Connection connection = get(key);
		if (null == connection || false == connection.isAuthenticationComplete()) {
			synchronized (lock) {
				connection = get(key);
				if (null == connection || false == connection.isAuthenticationComplete()) {
					//指明连接主机的IP地址
					try {
						connection = new Connection(sshHost,sshPort);
						connection.connect();
						//使用用户名和密码校验
						boolean isconn = connection.authenticateWithPassword(sshUser, sshPass);
						if (isconn) {
							//使用用户名和密码校验
							put(key, connection);
						}
					} catch (IOException e) {
						throw e;
					}
				}
			}
		}
		return connection;
	}

	/**
	 * 加入Connection
	 * 
	 * @param key 键
	 * @param conn Connection
	 */
	public void put(String key, Connection conn) {
		this.connectPool.put(key, conn);
	}

	/**
	 * 关闭SSH连接连接
	 * 
	 * @param key 主机，格式为user@host:port
	 */
	public void close(String key) {
		Connection conn = connectPool.get(key);
		if (conn != null) {
			conn.close();
		}
		connectPool.remove(key);
	}
	
	/**
	 * 移除指定Connection
	 * 
	 * @param connection Connection连接
	 * @since 4.1.15
	 */
	public void remove(Connection connection) {
		if(null != connection) {
			final Iterator<Entry<String, Connection>> iterator = this.connectPool.entrySet().iterator();
			Entry<String, Connection> entry;
			while(iterator.hasNext()) {
				entry = iterator.next();
				if(connection.equals(entry.getValue())) {
					iterator.remove();
					break;
				}
			}
		}
	}

	/**
	 * 关闭所有SSH连接连接
	 */
	public void closeAll() {
		Collection<Connection> connections = connectPool.values();
		for (Connection conn : connections) {
			conn.close();
		}
		connectPool.clear();
	}
}

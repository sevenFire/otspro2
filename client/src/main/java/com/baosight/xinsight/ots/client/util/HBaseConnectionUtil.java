/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.baosight.xinsight.ots.client.util;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Table;

import java.io.IOException;

/**
 * Singleton class encapsulating global REST servlet state and functions.
 */
public class HBaseConnectionUtil {
	private static HBaseConnectionUtil INSTANCE;
	private final Configuration conf;
	private final Connection connection;  

	
	/**
	 * 返回单例对象
	 *@return the RESTServlet singleton instance
	 */
	public synchronized static HBaseConnectionUtil getInstance() {
		assert(INSTANCE != null);
		return INSTANCE;
	}

	/**
	 * 初始化，单例
	* @param conf Existing configuration to use in rest servlet
	* @return the RESTServlet singleton instance
	* @throws IOException
	*/
	public synchronized static HBaseConnectionUtil init(Configuration conf) throws IOException {
		if (INSTANCE == null) {
			INSTANCE = new HBaseConnectionUtil(conf);
		}
		return INSTANCE;
	}

	/**
	 * 私有构造类
	 * Constructor with existing configuration
	 * @param conf existing configuration
	 * @throws IOException
	 */
	private HBaseConnectionUtil(final Configuration conf) throws IOException {
		this.conf = conf;
		connection = ConnectionFactory.createConnection(conf);
	}

	/**
	 * 关闭连接
	 */
	public synchronized void stop() {
		if (INSTANCE != null)  {
			try {
				connection.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			INSTANCE = null;
		}
	}

	public Connection getConnection() throws IOException {
		return connection;
	}

	public Admin getAdmin() throws IOException {
		return connection.getAdmin();
	}
	
	/**
	* Caller closes the table afterwards.
	*/
	public Table getTable(String tableName) throws IOException {
		return connection.getTable(TableName.valueOf(tableName));
	}

	public Table getTable(TableName tableName) throws IOException {
		return connection.getTable(tableName);
	}

	public Configuration getConfiguration() {
		return conf;
	}
}

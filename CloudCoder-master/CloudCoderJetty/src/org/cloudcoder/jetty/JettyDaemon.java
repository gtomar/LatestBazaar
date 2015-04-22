// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2013, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2013, David H. Hovemeyer <david.hovemeyer@gmail.com>
// Copyright (C) 2013, York College of Pennsylvania
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

package org.cloudcoder.jetty;

import org.apache.log4j.PropertyConfigurator;
import org.cloudcoder.daemon.IDaemon;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link IDaemon} for starting and shutting down
 * an embedded Jetty server.
 * 
 * @author David Hovemeyer
 *
 * @param <ConfigType> the exact type of the configuration object, which
 *                     must implement {@link JettyDaemonConfig} either directly
 *                     or indirectly
 */
public abstract class JettyDaemon<ConfigType extends JettyDaemonConfig> implements IDaemon {

	private static final Logger logger = LoggerFactory.getLogger(JettyWebappDaemon.class);

	private Server server;

	/**
	 * Constructor.
	 */
	public JettyDaemon() {
		super();
	}

	/**
	 * Downcall method to get the {@link JettyDaemonConfig} object describing
	 * how Jetty should be configured.
	 * 
	 * @return the {@link JettyDaemonConfig} object describing
	 *         how Jetty should be configured
	 */
	protected abstract ConfigType getJettyConfig();
	
	/**
	 * Downcall method to configure the Jetty {@link Server} object.
	 * 
	 * @param server the server object
	 * @param config the configuration object
	 */
	protected abstract void onCreateServer(Server server, ConfigType config);

	@Override
	public void start(String instanceName) {
		ConfigType jettyConfig = getJettyConfig();
		
		// Configure logging
		PropertyConfigurator.configure(jettyConfig.getLog4jProperties());
		
		// Create an embedded Jetty server
		this.server = new Server();
		
		// Create a connector
		SelectChannelConnector connector = new SelectChannelConnector();
		connector.setPort(jettyConfig.getPort());
		if (jettyConfig.isLocalhostOnly()) {
		    //System.out.println("happening?");
			connector.setHost("localhost");
		}
		server.addConnector(connector);

		// Configure the Server by setting up handlers
		onCreateServer(server, jettyConfig);
	
		// Other misc. options
		int numThreads = jettyConfig.getNumThreads();
		logger.info("Creating thread pool with {} threads", numThreads);
		server.setThreadPool(new QueuedThreadPool(numThreads));
	
		// And start it up
		logger.info("Starting up the server...");
		try {
			server.start();
		} catch (Exception e) {
			logger.error("Could not start server", e);
		}
	}

	@Override
	public void shutdown() {
		try {
			logger.info("Stopping the server...");
			server.stop();
			logger.info("Waiting for server to finish...");
			server.join();
			logger.info("Server is finished");
		} catch (Exception e) {
			logger.error("Exception shutting down Jetty server", e);
		}
	}

}
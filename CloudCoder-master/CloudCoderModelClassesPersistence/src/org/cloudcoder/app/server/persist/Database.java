// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2012, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2012, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

package org.cloudcoder.app.server.persist;

import java.sql.SQLException;

/**
 * Factory to provide a default implementation of IDatabase.
 * 
 * @author David Hovemeyer
 */
public class Database {
	private static IDatabase instance;
	static {
		try {
			instance = new JDBCDatabase();
		} catch (SQLException e) {
			throw new IllegalStateException("Could not create JDBCDatabase object", e);
		}
	}
	
	/**
	 * Get the default implementation of IDatabase.
	 * 
	 * @return the default implementation of IDatabase
	 */
	public static IDatabase getInstance() {
		return instance;
	}
}

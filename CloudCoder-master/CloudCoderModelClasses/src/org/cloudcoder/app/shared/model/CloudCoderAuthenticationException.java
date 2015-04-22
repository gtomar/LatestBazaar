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

package org.cloudcoder.app.shared.model;

/**
 * Exception type to indicate than an RPC failed because
 * the client was not authenticated.
 */
public class CloudCoderAuthenticationException extends Exception {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor (no message).
	 */
	public CloudCoderAuthenticationException() {
		
	}
	
	/**
	 * Constructor.
	 * 
	 * @param msg message describing the exception
	 */
	public CloudCoderAuthenticationException(String msg) {
		super(msg);
	}

}

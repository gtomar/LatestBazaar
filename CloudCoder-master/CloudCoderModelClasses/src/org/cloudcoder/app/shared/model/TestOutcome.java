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
 * Enumeration representing the outcome of a test case.
 * 
 * @author Jaime Spacco
 * @author David Hovemeyer
 */
public enum TestOutcome {
    PASSED("Test passed", false),
    FAILED_ASSERTION("Test failed", false),
    FAILED_WITH_EXCEPTION("Exception", true),
    FAILED_FROM_TIMEOUT("Timeed out", false),
    FAILED_BY_SECURITY_MANAGER("Security exception", true),
    INTERNAL_ERROR("Internal error", true),;
    
    private final String shortMessage;
    private final boolean displayProcessStatus;
    
	private TestOutcome(String shortMessage, boolean displayProcessStatus) {
		this.shortMessage = shortMessage;
		this.displayProcessStatus = displayProcessStatus;
	}

	/**
	 * @return the shortMessage
	 */
	public String getShortMessage() {
		return shortMessage;
	}

	/**
	 * @return true if TestResults with this outcome should display extra
	 *         information about the process status (how it terminated)
	 */
	public boolean isDisplayProcessStatus() {
		return displayProcessStatus;
	}
}

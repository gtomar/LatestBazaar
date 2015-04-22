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

package org.cloudcoder.app.loadtester;

import java.util.Scanner;

import org.cloudcoder.app.shared.model.CloudCoderAuthenticationException;
import org.cloudcoder.app.shared.model.CourseAndCourseRegistration;
import org.cloudcoder.app.shared.model.CourseRegistrationType;
import org.cloudcoder.app.shared.model.User;

/**
 * Create test user accounts for load testing.
 * 
 * @author David Hovemeyer
 */
public class CreateTestUsers {
	/**
	 * Interactively create test user accounts for load testing.
	 * User will be prompted to enter username, password (for an instructor
	 * account), course name, and minumum and maximum test user
	 * numbers.  Each account will be named "userN", where N is
	 * an integer in the specified range, and the password for
	 * each account will be the same as the username.
	 * 
	 * @param hostConfig the {@link HostConfig} specifying how to connect to the webapp
	 * @throws CloudCoderAuthenticationException
	 */
	public static void createTestUserAccounts(HostConfig hostConfig)
			throws CloudCoderAuthenticationException {
		@SuppressWarnings("resource")
		Scanner keyboard = new Scanner(System.in);
		
		Client client = new Client(hostConfig);
		
		// Must be logged in using an instructor account
		System.out.print("Instructor username: ");
		String userName = keyboard.nextLine();
		System.out.print("Instructor password: ");
		String password = keyboard.nextLine();
		
		client.login(userName, password);
		
		System.out.print("Course name: ");
		String courseName = keyboard.nextLine();

		// Find the course and make sure user is really an instructor
		CourseAndCourseRegistration theCCR = null;
		
		CourseAndCourseRegistration[] courses = client.getRegisteredCourses();
		for (CourseAndCourseRegistration ccr : courses) {
			if (ccr.getCourse().getName().equals(courseName)) {
				theCCR = ccr;
				break;
			}
		}
		
		if (theCCR == null) {
			System.out.println("Could not find course " + courseName);
			System.exit(1);
		}
		if (!theCCR.getCourseRegistration().getRegistrationType().isInstructor()) {
			System.out.println("User is not an instructor");
			System.exit(1);
		}
		
		System.out.print("Start user number: ");
		int startUserNum = keyboard.nextInt();
		System.out.print("End user number: ");
		int endUserNum = keyboard.nextInt();
		
		for (int n = startUserNum; n <= endUserNum; n++) {
			User user = new User();
			String testUserName = "user" + n;
			user.setUsername(testUserName);
			user.setFirstname("Test");
			user.setLastname("User");
			user.setPasswordHash(testUserName); // set to plaintext when adding/registering a user
			user.setEmail(testUserName + "@unseen.edu");
			user.setConsent("");
			user.setWebsite("http://student.unseen.edu/~" + testUserName);
			
			System.out.println("Adding user " + testUserName);
			client.createUser(user, theCCR.getCourse(), CourseRegistrationType.STUDENT, 101);
		}
	}
}

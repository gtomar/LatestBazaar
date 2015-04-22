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

package org.cloudcoder.app.client.rpc;

import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.CourseRegistration;
import org.cloudcoder.app.shared.model.CourseRegistrationList;
import org.cloudcoder.app.shared.model.CourseRegistrationType;
import org.cloudcoder.app.shared.model.CloudCoderAuthenticationException;
import org.cloudcoder.app.shared.model.EditedUser;
import org.cloudcoder.app.shared.model.User;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;


/**
 * @author jspacco
 *
 */
@RemoteServiceRelativePath("user")
public interface UserService extends RemoteService
{
    /**
     * 
     * Get a list of all {@link User} objects for each of
     * the users registered for the given Course.
     * 
     * @param courseId The course for which we want the users.
     * @param sectionNumber the section number of the course (0 to get all sections)
     * @return An array of all of the Users registered for the given course.
     * @throws CloudCoderAuthenticationException if the client is not authenticated
     */
    User[] getUsers(int courseId, int sectionNumber) throws CloudCoderAuthenticationException;
    
    /**
     * Add a user to a course.
     * 
     * @param editedUser the {@link EditedUser} object containing the user information
     * @param courseId   the id of the {@link Course} in which the user should be added
     * @return true if successful
     * @throws CloudCoderAuthenticationException if the authenticated user is not an instructor in the course
     */
    Boolean addUserToCourse(EditedUser editedUser, int courseId) throws CloudCoderAuthenticationException;
    
    /**
     * Edit the fields of the {@link User} record with the new values.
     * Use the id field in the given record to locate the record to update.
     * 
     * @param user
     * @return
     */
    Boolean editUser(User user) throws CloudCoderAuthenticationException;
    
    /**
     * Update the user and course registration information specified
     * by given {@link EditedUser} object.  The authenticated user must
     * be an instructor in the course.
     * 
     * @param editedUser  the {@link EditedUser}
     * @param course      the {@link Course}
     * @return true if successful, false otherwise
     * @throws CloudCoderAuthenticationException
     */
    Boolean editUser(EditedUser editedUser, Course course) throws CloudCoderAuthenticationException;
    
    
    void editCourseRegistrationType(int userId, int courseId, CourseRegistrationType type) throws CloudCoderAuthenticationException;
    
    /**
     * Get the {@link CourseRegistrationList} with all of the given {@link User}'s
     * {@link CourseRegistration}s for given {@link Course}.
     * The authenticated user must be an instructor in the course.
     * 
     * @param course the {@link Course}
     * @param user   the {@link User}
     * @return the {@link CourseRegistrationList}, or null if the authenticated user
     *         is not an instructor in the course
     */
    CourseRegistrationList getUserCourseRegistrationList(Course course, User user) throws CloudCoderAuthenticationException;
}

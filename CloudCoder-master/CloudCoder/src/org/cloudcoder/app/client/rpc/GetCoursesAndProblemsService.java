// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2014, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2014, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

import org.cloudcoder.app.shared.dto.ShareExercisesResult;
import org.cloudcoder.app.shared.model.CloudCoderAuthenticationException;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.CourseAndCourseRegistration;
import org.cloudcoder.app.shared.model.Module;
import org.cloudcoder.app.shared.model.NamedTestResult;
import org.cloudcoder.app.shared.model.OperationResult;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemAndSubmissionReceipt;
import org.cloudcoder.app.shared.model.ProblemAndTestCaseList;
import org.cloudcoder.app.shared.model.Quiz;
import org.cloudcoder.app.shared.model.SubmissionReceipt;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.User;
import org.cloudcoder.app.shared.model.UserAndSubmissionReceipt;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * RPC service for getting {@link Course}s and {@link Problem}s
 * for the currently-authenticated client.
 */
@RemoteServiceRelativePath("getCoursesAndProblems")
public interface GetCoursesAndProblemsService extends RemoteService {
	/**
	 * Get the courses the client user is registered for.
	 * 
	 * @return list of courses the client user is registered for
	 * @throws CloudCoderAuthenticationException if the client is not authenticated
	 */
	public Course[] getCourses() throws CloudCoderAuthenticationException;
	
	/**
	 * Get {@link CourseAndCourseRegistration} objects representing
	 * all of the courses the client is registered for, along with
	 * registration information (such as instructor status) for each
	 * course.
	 * 
	 * @return list of {@link CourseAndCourseRegistration} objects for client
	 * @throws CloudCoderAuthenticationException if the client is not authenticated
	 */
	public CourseAndCourseRegistration[] getCourseAndCourseRegistrations() throws CloudCoderAuthenticationException;
	
	/**
	 * Get {@link Problem}s available in given {@link Course}.
	 * The client must be registered in the Course.
	 * 
	 * @param course the Course
	 * @return the Problems available in the course
	 * @throws CloudCoderAuthenticationException if the client is not authenticated,
	 *         or is not regsitered in the course 
	 */
	public Problem[] getProblems(Course course) throws CloudCoderAuthenticationException;
	
	/**
	 * Get {@link ProblemAndSubmissionReceipt}s for given {@link Course} and {@link Module}.
	 * This allows the client to get not only the Problems in the Course,
	 * but also the most recent {@link SubmissionReceipt} for each Problem.
	 * The client must be registered in the Course.
	 * 
	 * @param course the Course
	 * @param forUser the {@link User} for whom to receive {@link ProblemAndSubmissionReceipt}s
	 * @param module the Module: if null, then all problems and submission receipts in course are returned
	 * @return the ProblemAndSubmissionReceipts for the client's work in the Course
	 * @throws CloudCoderAuthenticationException if the client is not authenticated,
	 *         or is not regsitered in the course 
	 */
	public ProblemAndSubmissionReceipt[] getProblemAndSubscriptionReceipts(Course course, User forUser, Module module) throws CloudCoderAuthenticationException;
	
	/**
	 * Get the best submission receipts for each {@link User} on a specific {@link Problem}.
	 * Currently-authenticated user must be an instructor in the course.
	 * 
	 * @param problem the {@link Problem}
	 * @param section the section (0 for all sections)
	 * @return the {@link UserAndSubmissionReceipt}s with best submission receipt for each user
	 * @throws CloudCoderAuthenticationException
	 */
	public UserAndSubmissionReceipt[] getBestSubmissionReceipts(Problem problem, int section) throws CloudCoderAuthenticationException;
	
	/**
	 * Get the list of {@link TestCase}s for a {@link Problem}.
	 * This will only succeed if the authenticated user is an instructor in the course
	 * the problem is assigned in.
	 * 
	 * @param problemId the id of the Problem
	 * @return the list of TestCases for the Problem
	 */
	public TestCase[] getTestCasesForProblem(int problemId) throws CloudCoderAuthenticationException;
	
	/**
	 * Get all of the non-secret {@link TestCase}s for a {@link Problem}.
	 * The authenticated user must be registered in the course in which
	 * the problem is assigned.
	 * 
	 * @param problemId the id of the Problem
	 * @return the list of non-secret TestCases for the Problem
	 * @throws CloudCoderAuthenticationException
	 */
	public TestCase[] getNonSecretTestCasesForProblem(int problemId) throws CloudCoderAuthenticationException;
	
	/**
	 * Get list of names of the {@link TestCase}s for given {@link Problem}.
	 * No instructor privilege is required.
	 * 
	 * @param problemId the unique id of the {@link Problem}
	 * @return list of test case names
	 * @throws CloudCoderAuthenticationException
	 */
	public String[] getTestCaseNamesForProblem(int problemId) throws CloudCoderAuthenticationException;
	
	/**
	 * Store given {@link ProblemAndTestCaseList} in the database.
	 * The authenticated user must be registered as an instructor for the course
	 * in which the problem is assigned.
	 * If the problem already has a valid problem id, it will be updated.
	 * If the problem does not yet have a valid problem id, it will be inserted.
	 * 
	 * @param problemAndTestCaseList the ProblemAndTestCaseList to store
	 * @return the updated ProblemAndTestCaseList (which may have had ids assigned, etc.)
	 */
	public ProblemAndTestCaseList storeProblemAndTestCaseList(
			ProblemAndTestCaseList problemAndTestCaseList,
			Course course) throws CloudCoderAuthenticationException;
	
	/**
	 * Submit an exercise (problem and testcases) to the exercise repository.
	 * 
	 * @param exercise the exercise to submit
	 * @param repoUsername the repository username
	 * @param repoPassword the repository password
	 */
	public OperationResult submitExercise(ProblemAndTestCaseList exercise, String repoUsername, String repoPassword)
		throws CloudCoderAuthenticationException;
	
	/**
	 * Submit an array of problems to the exercise repository.
	 * This method uploads the problems to the server, which is then responsible
	 * for lookup up the corresponding test cases, JSONifying the results,
	 * and sending the JSON to the repository.
	 * 
	 * @param problems
	 * @param repoUsername
	 * @param repoPassword
	 * @throws CloudCoderAuthenticationException
	 */
	public ShareExercisesResult submitExercises(Problem[] problems, String repoUsername,
	        String repoPassword) throws CloudCoderAuthenticationException;
	
	/**
	 * Import an exercise (problem and testcases) from the exercise repository.
	 * The currently-authenticated user must be an instructor in the course.
	 * 
	 * @param course the {@link Course} to import the exercise into
	 * @param exerciseHash the hash of the execise to import
	 * @return the exercise, or null if no such exercise could be found in the repository
	 */
	public ProblemAndTestCaseList importExercise(Course course, String exerciseHash) throws CloudCoderAuthenticationException;
	
	/**
	 * Delete a problem (and its test cases) from the local database.
	 * The currently-authenticated user must be an instructor in the course.
	 */
	public OperationResult deleteProblem(Course course, Problem problem) throws CloudCoderAuthenticationException;
	
	/**
	 * Start a quiz.
	 * 
	 * @param problem  the {@link Problem} to administer as a quiz
	 * @param section  the section in which to administer the quiz
	 * @return the {@link Quiz}
	 * @throws CloudCoderAuthenticationException if the current user is not authorized
	 *         to give a quiz for the {@link Problem}/section
	 */
	public Quiz startQuiz(Problem problem, int section) throws CloudCoderAuthenticationException;
	
	/**
	 * Find out if there is a current (ongoing) {@link Quiz} for the
	 * given {@link Problem} in a section for which the currently-authenticated
	 * user is an instructor.  Note that there is an implicit assumption
	 * that instructors of multiple sections will give a quiz in at most
	 * one section at a time.
	 * 
	 * Note that the if a non-null value is returned, the end time field
	 * of the returned quiz will contain the current server-side time: this
	 * can be used to compute how long the quiz has been ongoing. 
	 * 
	 * @param problem the {@link Problem}
	 * @return the current (ongoing) {@link Quiz}, or null if there is no
	 *         quiz, or if the current user is not authorized to administer
	 *         quizzes in the course
	 * @throws CloudCoderAuthenticationException if there is no authenticated user in
	 *         the server-side session
	 */
	public Quiz findCurrentQuiz(Problem problem) throws CloudCoderAuthenticationException;
	
	/**
	 * End given {@link Quiz}.
	 * 
	 * @param quiz the {@link Quiz} to end
	 * @return true if successful, false if not (for example, if there is no such quiz)
	 * @throws CloudCoderAuthenticationException if there is no authenticated user in
	 *         the server-side session 
	 */
	public Boolean endQuiz(Quiz quiz) throws CloudCoderAuthenticationException;
	
	/**
	 * Get all {@link Module}s used to tag the {@link Problem}s in given {@link Course}.
	 * 
	 * @param course the course
	 * @return all of the modules in the course
	 * @throws CloudCoderAuthenticationException 
	 */
	public Module[] getModulesForCourse(Course course) throws CloudCoderAuthenticationException;

	/**
	 * Set the {@link Module} in which a {@link Problem} is categorized.
	 * If no module currently exists with the given name, a new one is
	 * created.  The currently-authenticated user must be an instructor
	 * in the course in which the problem is assigned.
	 * 
	 * @param problem     the {@link Problem}
	 * @param moduleName  the new module name
	 * @return the {@link Module} in which the {@link Problem} is now categorized
	 * @throws CloudCoderAuthenticationException
	 */
	public Module setModule(Problem problem, String moduleName) throws CloudCoderAuthenticationException;

	/**
	 * Get all section numbers for the given {@link Course}.
	 * 
	 * @param course the course
	 * @return array containing all of the section numbers for the course
	 * @throws CloudCoderAuthenticationException
	 */
	public Integer[] getSectionsForCourse(Course course) throws CloudCoderAuthenticationException;
	
	/**
	 * Get all submission receipts for given user on given problem.
	 * Currently-authenticated user must be an instructor.
	 * 
	 * @param problem the {@link Problem}
	 * @param user    the {@link User}
	 * @return list of {@link SubmissionReceipt}s
	 * @throws CloudCoderAuthenticationException
	 */
	public SubmissionReceipt[] getAllSubmissionReceiptsForUser(Problem problem, User user) throws CloudCoderAuthenticationException;
	
	/**
	 * Get all test results for given submission.
	 * Authenticated user must either be the user specified in the
	 * submission receipt, or an instructor in the course.
	 * 
	 * @param problem the problem
	 * @param receipt the submission receipt
	 * @return the test results
	 * @throws CloudCoderAuthenticationException 
	 */
	public NamedTestResult[] getTestResultsForSubmission(Problem problem, SubmissionReceipt receipt) throws CloudCoderAuthenticationException;

	/**
	 * Import all courses from given source {@link Course} into the
	 * given destination Course.  This method starts the operation:
	 * the client is expected to use {@link #checkImportAllProblemsFromCourse()}
	 * to poll for the completion of the operation.
	 * 
	 * @param source the source {@link Course}
	 * @param dest   the destination {@link Course}
	 * @throws CloudCoderAuthenticationException if the user is not logged in, or if the user is not
	 *                                           an instructor in both courses
	 */
	public void startImportAllProblemsFromCourse(Course source, Course dest) throws CloudCoderAuthenticationException;

	/**
	 * Check for the result of importing all problems from a source
	 * {@link Course} to a destination Course, started by a previous
	 * call to {@link #startImportAllProblemsFromCourse(Course, Course)}.
	 * 
	 * @return the {@link OperationResult} indicating the success/failure of the operation,
	 *         or null if the operation hasn't completed yet
	 */
	public OperationResult checkImportAllProblemsFromCourse();
	
	/**
	 * Update the "when assigned" and "when due" dates/times for
	 * given {@link Problem}s.
	 * 
	 * @param problems list of {@link Problem}s whose dates/times should be changed
	 * @return {@link OperationResult} indicating the success or failure
	 *         of the operation
	 */
	public OperationResult updateProblemDates(Problem[] problems) throws CloudCoderAuthenticationException;
}

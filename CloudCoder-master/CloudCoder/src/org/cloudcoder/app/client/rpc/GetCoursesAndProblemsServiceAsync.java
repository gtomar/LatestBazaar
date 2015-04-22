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

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface GetCoursesAndProblemsServiceAsync {

	void getCourses(AsyncCallback<Course[]> callback);

	void getCourseAndCourseRegistrations(
			AsyncCallback<CourseAndCourseRegistration[]> callback);

	void getProblems(Course course, AsyncCallback<Problem[]> callback);

	void getProblemAndSubscriptionReceipts(Course course, User forUser, Module module,
			AsyncCallback<ProblemAndSubmissionReceipt[]> callback);

	void getBestSubmissionReceipts(Problem problem, int section,
			AsyncCallback<UserAndSubmissionReceipt[]> callback);
	
	void getTestCasesForProblem(int problemId,
			AsyncCallback<TestCase[]> callback);

	void getTestCaseNamesForProblem(int problemId,
			AsyncCallback<String[]> callback);

	void getNonSecretTestCasesForProblem(int problemId,
			AsyncCallback<TestCase[]> callback);

	void storeProblemAndTestCaseList(
			ProblemAndTestCaseList problemAndTestCaseList, Course course,
			AsyncCallback<ProblemAndTestCaseList> callback);

	void submitExercise(ProblemAndTestCaseList exercise, String repoUsername,
			String repoPassword, AsyncCallback<OperationResult> callback);

	void importExercise(Course course, String exerciseHash,
			AsyncCallback<ProblemAndTestCaseList> callback);

	void deleteProblem(Course course, Problem problem,
			AsyncCallback<OperationResult> callback);

	void startQuiz(Problem problem, int section, AsyncCallback<Quiz> callback);

	void findCurrentQuiz(Problem problem, AsyncCallback<Quiz> callback);

	void endQuiz(Quiz quiz, AsyncCallback<Boolean> callback);

	void getModulesForCourse(Course course, AsyncCallback<Module[]> callback);

	void setModule(Problem problem, String moduleName,
			AsyncCallback<Module> callback);

	void getSectionsForCourse(Course course, AsyncCallback<Integer[]> callback);

	void getAllSubmissionReceiptsForUser(Problem problem, User user,
			AsyncCallback<SubmissionReceipt[]> callback);

	void getTestResultsForSubmission(Problem problem, SubmissionReceipt receipt,
			AsyncCallback<NamedTestResult[]> callback);

    void submitExercises(Problem[] problems, String repoUsername,
        String repoPassword, AsyncCallback<ShareExercisesResult> asyncCallback);

	void startImportAllProblemsFromCourse(Course destinationCourse,
			Course sourceCourse, AsyncCallback<Void> callback);

	void checkImportAllProblemsFromCourse(
			AsyncCallback<OperationResult> callback);

	void updateProblemDates(Problem[] problems,
			AsyncCallback<OperationResult> callback);

}

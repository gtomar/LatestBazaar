// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2013, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2013, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

package org.cloudcoder.app.client.view;

import java.util.Arrays;

import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.model.StatusMessage;
import org.cloudcoder.app.client.page.SessionObserver;
import org.cloudcoder.app.client.rpc.RPC;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.CourseSelection;
import org.cloudcoder.app.shared.model.ICallback;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemAndSubmissionReceipt;
import org.cloudcoder.app.shared.model.SubmissionReceipt;
import org.cloudcoder.app.shared.model.SubmissionStatus;
import org.cloudcoder.app.shared.model.User;
import org.cloudcoder.app.shared.model.UserSelection;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;

/**
 * View for showing the progress of a single {@link User} (student)
 * in a  {@link Course} by showing the student's progress on
 * every {@link Problem} assigned in the course. 
 * 
 * @author David Hovemeyer
 */
public class UserProgressView extends Composite implements SessionObserver {
	private CourseSelection courseSelection;
	private UserSelection userSelection;
	private ProblemAndSubmissionReceipt[] data;
	private DataGrid<ProblemAndSubmissionReceipt> grid;
	private ICallback<Problem> viewSubmissionsCallback;
	
	public UserProgressView() {
		data = new ProblemAndSubmissionReceipt[0];
		grid = new DataGrid<ProblemAndSubmissionReceipt>();
		
		grid.addColumn(new ProblemNameColumn(), "Problem");
		grid.addColumn(new StartedColumn(), "Started");
		grid.addColumn(new BestScoreColumn(), "Best score");
		grid.addColumn(new BestScoreBarColumn(), "Best score bar");
		grid.addColumn(new ViewSubmissionsColumn(), "Submissions");
		
		initWidget(grid);
	}
	
	/**
	 * Set callback to invoke when the view submissions button is clicked.
	 * Callback argument is the {@link Problem}.
	 * 
	 * @param viewSubmissionsCallback the viewSubmissionsCallback to set
	 */
	public void setViewSubmissionsCallback(
			ICallback<Problem> viewSubmissionsCallback) {
		this.viewSubmissionsCallback = viewSubmissionsCallback;
	}
	
	private class ProblemNameColumn extends TextColumn<ProblemAndSubmissionReceipt> {
		@Override
		public String getValue(ProblemAndSubmissionReceipt object) {
			return object.getProblem().getTestname();
		}
	}
	
	private class StartedColumn extends TextColumn<ProblemAndSubmissionReceipt> {
		@Override
		public String getValue(ProblemAndSubmissionReceipt object) {
			SubmissionReceipt receipt = object.getReceipt();
			return (receipt != null && receipt.getStatus() != SubmissionStatus.NOT_STARTED) ? "true" : "false";
		}
	}
	
	private class BestScoreColumn extends TextColumn<ProblemAndSubmissionReceipt> {
		@Override
		public String getValue(ProblemAndSubmissionReceipt object) {
			SubmissionReceipt receipt = object.getReceipt();
			if (receipt == null) {
				return "0";
			} else {
				return receipt.getNumTestsPassed() + "/" + receipt.getNumTestsAttempted();
			}
		}
	}
	
	private class BestScoreBarColumn extends Column<ProblemAndSubmissionReceipt, ProblemAndSubmissionReceipt> {
		public BestScoreBarColumn() {
			super(new BestScoreBarCell<ProblemAndSubmissionReceipt>());
		}
		
		@Override
		public ProblemAndSubmissionReceipt getValue(ProblemAndSubmissionReceipt object) {
			return object;
		}
	}
	
	private class ViewSubmissionsColumn extends Column<ProblemAndSubmissionReceipt, String> {
		public ViewSubmissionsColumn() {
			super(new ButtonCell());
			
			setFieldUpdater(new FieldUpdater<ProblemAndSubmissionReceipt, String>() {
				@Override
				public void update(int index, ProblemAndSubmissionReceipt object, String value) {
					if (viewSubmissionsCallback != null) {
						viewSubmissionsCallback.call(object.getProblem());
					}
				}
			});
		}
		
		@Override
		public String getValue(ProblemAndSubmissionReceipt object) {
			return "View";
		}
	}
	
	@Override
	public void activate(final Session session, final SubscriptionRegistrar subscriptionRegistrar) {
		courseSelection = session.get(CourseSelection.class);
		userSelection = session.get(UserSelection.class);
		
		session.add(StatusMessage.pending("Loading data for " + userSelection.getUser().getUsername() + "..."));
		
		// Load data
		RPC.getCoursesAndProblemsService.getProblemAndSubscriptionReceipts(courseSelection.getCourse(), userSelection.getUser(), null, new AsyncCallback<ProblemAndSubmissionReceipt[]>() {
			@Override
			public void onSuccess(ProblemAndSubmissionReceipt[] result) {
				data = result;
				displayData();
				session.add(StatusMessage.goodNews("Successfully loaded data for " + userSelection.getUser().getUsername()));
			}

			@Override
			public void onFailure(Throwable caught) {
				session.add(StatusMessage.error("Could not load data for " + userSelection.getUser().getUsername()));
			}
		});
	}

	protected void displayData() {
		grid.setRowCount(data.length);
		grid.setRowData(Arrays.asList(data));
	}
}

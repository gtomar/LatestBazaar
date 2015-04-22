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

package org.cloudcoder.app.client.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.model.TestResultUtil;
import org.cloudcoder.app.client.page.SessionObserver;
import org.cloudcoder.app.shared.model.NamedTestResult;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemType;
import org.cloudcoder.app.shared.model.TestOutcome;
import org.cloudcoder.app.shared.util.Publisher;
import org.cloudcoder.app.shared.util.Subscriber;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.ResizeComposite;

/**
 * View displaying the list of {@link NamedTestResult}s.
 * 
 * @author David Hovemeyer
 */
public class TestResultListView extends ResizeComposite implements SessionObserver, Subscriber, IResultsTabPanelWidget {
	private DataGrid<NamedTestResult> cellTable;
	
	public TestResultListView() {
		cellTable = new DataGrid<NamedTestResult>();
		
		initWidget(cellTable);
	}
	
	private static class TestCaseNameColumn extends TextColumn<NamedTestResult> {
		@Override
		public String getValue(NamedTestResult object) {
			return object.getTestCaseName();
		}
	}
	
	private static class OutcomeColumn extends TextColumn<NamedTestResult> {
		@Override
		public String getValue(NamedTestResult object) {
			return object.getTestResult().getOutcome().toString().toLowerCase();
		}
		
		@Override
		public String getCellStyleNames(Context context, NamedTestResult object) {
			if (object.getTestResult().getOutcome() == TestOutcome.PASSED) {
				return "cc-passedTest";
			} else {
				return "cc-failedTest";
			}
		}
	}
	
	private static class InputColumn extends TextColumn<NamedTestResult> {
	    @Override
	    public String getValue(NamedTestResult object) {
	    	return TestResultUtil.getInput(object.getTestResult());
	    }
	}
	
	private static class ActualOutputColumn extends TextColumn<NamedTestResult> {
        @Override
        public String getValue(NamedTestResult object) {
            TestOutcome outcome=object.getTestResult().getOutcome();
//            if (outcome==TestOutcome.PASSED) {
//                return "";
//            }
            if (outcome==TestOutcome.FAILED_FROM_TIMEOUT) {
                return TestOutcome.FAILED_FROM_TIMEOUT.toString();
            }
            if (outcome==TestOutcome.FAILED_WITH_EXCEPTION) {
            	String actualOutput = TestResultUtil.getActualOutput(object.getTestResult());
                if (actualOutput!=null && !actualOutput.equals("")) {
                    return actualOutput;
                }
                return TestOutcome.FAILED_WITH_EXCEPTION.toString();
            }
            return TestResultUtil.getActualOutput(object.getTestResult());
        }
    }
	
	private static class ExpectedOutputColumn extends TextColumn<NamedTestResult> {
        @Override
        public String getValue(NamedTestResult object) {
        	return TestResultUtil.getExpectedOutput(object.getTestResult());
        }
    }
	
	private static class MessageColumn extends TextColumn<NamedTestResult> {
		@Override
		public String getValue(NamedTestResult object) {
			return object.getTestResult().getMessage();
		}
	}
	
	private static class OutputColumn extends Column<NamedTestResult, NamedTestResult> {
		public OutputColumn(ExtractOutputText<NamedTestResult> extractor) {
			super(new CompositeCell<NamedTestResult>(getCells(extractor)));
		}
		
		private static List<HasCell<NamedTestResult, ?>> getCells(final ExtractOutputText<NamedTestResult> extractor) {
			List<HasCell<NamedTestResult, ?>> result = new ArrayList<HasCell<NamedTestResult, ?>>();
			result.add(new ShowFullOutputButtonColumn<NamedTestResult>(){
				@Override
				protected String getText(NamedTestResult object) {
					return extractor.getOutputText(object);
				}
			});
			result.add(new OutputFirstLineColumn<NamedTestResult>() {
				@Override
				protected String getText(NamedTestResult object) {
					return extractor.getOutputText(object);
				}
			});
			return result;
		}

		@Override
		public NamedTestResult getValue(NamedTestResult object) {
			return object;
		}
	}

	private void initColumns(Problem problem) {
		TestCaseNameColumn testCaseNameColumn = new TestCaseNameColumn();
		cellTable.addColumn(testCaseNameColumn, "Test name");
		cellTable.setColumnWidth(testCaseNameColumn, "120px");
		
		OutcomeColumn outcomeColumn = new OutcomeColumn();
		cellTable.addColumn(outcomeColumn, "Outcome");
		cellTable.setColumnWidth(outcomeColumn, "160px");
		
		ProblemType type = problem.getProblemType();
	    InputColumn inputColumn = new InputColumn();
		cellTable.addColumn(inputColumn, "Input");
	    cellTable.setColumnWidth(inputColumn, "100px");
		if (type.isOutputLiteral()) {
		    // appropriate for function/methods where we know the expected result,
		    // and the actual result.
		    // Unclear if C functions can be made to fit this category...
		    
			ExpectedOutputColumn expectedOutputColumn = new ExpectedOutputColumn();
			cellTable.addColumn(expectedOutputColumn, "Expected");
		    cellTable.setColumnWidth(expectedOutputColumn, "100px");

		    ActualOutputColumn actualOutputColumn = new ActualOutputColumn();
			cellTable.addColumn(actualOutputColumn, "Actual");
			cellTable.setColumnWidth(actualOutputColumn, "100px");
		}
		
		// Include the message for all problem types because certain code errors,
		// such as references to undefined variables, are only detected at runtime
		// for some languages (I'm looking at you, Python!).  The message field
		// is the only means we have currently to convey these errors to the
		// user.  It would be more elegant to show these as compiler diagnostics:
		// perhaps we will do that eventually.
	    cellTable.addColumn(new MessageColumn(), "Message");
		
		cellTable.addColumn(new OutputColumn(new ExtractOutputText<NamedTestResult>() {
			@Override
			public String getOutputText(NamedTestResult testResult) {
				return testResult.getTestResult().getStdout();
			}
		}), "Output");
		
		cellTable.addColumn(new OutputColumn(new ExtractOutputText<NamedTestResult>() {
			@Override
			public String getOutputText(NamedTestResult testResult) {
				return testResult.getTestResult().getStderr();
			}
		}), "Error output");
	}
	
	public void activate(final Session session, final SubscriptionRegistrar subscriptionRegistrar) {
		session.subscribe(Session.Event.ADDED_OBJECT, this, subscriptionRegistrar);
		
		Problem problem = session.get(Problem.class);
		
		// Create the columns - they are dependent on the problem type
		initColumns(problem);
		
		NamedTestResult[] testResultList = session.get(NamedTestResult[].class);
		if (testResultList != null) {
			displayTestResults(testResultList);
		}
	}

	@Override
	public void eventOccurred(Object key, Publisher publisher, Object hint) {
		if (key == Session.Event.ADDED_OBJECT && hint instanceof NamedTestResult[]) {
			displayTestResults((NamedTestResult[]) hint);
		}
	}
	
	private void displayTestResults(NamedTestResult[] testResultList) {
		cellTable.setRowData(Arrays.asList(testResultList));
	}
	
	@Override
	public void setSelected() {
		// Workaround for http://code.google.com/p/google-web-toolkit/issues/detail?id=7065
		cellTable.redraw();
	}
}

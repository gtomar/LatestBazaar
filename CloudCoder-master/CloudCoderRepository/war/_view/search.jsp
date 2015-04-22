<!DOCTYPE html>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="repo" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<html>
	<head>
		<repo:headStuff title="Search the repository"></repo:headStuff>
		<link
			rel="stylesheet"
			type="text/css"
			href="${pageContext.servletContext.contextPath}/css/jquery.dataTables.css" />
		<link
			rel="stylesheet"
			type="text/css"
			href="https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.24/themes/smoothness/jquery-ui.css" />
		<link
			rel="stylesheet"
			href="css/jquery.tagit.css"
			type="text/css" />
		<script
			src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.24/jquery-ui.min.js"
			type="text/javascript"
			charset="utf-8"></script>
		<script
			src="${pageContext.servletContext.contextPath}/js/tag-it.js"
			type="text/javascript"
			charset="utf-8"></script>
		<script
			type="text/javascript"
			src="${pageContext.servletContext.contextPath}/js/jquery.dataTables.min.js"></script>
		<script type="text/javascript">
			var problemTypeToLanguage = ${problemTypeToLanguage};
		
			// How to format a raw RepoProblem JSON object as a tuple to be displayed in the
			// search results DataTable.
			var repoProblemConvertFields = [
				function(obj) { return problemTypeToLanguage[obj.repo_problem.problem_type]; },
				function(obj) { return obj.repo_problem.testname; },
				function(obj) { return obj.repo_problem.brief_description; },
				function(obj) { return obj.repo_problem.author_name; },
				function(obj) { return obj.matched_tag_list.join(' '); }
			];

			// Variable to store the DataTable object so it can be used by callbacks			
			var dataTable;
			
			// Initiate an AJAX request to retrieve search results.
			function onSubmit() {
				$("#status").attr('class', 'status-pending').text("Seaching...");
				
				var queryUri = "${pageContext.servletContext.contextPath}/search";
				$.ajax({
					url: queryUri,
					dataType: "json",
					type: "post",
					data: {
						selectedTags: $("#selectedTags").tagit("assignedTags").join(" ")
					},
					success: function(data, textStatus, jqXHR) {
						// Result will be an array of JSON-encoded RepoProblemSearchResults
						
						//alert("Search returned " + data.length + " exercises");
						$("#status").attr('class', 'status-none').text("Received " + data.length + " results");

						// Convert exercises to row tuples						
						var j, i;
						var rowData = [];
						for (j = 0; j < data.length; j++) {
							var searchResult = data[j];
						
							var tuple = [];
							for (i = 0; i < repoProblemConvertFields.length; i++) {
								tuple.push(repoProblemConvertFields[i](searchResult));
							}
							
							// Store the search result object as the (non-displayed) last column value
							tuple.push(searchResult);
							
							rowData.push(tuple);
						}
						
						$("#searchResultsTable").dataTable().fnClearTable();
						$("#searchResultsTable").dataTable().fnAddData(rowData);
					},
					error: function(jqXHR, textStatus, errorThrown) {
						$("#status").attr('class', 'status-error').text("" + errorThrown);
					}
				});
			}
		
			$(document).ready(function() {
				$("#searchButton").click(onSubmit);

				// Use tag-it! on the selectedTags textbox.
				// Ajax/autocomplete code shamelessly stolen from
				// http://tag-it-autocomplete.heroku.com/
				$("#selectedTags").tagit({
				  tagSource: function(search, showChoices) {
				    var that = this;
				    $.ajax({
				      url: "${pageContext.servletContext.contextPath}/suggestTags",
				      data: {term: search.term},
				      type: "GET",
				      success: function(choices) {
				        showChoices(that._subtractArray(choices, that.assignedTags()));
				      }
				    });
				  }/*,
				  show_tag_url: "/tags/",
				  singleField: true,
				  singleFieldNode: $('#submit_tag_names')*/
				});
				
				// Enable DataTable on the search results table.
				dataTable = $("#searchResultsTable").dataTable({
					aoColumnDefs: [{
						fnRender: function(oObj, sVal) {
							return "<a href='${pageContext.servletContext.contextPath}/exercise/" +
								oObj.aData[5].repo_problem.hash +
								"' target='_blank' >" + sVal + "</a> " +
								"<img src='${pageContext.servletContext.contextPath}/images/newWindow.png' />";
						},
						aTargets: [ 2 ]
					}],
					//bPaginate: false,
					bSort: false,
					bInfo: false,
					bFilter: false
				});
			});
		</script>
	</head>
	<body>
		<repo:topBanner/>
		<div id="content">
			<h1>Search the exercise repository</h1>
			<p> Enter tags (e.g., java, c, etc.):
			<input id="selectedTags" type="text" size="60" />
			</p>
			
			<button id="searchButton">Search!</button> <span id="status" class="status-none"></span>
			
			<p>
				To see information about a specific exercise, click on the link in the
				<b>Description</b> column.
			</p>
			
			<table id="searchResultsTable">
				<thead>
						<tr>
							<th width="40px">Language</th>
							<th width="80px">Name</th>
							<th width="300px">Description</th>
							<th width="100px">Author</th>
							<th width="100px">Matched Tags</th>
						</tr>
				</thead>
				<tbody>
				</tbody>
			</table>
		</div>
	</body>
</html>
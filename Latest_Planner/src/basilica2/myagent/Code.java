package basilica2.myagent;

import java.util.ArrayList;

import basilica2.myagent.PlanTree.Node;

public class Code {
	public ArrayList<String> insertTextChanges; 
	public ArrayList<String> insertLinesChanges; 
	public ArrayList<String> deleteTextChanges; 
	public ArrayList<String> deleteLinesChanges; 
	public ArrayList<String> fullTextChanges; 
	public String overallChange;
	
	public Code() {
		insertTextChanges = new ArrayList<String>();
		insertLinesChanges = new ArrayList<String>();
		deleteTextChanges = new ArrayList<String>();
		deleteLinesChanges = new ArrayList<String>();
		fullTextChanges = new ArrayList<String>();
		overallChange = "";
    }
}

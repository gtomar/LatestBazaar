package edu.cmu.cs.lti.tutalk.script;

import java.util.List;

/**
*
* @author leah.nh
* This class bundles together the information from a step in the scenario that will trigger Events in TutorActor.
* This information was previously represented as List<String> TutorTurns, but with the addition of Knowledge Tracing,
* it has become necessary to also pass Knowledge Components, and their result (success or failure). 
*/

public class TutorTurns {
	
	private List<String> tutorTurns;
	private List<String> kcSet;
	private String result;
	
	public TutorTurns()
	{
		tutorTurns = null;
		kcSet = null;
		result = null;
	}
	
	public void setTutorTurns(List<String> tt)
	{
		tutorTurns = tt;
	}
	
	public List<String> getTutorTurns()
	{
		return tutorTurns;
	}
	
	public void setKCset(List<String> kcs)
	{
		kcSet = kcs;
	}
	
	public List<String> getKCset()
	{
		return kcSet;
	}
	
	public void setResult(String r)
	{
		result = r;
	}
	
	public String getResult()
	{
		return result;
	}
	
	public void combine(TutorTurns tt2)
	{
		tutorTurns.addAll(tt2.getTutorTurns());
		if(kcSet!=null && tt2.getKCset()!=null)
		{
			//I'm not sure if this is a possible situation. I think it's not?
			//If it is, then we will have to find a more sophisticated method of combining TutorTurns.
			System.err.println("Cannot combine two KC sets.");
		} else if (tt2.kcSet!=null)
		{
			kcSet = tt2.getKCset();
			result = tt2.getResult();
		}
	}
	
	public void addTurns(String s)
	{
		tutorTurns.add(s);
	}
	
	public String toString()
	{
		return "Most recent tutor turns: " + tutorTurns.toString() + "\nKCs to change: " + kcSet.toString() + "\nResult of action: " + result;
	}

}

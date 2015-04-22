package basilica2.agents.events;

import edu.cmu.cs.lti.basilica2.core.Component;
import edu.cmu.cs.lti.basilica2.core.Event;

public class CodeEvent extends Event{
    
	public String insertTextChange; 
	public String insertLinesChange; 
	public String deleteTextChange; 
	public String deleteLinesChange; 
	public String fullTextChange; 
	public String overallChange;
	
	
	public CodeEvent(Component source, String insertText, String insertLines, 
			                           String deleteText, String deleteLines, 
			                           String fullText, String overall)
	{
		
		super(source);
		System.out.println("success");
		this.insertTextChange = insertText; 
		this.insertLinesChange = insertLines; 
		this.deleteTextChange = deleteText; 
		this.deleteLinesChange = deleteLines; 
		this.fullTextChange = fullText; 
		this.overallChange = overall;
		
	}
	
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return null;
	}

}

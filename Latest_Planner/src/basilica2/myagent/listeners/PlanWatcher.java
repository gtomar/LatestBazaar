package basilica2.myagent.listeners;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import edu.cmu.cs.lti.basilica2.core.Event;
import edu.cmu.cs.lti.project911.utils.log.Logger;
import basilica2.agents.components.InputCoordinator;
import basilica2.agents.events.CodeEvent;
import basilica2.agents.events.MessageEvent;
import basilica2.agents.events.MethodEvent;
import basilica2.agents.events.PlanEvent;
import basilica2.agents.events.ReadyEvent;
import basilica2.agents.events.priority.PriorityEvent;
import basilica2.agents.events.priority.PriorityEvent.Callback;
import basilica2.agents.listeners.BasilicaPreProcessor;
import basilica2.social.events.DormantGroupEvent;
import basilica2.social.events.DormantStudentEvent;
import basilica2.tutor.events.DoTutoringEvent;
import basilica2.agents.listeners.BasilicaAdapter;

import edu.cmu.cs.lti.basilica2.core.Agent;
import edu.cmu.cs.lti.basilica2.core.Event;
import edu.cmu.cs.lti.project911.utils.log.Logger;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import basilica2.myagent.Code;
import basilica2.myagent.PlanTree;
import basilica2.myagent.interpretCode;

public class PlanWatcher extends BasilicaAdapter implements BasilicaPreProcessor 
{
    
	private String plan;

	public PlanTree tree;
	public Code code;
	public PlanTree currentTree;

	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public PlanWatcher(Agent a) 
    {
		super(a);
		Date date= new Date();
		plan="";
    	tree = new PlanTree("ROOT", "ROOT");
    	currentTree = tree;
    	code = new Code(new Timestamp(date.getTime()));
    	tree.root.parent = null;

    	String dialogueConfigFile="dialogues/dialogues-example.xml";
    	loadconfiguration(dialogueConfigFile, tree);
    	findInTree(tree,"ROOT");
    	findInTree(tree,"INSTRUCTION_REVIEW");
    	findInTree(tree,"NTH_CIRCULAR_PRIME");
	}
    

	private void printTree(PlanTree tree)
	{
		ArrayList list = tree.root.children;
        for (int i = 0; i < list.size(); i++) {
        	PlanTree t = (PlanTree) list.get(i);
        	System.out.println(t.root.name + " " + t.root.identified);
        	printTree(t);

        }
	}
	
	private boolean isWholeTreeTraversed(PlanTree tree)
	{
		ArrayList list = tree.root.children;
		boolean traversed = true;
        for (int i = 0; i < list.size(); i++) {
        	PlanTree t = (PlanTree) list.get(i);
        	if( t.root.identified == false){
        		return false;
        	}
        	if(isWholeTreeTraversed(t) == false){
        		return false;
        	}
        }
        return true;
	}
	
	public boolean findInTree(PlanTree tree, String concept)
	{
		boolean found = false;
		if(tree.root.name.equals(concept))
		{
			if(tree.root.identified == false){
				tree.root.identified = true;
				plan = plan + concept + "\n";
				currentTree = tree;
				IsParentIdentified(tree);
				return true;			
			}
			else
			{
				return false;
			}
		}
		else
		{
			ArrayList list = tree.root.children;
	        for (int i = 0; i < list.size(); i++) {
	        	PlanTree t = (PlanTree) list.get(i);
	        	found = found | findInTree(t, concept);
	        }
		}
		return found;
	}
	
	
	public String findUnidentifiedConcept(PlanTree tree)
	{
		String concept = null;
		if(tree.root.identified == false)
		{
			tree.root.identified = true;
			concept = (String) tree.root.name;
			plan = plan + concept + "\n";
			currentTree = tree;
			IsParentIdentified(tree);
		}
		else
		{
			ArrayList list = tree.root.children;
			for (int i = 0; i < list.size(); i++) {
	        	PlanTree t = (PlanTree) list.get(i);
	        	concept = findUnidentifiedConcept(t);
	        	if(concept != null)
	        	{
	        		break;
	        	}
	        }
		}
		return concept;
	}

	
	private void IsParentIdentified(PlanTree tree)
	{
		PlanTree parent = tree.root.parent;
		if(parent == null)
		{
			return;
		}
		ArrayList list = parent.root.children;
		boolean identified = true;
        for (int i = 0; i < list.size(); i++) {
        	PlanTree t = (PlanTree) list.get(i);
        	if(t.root.identified == false)
        	{
        		identified = false;
        	}
        }
        
        if(identified)
        {
        	parent.root.identified = true;
        	//currentTree = parent;
        	IsParentIdentified(parent);
        }
	}

	
	private void loadconfiguration(String f, PlanTree tree)
	{
		try
		{
			DOMParser parser = new DOMParser();
			parser.parse(f);
			Document dom = parser.getDocument();
			NodeList dialogsNodes = dom.getElementsByTagName("dialogs");
			if ((dialogsNodes != null) && (dialogsNodes.getLength() != 0))
			{
				Element conceptNode = (Element) dialogsNodes.item(0);
				NodeList conceptNodes = conceptNode.getElementsByTagName("CONCEPT");
				addTriggers(conceptNodes, tree, 1);
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}	    
	}
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void addTriggers(NodeList conceptNodes, PlanTree tree, int level)
	{
		if ((conceptNodes != null) && (conceptNodes.getLength() != 0))
		{
			for (int i = 0; i < conceptNodes.getLength(); i++)
			{
				Element conceptElement = (Element) conceptNodes.item(i);
				String conceptName = conceptElement.getAttribute("name");
				String conceptDetailedName = conceptElement.getAttribute("detailed_name");
				
				String[] triggers = new String[1];
				triggers[0] = conceptName;  
				
				PlanTree temp_tree = new PlanTree(conceptName, conceptDetailedName);
				temp_tree.root.parent = tree;
				tree.root.children.add(temp_tree);
				
				NodeList tempconceptNodes = conceptElement.getElementsByTagName("CONCEPT"+level);
				addTriggers(tempconceptNodes, temp_tree, level+1);
			}
		}
	}	
	
	
	/**
	 * @param source the InputCoordinator - to push new events to. (Modified events don't need to be re-pushed).
	 * @param event an incoming event which matches one of this preprocessor's advertised classes (see getPreprocessorEventClasses)
	 * 
	 * Preprocess an incoming event, by modifying this event or creating a new event in response. 
	 * All original and new events will be passed by the InputCoordinator to the second-stage Reactors ("BasilicaListener" instances).
	 */
	@Override
	public void preProcessEvent(InputCoordinator source, Event event)
	{
		
		if (event instanceof MessageEvent)
		{
			MessageEvent me = (MessageEvent)event;
			String[] annotations = me.getAllAnnotations();
			
			for (String s: annotations)
		    {
				if(findInTree(tree,s))
				{
					sendMethodEvent(source, s);
					System.out.println("sent method proposal => " + s);
					log(Logger.LOG_NORMAL, "plan after message event => " + plan);
				}
				
				printTree(tree);
				System.out.println(tree.root.name + " " + tree.root.identified);
				if(isWholeTreeTraversed(tree))
				{
					//this new event will be added to the queue for second-stage processing.
					PlanEvent plan = new PlanEvent(source,  "Okay, it seems plan is ready now. You're on your own from here on. Good luck!", "NOTICE_DONE");
					source.queueNewEvent(plan);							
				}
				else if(s.equals("HELP"))
				{           
					PlanTree parent  = currentTree.root.parent;
					String key = findUnidentifiedConcept(parent);
					while(key==null)
					{
						parent = parent.root.parent;
						key = findUnidentifiedConcept(parent);
					}
			    	DoTutoringEvent toot = new DoTutoringEvent(source, key);
					source.addPreprocessedEvent(toot);
				}			
	        }								    
	    }
		else if (event instanceof ReadyEvent)
		{
			if(isWholeTreeTraversed(tree))
			{
				
				PlanEvent plan = new PlanEvent(source,  "Okay, it seems plan is ready now. You're on your own from here on. Good luck!", "NOTICE_DONE");
				source.queueNewEvent(plan);
			}
			else
			{
		    	printTree(tree);
				PlanTree parent  = currentTree.root.parent;
				String key = findUnidentifiedConcept(parent);
				while(key==null || key == "ROOT")
				{
					parent = parent.root.parent;
					key = findUnidentifiedConcept(parent);
				}
				PlanEvent plan = new PlanEvent(source,  "Plan is not yet complete. You are missing some steps. Let me help you with this.", "INCOMPLETE");

				source.queueNewEvent(plan);
					    
				DoTutoringEvent toot = new DoTutoringEvent(source, key + "_HELP");
				source.addPreprocessedEvent(toot);
			}						
		}
		else if (event instanceof CodeEvent)
		{
			
			
			code.insertTextChanges.add(((CodeEvent) event).insertTextChange);
			code.insertLinesChanges.add(((CodeEvent) event).insertLinesChange);
			code.deleteTextChanges.add(((CodeEvent) event).deleteTextChange);
			code.deleteLinesChanges.add(((CodeEvent) event).deleteLinesChange);
			code.fullTextChanges.add(((CodeEvent) event).fullTextChange);
			code.overallChange =((CodeEvent) event).overallChange;
			
			int length = code.insertTextChanges.size();
			String previous_change = "";
			if(length >= 2)
			{
				previous_change = code.insertTextChanges.get(length-2);
			}
			
			System.out.println(((CodeEvent) event).insertTextChange);
			System.out.println(((CodeEvent) event).insertLinesChange);
			System.out.println(((CodeEvent) event).overallChange);
			
			Date date= new Date();
			code.timestamp = new Timestamp(date.getTime());
			
			String method1 = "unknown";
			interpretCode interpretcode = new interpretCode();
			try {
				method1 = interpretcode.getMethod(code.overallChange, ((CodeEvent) event).insertTextChange);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("method1 # " + method1);
			if(findInTree(tree,method1.toUpperCase()))
			{
				System.out.println("method1 => " + method1);
				PlanEvent plan = new PlanEvent(source,  "I see you are working on " + currentTree.root.detailed_name + ". Good! Let me know if you want to talk about it more.", "STUCK");

				source.queueNewEvent(plan);
			}
			
			
			String method2 = "unknown";
			try {
				method2 = interpretcode.getMethod(code.overallChange, ((CodeEvent) event).insertLinesChange);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("method2 # " + method2);
			if(findInTree(tree,method2.toUpperCase()))
			{
				System.out.println("method2 => " + method2);
				PlanEvent plan = new PlanEvent(source,  "I see you are working on " + currentTree.root.detailed_name + ". Good! Let me know if you want to talk about it more.", "STUCK");

				source.queueNewEvent(plan);
			}
		
			String method3 = "unknown";
			try {
				method3 = interpretcode.getMethod(code.overallChange, previous_change + ((CodeEvent) event).insertLinesChange);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("method3 # " + method3);
			if(findInTree(tree,method3.toUpperCase()))
			{
				System.out.println("method3 => " + method3);
				PlanEvent plan = new PlanEvent(source,  "I see you are working on " + currentTree.root.detailed_name + ". Good! Let me know if you want to talk about it more.", "STUCK");

				source.queueNewEvent(plan);
			}
			

			log(Logger.LOG_NORMAL, "insertTextChange: " + ((CodeEvent) event).insertTextChange + "\n" +
					               "insertLinesChange: " + ((CodeEvent) event).insertLinesChange + "\n" +
					               "previous change: " + previous_change + "\n" +
					               "method1: " + method1 + "\n" +
					               "method2: " + method2 + "\n" +
					               "method3: " + method3);
			log(Logger.LOG_NORMAL, "Code => " + ((CodeEvent) event).overallChange);
			log(Logger.LOG_NORMAL, "plan after code event => " + plan);
			
		}
		else if (event instanceof DormantStudentEvent || event instanceof DormantGroupEvent)
		{
			Date date= new Date();
			Timestamp currentTimestamp = new Timestamp(date.getTime());
			
	    	if(currentTimestamp.getTime() - code.timestamp.getTime() > 60000)
	    	{
				PlanTree parent  = currentTree.root.parent;
				String key = findUnidentifiedConcept(parent);
				while(key==null || key == "ROOT")
				{
					parent = parent.root.parent;
					key = findUnidentifiedConcept(parent);
				}
				PlanEvent plan = new PlanEvent(source,  "You seem to be stuck. Let me help you with this.", "STUCK");

				source.queueNewEvent(plan);
					    
				DoTutoringEvent toot = new DoTutoringEvent(source, key);
				source.addPreprocessedEvent(toot);
	    	}
		}			
	}
    
	public void sendMethodEvent(InputCoordinator source, String method1)
	{
		MethodEvent method = new MethodEvent(source, method1, "METHOD_SUGGESTION");
		
		PriorityEvent blackout = PriorityEvent.makeBlackoutEvent(method.from, method, 1000000.0, 5, 15);
		
		
		blackout.addCallback(new Callback()
		{
			@Override
			public void accepted(PriorityEvent p) 
			{

				
			}

			@Override
			public void rejected(PriorityEvent p) 
			{ 
				// ignore our rejected proposals
			}
		});
		
		/*
		 * There are other was to add a proposal besides addProposal -- see addEventProposal, pushProposal, etc in InputCoordinator.
		 */
		source.addProposal(blackout);
	}
	
	/**
	 * @return the classes of events that this Preprocessor cares about
	 */
	@Override
	public Class[] getPreprocessorEventClasses()
	{
		//only MessageEvents will be delivered to this watcher.
		return new Class[]{MessageEvent.class, ReadyEvent.class, DormantStudentEvent.class, CodeEvent.class, DormantGroupEvent.class};
	}


	@Override
	public void processEvent(InputCoordinator source, Event event) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public Class[] getListenerEventClasses() {
		// TODO Auto-generated method stub
		return null;
	}
}

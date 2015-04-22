package basilica2.myagent.listeners;

import java.util.ArrayList;

import edu.cmu.cs.lti.basilica2.core.Event;
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
import basilica2.tutor.events.DoTutoringEvent;


import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import basilica2.myagent.Code;
import basilica2.myagent.PlanTree;

public class PlanWatcher implements BasilicaPreProcessor
{
    
	private String plan;

	public PlanTree tree;
	public Code code;

	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public PlanWatcher() 
    {
    	plan="";
    	tree = new PlanTree("ROOT");
    	code = new Code();
    	tree.root.parent = null;
    	String dialogueConfigFile="dialogues/dialogues-example.xml";
    	loadconfiguration(dialogueConfigFile, tree);

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
	
	
	public void findInTree(PlanTree tree, String concept)
	{
		if(tree.root.name.equals(concept) && tree.root.identified == false)
		{
			tree.root.identified = true;
			plan = plan + concept + "\n";
			IsParentIdentified(tree);
		}
		else
		{
			ArrayList list = tree.root.children;
	        for (int i = 0; i < list.size(); i++) {
	        	PlanTree t = (PlanTree) list.get(i);
	        	findInTree(t, concept);
	        }
		}
	}
	
	
	public String findUnidentifiedConcept(PlanTree tree)
	{
		String concept = null;
		if(tree.root.identified == false)
		{
			tree.root.identified = true;
			concept = (String) tree.root.name;
			plan = plan + concept + "\n";
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
				
				String[] triggers = new String[1];
				triggers[0] = conceptName;  
				
				PlanTree temp_tree = new PlanTree(conceptName);
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
				findInTree(tree,s);
				
				printTree(tree);
				System.out.println(tree.root.name + " " + tree.root.identified);
				if(tree.root.identified)
				{
					//this new event will be added to the queue for second-stage processing.
					PlanEvent plan = new PlanEvent(source,  "Okay, it seems plan is ready now. You're on your own from here on. Good luck!", "NOTICE_DONE");
					source.queueNewEvent(plan);							
				}
				else if(s.equals("HELP"))
				{           
					String key = findUnidentifiedConcept(tree);
			    	DoTutoringEvent toot = new DoTutoringEvent(source, key+"_HELP");
					source.addPreprocessedEvent(toot);
				}			
	        }								    
	    }
		else if (event instanceof ReadyEvent)
		{
			if(tree.root.identified)
			{
				
				PlanEvent plan = new PlanEvent(source,  "Okay, it seems plan is ready now. You're on your own from here on. Good luck!", "NOTICE_DONE");
				source.queueNewEvent(plan);
			}
			else
			{
		    	printTree(tree);
		    	String key = findUnidentifiedConcept(tree);
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
			
			System.out.println("method time.");
			
			
			//source.queueNewEvent(method);
			
			MethodEvent method = new MethodEvent(source, "hello", "METHOD_SUGGESTION");
			
			
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
		/*else if (event instanceof DormantGroupEvent)
		{
			for (String key : myConcepts.keySet()) {
			    if(myConcepts.get(key)==0)
			    {					
					DoTutoringEvent toot = new DoTutoringEvent(source, key);
					source.addPreprocessedEvent(toot);
					myConcepts.put(key, 1);
					NumConcepts = NumConcepts - 1;
					break;
			    }
			}
		}	*/			
	}

	
	/**
	 * @return the classes of events that this Preprocessor cares about
	 */
	@Override
	public Class[] getPreprocessorEventClasses()
	{
		//only MessageEvents will be delivered to this watcher.
		return new Class[]{MessageEvent.class, ReadyEvent.class, DormantGroupEvent.class, CodeEvent.class};
	}
}

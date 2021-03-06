package basilica2.myagent.listeners;

import java.util.ArrayList;
import java.util.HashMap;

import edu.cmu.cs.lti.basilica2.core.Agent;
import edu.cmu.cs.lti.basilica2.core.Event;
import basilica2.agents.components.InputCoordinator;
import basilica2.agents.events.MessageEvent;
import basilica2.agents.events.ReadyEvent;
import basilica2.agents.events.priority.PriorityEvent;
import basilica2.agents.events.priority.PriorityEvent.Callback;
import basilica2.agents.listeners.BasilicaPreProcessor;
import basilica2.myagent.events.PlanEvent;
import basilica2.social.events.DormantGroupEvent;
import basilica2.tutor.events.DoTutoringEvent;


public class PlanWatcher implements BasilicaPreProcessor
{
    public PlanWatcher() 
    {
    	myMap.put("RECORDER_API",0);
    	myMap.put("AUDIO_SOURCE",0);
    	myMap.put("AUDIO_ENCODER",0);
    	myMap.put("OUTPUT_FORMAT",0);
    	myMap.put("PREPARE",0);
    	myMap.put("START",0);
    	myMap.put("STOP",0);
    	myMap.put("RELEASE",0);
    	NumConcepts = 8;
	}
    
    private Integer NumConcepts;
    
	private HashMap<String, Integer> myMap = new HashMap<String, Integer>();
    

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

			
			if(myMap.containsKey(s) && myMap.get(s)==0) // detect a repeated message
			{
				System.out.println(s);
				myMap.put(s,1);
				NumConcepts = NumConcepts - 1;
			}

			if(NumConcepts < 1)
			{
				//this new event will be added to the queue for second-stage processing.
				PlanEvent repeat = new PlanEvent(source,  "Okay, it seems plan is ready now. You're on your own from here on. Good luck!", "NOTICE_DONE");
				
				//source.addPreprocessedEvent(repeat);
				source.queueNewEvent(repeat);
				
				
				//MessageEvent m = new MessageEvent(source, source.getAgent().getUsername(), "Okay, it seems plan is ready now. You're on your own from here on. Good luck!", "NOTICE_DONE");
				//PriorityEvent blackout = PriorityEvent.makeBlackoutEvent("DONE", m, 1.0, 5, 15);
				
				/**
				 * components can be notified of accepted/rejected proposals by registering a callback.
				 */
				/*blackout.addCallback(new Callback()
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
				});*/
				
				/*
				 * There are other was to add a proposal besides addProposal -- see addEventProposal, pushProposal, etc in InputCoordinator.
				 */
				//source.addProposal(blackout);
				//source.addPreprocessedEvent(m);
			
			}
			
			if(s.equals("HELP"))
			{           
				for (String key : myMap.keySet()) {
				    if(myMap.get(key)==0)
				    {					

				    	DoTutoringEvent toot = new DoTutoringEvent(source, key);
						source.addPreprocessedEvent(toot);
						myMap.put(key, 1);
						NumConcepts = NumConcepts - 1;
						break;
				    }
				}
			}
			
	        }
		
		
		
		    
	    }
		else if (event instanceof ReadyEvent)
		{

			if(NumConcepts < 1)
			{
				
				PlanEvent repeat = new PlanEvent(source,  "Okay, it seems plan is ready now. You're on your own from here on. Good luck!", "NOTICE_DONE");
				
				//source.addPreprocessedEvent(repeat);
				source.queueNewEvent(repeat);
			
			}
			else
			{
				for (String key : myMap.keySet()) {
				    if(myMap.get(key)==0)
				    {					
						
				    	PlanEvent repeat = new PlanEvent(source,  "Plan is not yet complete. You are missing some steps. Let me help you with this.", "HELPING");
						
						//source.addPreprocessedEvent(repeat);
						source.queueNewEvent(repeat);
					    
				    	DoTutoringEvent toot = new DoTutoringEvent(source, key);
						source.addPreprocessedEvent(toot);
				    	//source.queueNewEvent(toot);
				    	
						myMap.put(key, 1);
						NumConcepts = NumConcepts - 1;
						break;
				    }
				}
			}
			
			
		}
		else if (event instanceof DormantGroupEvent)
		{
			for (String key : myMap.keySet()) {
			    if(myMap.get(key)==0)
			    {					
					DoTutoringEvent toot = new DoTutoringEvent(source, key);
					source.addPreprocessedEvent(toot);
					myMap.put(key, 1);
					NumConcepts = NumConcepts - 1;
					break;
			    }
			}
		}
		

		

	}

	/**
	 * @return the classes of events that this Preprocessor cares about
	 */
	@Override
	public Class[] getPreprocessorEventClasses()
	{
		//only MessageEvents will be delivered to this watcher.
		return new Class[]{MessageEvent.class, ReadyEvent.class, DormantGroupEvent.class};
	}

}

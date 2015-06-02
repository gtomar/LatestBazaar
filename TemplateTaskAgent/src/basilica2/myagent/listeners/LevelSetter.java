/*
 *  Copyright (c), 2009 Carnegie Mellon University.
 *  All rights reserved.
 *  
 *  Use in source and binary forms, with or without modifications, are permitted
 *  provided that that following conditions are met:
 *  
 *  1. Source code must retain the above copyright notice, this list of
 *  conditions and the following disclaimer.
 *  
 *  2. Binary form must reproduce the above copyright notice, this list of
 *  conditions and the following disclaimer in the documentation and/or
 *  other materials provided with the distribution.
 *  
 *  Permission to redistribute source and binary forms, with or without
 *  modifications, for any purpose must be obtained from the authors.
 *  Contact Rohit Kumar (rohitk@cs.cmu.edu) for such permission.
 *  
 *  THIS SOFTWARE IS PROVIDED BY CARNEGIE MELLON UNIVERSITY ``AS IS'' AND
 *  ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 *  THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 *  PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL CARNEGIE MELLON UNIVERSITY
 *  NOR ITS EMPLOYEES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 */
package basilica2.myagent.listeners;

import basilica2.agents.components.InputCoordinator;
import basilica2.agents.components.StateMemory;
import edu.cmu.cs.lti.basilica2.core.Agent;
import edu.cmu.cs.lti.basilica2.core.Component;
import edu.cmu.cs.lti.basilica2.core.Event;
//import basilica2.social.components.TriggerInfoMemory;
import basilica2.social.data.PerformanceInfo;
import basilica2.social.data.RulesInfo;
import basilica2.social.data.StrategyScores;
import basilica2.social.data.TriggerInfo;
import basilica2.social.data.TurnCounts;
//import basilica2.social.events.CheckBlockingEvent;
import basilica2.social.events.DormantGroupEvent;
import basilica2.social.events.DormantStudentEvent;
import basilica2.agents.events.MessageEvent;
import basilica2.agents.listeners.BasilicaAdapter;
import basilica2.agents.listeners.BasilicaPreProcessor;
import basilica2.social.events.SocialTriggerEvent;
import java.util.Map;

/**
 * 
 * @author leah.nh
 * 
 * Listen for the user to choose a level, and then pass that information to TutorActor.java
 * 
 */
public class LevelSetter extends BasilicaAdapter
{

	public static String GENERIC_NAME = "LevelSetter";
	private InputCoordinator source;

	public LevelSetter(Agent a)
	{
		super(a);
		System.out.println("*****Initialized Level Setter");
	}

	private void handleMessageEvent(MessageEvent me)
	{
		String[] beginner = me.checkAnnotation("BEGINNER");
		String[] intermediate = me.checkAnnotation("INTERMEDIATE");
		String[] advanced = me.checkAnnotation("ADVANCED");	
		
		System.out.println("*********String beginner: " + beginner);
		System.out.println("*********String intermediate: " + intermediate);
		System.out.println("*********String advanced: " + advanced);
		
	}

	@Override
	public void preProcessEvent(InputCoordinator source, Event e)
	{
		System.out.println("*******Preprocess event");
		this.source = source;
		if (e instanceof MessageEvent)
		{
			handleMessageEvent((MessageEvent) e);
			System.out.println("******Message event");
		}
		/**else if (e instanceof DormantStudentEvent)
		{
			handleDormantStudentEvent((DormantStudentEvent) e);
		}
		else if (e instanceof DormantGroupEvent)
		{
			handleDormantGroupEvent((DormantGroupEvent) e);
		}
		// else if (e instanceof CheckBlockingEvent) {
		// handleCheckBlockingEvent((CheckBlockingEvent) e);
		 * */
	}

	@Override
	public void processEvent(InputCoordinator source, Event event)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public Class[] getPreprocessorEventClasses()
	{
		return new Class[] { MessageEvent.class, DormantStudentEvent.class, DormantGroupEvent.class };
	}

	@Override
	public Class[] getListenerEventClasses()
	{
		// TODO Auto-generated method stub
		return new Class[] {};
	}
}

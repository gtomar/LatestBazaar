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
import basilica2.myagent.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import basilica2.myagent.KnowledgeComponent;
import basilica2.myagent.knowledgetracing.UserModel;

/**
 * 
 * @author leah.nh
 * 
 * Tracks the user model and knowledge components in the Bazaar context
 * 
 */
public class KnowledgeTracingListener extends BasilicaAdapter
{

	public static String GENERIC_NAME = "KnowledgeTracingListener";
	private InputCoordinator source;
	
	HashMap<String, UserModel> users = new HashMap<String, UserModel>();

	public KnowledgeTracingListener(Agent a)
	{
		super(a);
	}

	private void handleMessageEvent(MessageEvent me)
	{
		
	}

	@Override
	public void preProcessEvent(InputCoordinator source, Event e)
	{
		this.source = source;
	}

	@Override
	public void processEvent(InputCoordinator source, Event event)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public Class[] getPreprocessorEventClasses()
	{
		return new Class[] { MessageEvent.class };
	}

	@Override
	public Class[] getListenerEventClasses()
	{
		// TODO Auto-generated method stub
		return new Class[] {};
	}
	
	public double getProbabilityOfSuccess(String user, List<String> knowledgeComponents)
	{
		UserModel bob = getUser(user);
		return bob.predictSuccess(getKCs(knowledgeComponents));
	}

	public double getProbabilityOfKnowing(String user, String knowledgeComponent)
	{
		UserModel bob = getUser(user);
		return bob.getLearnedProb(getKC(knowledgeComponent));
	}
	
	public void observeAttempt(String user, String taskID, boolean success, Collection<String> kcs)
	{
		UserModel bob = getUser(user);
		bob.observeAttempt(taskID, success, getKCs(kcs));
	}

	public UserModel getUser(String user)
	{
		if(!users.containsKey(user))
		{
			users.put(user, new UserModel(user));
		}
		UserModel bob = users.get(user);
		return bob;
	}
	
	public void saveUserModels(String bobFilePath) throws IOException
	{
		File bobFile = new File(bobFilePath);
		//System.out.println("Storing knowledge component models to file "+bobFilePath);
		FileWriter writer = new FileWriter(bobFile);

		gee.toJson(users, writer);
		writer.close();
	}

	/** This section needs to be changed to extract KCs differently. As yet to be determined.
	public Collection<String> extractKCs(String text)
	{
		return extractor.getComponents(text);
	}
	*/
	
	private KnowledgeComponent[] getKCs(Collection<String> collection)
	{
		KnowledgeComponent[] kcs = new KnowledgeComponent[collection.size()];
		
		int i = 0;
		for(String key : collection)
		{
			KnowledgeComponent kc = getKC(key);
			kcs[i] = kc;
			i++;
		}
		return kcs;

	}

	/** This section needs to be revisited to decide on how to determine and store skills
	public KnowledgeComponent getKC(String key)
	{
		if (!blueprintSkills.containsKey(key)) 
			blueprintSkills.put(key, new KnowledgeComponent(key, 0.1, 0.01, 0.05, 0.01));
		return blueprintSkills.get(key);
	}

	public void clearUserModels()
	{
		blueprintSkills.clear();
		users.clear();
	}
	*/
}

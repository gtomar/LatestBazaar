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
package edu.cmu.cs.lti.tutalk.script;

import edu.cmu.cs.lti.tutalk.slim.ExecutionState;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author rohitk
 */
public class InitiationResponseStep extends Step
{

	private Initiation initiation;
	private List<Response> responses;
	private boolean responseMatched = false;
	private Goal subGoal = null;

	public InitiationResponseStep(Initiation i)
	{
		initiation = i;
		responses = new ArrayList<Response>();
	}

	public Initiation getInitiation()
	{
		return initiation;
	}

	public List<Response> getResponses()
	{
		return responses;
	}

	public void addResponse(Response r)
	{
		responses.add(r);
	}

	@Override
	public Concept execute(ExecutionState state)
	{
		if (!isDone())
		{

			Concept ret = null;
			if (!initiation.isDone())
			{
				state.log("step.initiationresponse." + initiation.getConcept().getLabel(), "Executing Initiation ... ", false);
				ret = initiation.execute(state);
			}
			else
			{
				if (!responseMatched)
				{
					state.log("step.initiationresponse." + initiation.getConcept().getLabel(), "Executing Response ... ", false);
					ret = new ResponseExpected(responses);
				}
				else
				{
					if (subGoal != null && !subGoal.isDone())
					{
						state.log("step.initiationresponse." + initiation.getConcept().getLabel(), "Executing SubGoal ... ", false);
						state.push(subGoal);

						ret = subGoal.execute(state);
						if (subGoal.isDone())
						{
							done = true;
							state.addDone(this);
							state.log("step.initiationresponse." + initiation.getConcept().getLabel(), "Done", true);
						}
					}
					else
					{
						done = true;
					}
				}
			}
			return ret;
		}
		return null;
	}

	@Override
	public Concept execute(Concept responseConcept, ExecutionState state)
	{
		if (!isDone())
		{
			Concept ret = null;
			if (initiation.isDone() && !responseMatched)
			{
				state.log("step.initiationresponse." + initiation.getConcept().getLabel(), "Matching Response ... ", false);
				for (int i = 0; i < responses.size(); i++)
				{
					if (responseConcept.getLabel().equals(responses.get(i).getConcept().getLabel()))
					{
						responseMatched = true;
						state.setExpected(new ArrayList<Response>());
						Response matchedResponse = responses.get(i);
						if (matchedResponse instanceof SubGoalResponse)
						{
							subGoal = ((SubGoalResponse) matchedResponse).getSubGoal();
							
							ret = matchedResponse.getFeedback().execute(state);
							
							
							if(!subGoal.isInProgress())
							{
								state.log("step.initiationresponse." + initiation.getConcept().getLabel(), "Pushing SubGoal ... ", false);
								state.push(subGoal);
							}
							else
							{
								System.err.println("Warning: Proposed Subgoal "+subGoal+"is already in progress!");
								state.log("step.initiationresponse." + initiation.getConcept().getLabel(), "Subgoal is already in progress!", false);
								subGoal = null;
							}
								
							
							/*ret = subGoal.execute(state);
							if (subGoal.isDone())
							{
								done = true;
								state.addDone(this);
								state.log("step.initiationresponse." + initiation.getConcept().getLabel(), "Done", true);
							}*/
						}
						else if (matchedResponse instanceof FeedbackResponse)
						{
							Feedback f = ((FeedbackResponse) matchedResponse).getFeedback();
							if (f != null)
							{
								ret = f.execute(state);
							}
							done = true;
							state.addDone(this);
							state.log("step.initiationresponse." + initiation.getConcept().getLabel(), "Done", true);
						}
						break;
					}
				}
			}
			return ret;
		}
		return null;
	}
}

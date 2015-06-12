package basilica2.tutor.knowledgetracing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import basilica2.tutor.knowledgetracing.BlameAssignmentStrategy.ConjunctiveBlameAssignmentStrategy;

/**
 * Represents a single user's estimated knowledge components -- how likely it is that they have knowledge of each component.
 * Includes methods (and encapsulates strategies) for updating these estimates from observations of task success/failure.
 * @author dadamson
 *
 */
public class UserModel 
{
	private String name;
	private HashMap<String, UserKnowledgeComponent> userKnowledgeComponents = new HashMap<String, UserKnowledgeComponent>();
	
	/**
	 * historical record of observed user task successes. May be used to learn new knowledge components later.
	 */
	private List<TaskObservation> observations = new ArrayList<TaskObservation>();
	
	/**
	 * The strategy currently being used to update the user knowledge components. Used by observeAttempt.
	 */
	private transient BlameAssignmentStrategy blameStrategy = null;
	
	/**
	 * 
	 * @return the strategy currently being used to update the user knowledge components
	 */
	public BlameAssignmentStrategy getBlameStrategy()
	{
		if(blameStrategy == null)
			blameStrategy = new ConjunctiveBlameAssignmentStrategy();
		return blameStrategy;
	}

	/**
	 * 
	 * @return the user's history of task observations.
	 */
	public List<TaskObservation> getObservations()
	{
		if(observations == null)
			observations  = new ArrayList<TaskObservation>();
		return observations;
	}
	
	
	/**
	 * Create a new model for a given user.
	 * @param name an identifying string for this user.
	 */
	public UserModel(String name) {
		super();
		this.name = name;
	}

	/**
	 * Get the estimate for this user for the given generic knowledge component. 
	 * If the user does not yet possess this KC, a new User KC is created and assigned default values.
	 * @param kc the generic knowledge component to look up for this user.
	 * @return
	 */
	public UserKnowledgeComponent getUserKC(KnowledgeComponent kc)
	{
		if(!userKnowledgeComponents.containsKey(kc.getName()))
		{
			UserKnowledgeComponent skill = new UserKnowledgeComponent(kc);
			userKnowledgeComponents.put(kc.getName(), skill);
		}
		return userKnowledgeComponents.get(kc.getName());
	}
	
	/**
	 * @param kc a single generic knowledge component
	 * @return the likelihood that the user has learned this particular component
	 */
	public double getLearnedProb(KnowledgeComponent kc)
	{
		return getUserKC(kc).getLearnedProb();
	}

	/**
	 * @param kc a single generic knowledge component
	 * @return the likelihood (including chance for error or luck) that the user will apply this KC successfully.
	 */
	public double getCorrectProb(KnowledgeComponent kc)
	{
		return getUserKC(kc).getCorrectProb();
	}
	
	public String toString()
	{
		StringBuilder description = new StringBuilder(String.format("User %s", name));
		
		for(UserKnowledgeComponent k : userKnowledgeComponents.values())
		{
			description.append("\n\t");
			description.append(k);
		}
		return description.toString();
	}

	/**
	 * 
	 * @param required all the knowledge components involved in a given task.
	 * @return the probability (including the chance of luck or error) that the user will successfully apply all of the given knowledge components.
	 */
	public double predictSuccess(KnowledgeComponent... required)
	{
		double prediction = 1.0;
		for(KnowledgeComponent kc : required)
		{
			prediction *= getUserKC(kc).getCorrectProb();
		}
		return prediction;
	}

	/**
	 * Update the user model with an observation of task success (or failure) involving the given knowledge components,
	 * and record the observation in the user's task history.
	 * @param taskID the unique ID of the observed task. May be used for later reconstructing the history to improve KC parameter estimation.
	 * @param success true if the user succeeded at the task without need for additional assistance, false otherwise.
	 * @param observed the collection of required knowledge components for this task.
	 */
	public void observeAttempt(String taskID, boolean success, KnowledgeComponent... observed)
	{
		getObservations().add(new TaskObservation(taskID, observed, success, predictSuccess(observed)));
		this.observeAttempt(success, observed);
	}

	/**
	 * Update the user model with an observation of task success (or failure) involving the given knowledge components.
	 * @param success true if the user succeeded at the task without need for additional assistance, false otherwise.
	 * @param observed the collection of required knowledge components for this task.
	 */
	private void observeAttempt(boolean success, KnowledgeComponent... observed)
	{
		System.out.println(Arrays.toString(observed));
		getBlameStrategy().observeAttempt(success, this, observed);
	}

}

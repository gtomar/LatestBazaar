package basilica2.tutor.knowledgetracing;

/**
 * Represents a single historical record of a task attempt. Potentially useful for learning per-KC parameters from collected user data.
 * @author dadamson
 *
 */
public class TaskObservation
{
	/**
	 * true if the user succeeded at this task.
	 */
	public boolean success;
	
	/**
	 * a unique identifier for this task.
	 */
	public String taskID;
	
	/**
	 * the UserModel's estimate of whether the user should have succeeded.
	 */
	public double pCorrect;
	
	/**
	 * the names of the knowledge components involved in this task.
	 */
	public String[] observedKCs;
	
	public TaskObservation(String taskID, KnowledgeComponent[] observedKCs, boolean success, double pCorrect)
	{
		super();
		this.observedKCs = new String[observedKCs.length];
		for(int i = 0; i < observedKCs.length; i++)
		{
			this.observedKCs[i] = observedKCs[i].getName();
		}
		this.success = success;
		this.taskID = taskID;
		this.pCorrect = pCorrect;
	}
	
	
}

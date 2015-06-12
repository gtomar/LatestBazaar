package basilica2.tutor.knowledgetracing;

/**
 * Represe
 * @author dadamson
 *
 */
public class KnowledgeComponent
{
	/** Human-friendly name of this KC **/
	private String skillName;

	/** probabilities of already-known, guess, slip, and learning rate **/
	private double defaultLikelihood, guess, slip, learningLikelihood;

	/**
	 * 
	 * @param name the name of this generic KC. Should be unique.
	 * @param l0 the initial likelihood that a user already knows this KC.
	 * @param guess the likelihood that a user will succeed by chance.
	 * @param slip the likelihood that a user will fail by chance.
	 * @param transit the likelihood that a user will learn this KC after applying it.
	 */
	public KnowledgeComponent(String name, double l0, double guess, double slip, double transit)
	{
		super();
		this.skillName = name;
		defaultLikelihood = l0;
		this.guess = guess;
		this.slip = slip;
		this.learningLikelihood = transit;
	}

	/**
	 * the name of this KC. Should be unique.
	 * @return
	 */
	public String getName()
	{
		return skillName;
	}

	/**
	 * 
	 * @return the likelihood that a new user already knows this KC.
	 */
	public double getDefaultLikelihood()
	{
		return defaultLikelihood;
	}

	/**
	 * 
	 * @return the likelihood that a user will succeed at applying this KC, by chance.
	 */
	public double getGuess()
	{
		return guess;
	}

	/**
	 * 
	 * @return the likelihood that a user will fail at applying this KC, by chance.
	 */
	public double getSlip()
	{
		return slip;
	}

	/**
	 * 
	 * @return the likelihood that a user will learn this KC after one additional attempt.
	 */
	public double getLearningLikelihood()
	{
		return learningLikelihood;
	}

	public String toString()
	{
		return getName();
	}
}

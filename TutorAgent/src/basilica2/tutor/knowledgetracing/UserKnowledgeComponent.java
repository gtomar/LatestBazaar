package basilica2.tutor.knowledgetracing;

public class UserKnowledgeComponent {

	private KnowledgeComponent kc;
	private double learnedProb;
	private double transit;
	
	public UserKnowledgeComponent(KnowledgeComponent knowledgeComponent) {
		super();
		this.kc = knowledgeComponent;
		this.learnedProb = knowledgeComponent.getDefaultLikelihood();
		this.transit = kc.getLearningLikelihood();
	}
	
	/**
	 * Construct an individualized skill, modified by multipliers for initial knowledge and learning rate.
	 * These may be considered to be user-specific (but perhaps skill-agnostic) factors that modify the general parameters for this skill.
	 * @param kc the skill being modeled.
	 * @param expertFactor relative to the population, how much more (or less) likely is it that this user has already learned this skill?
	 * @param learningFactor relative to the population, how much more (or less) likely is it that this user will quickly acquire this skill?
	 */
	public UserKnowledgeComponent(KnowledgeComponent kc, double expertFactor, double learningFactor)
	{
		this(kc);
		learnedProb = Math.max(kc.getDefaultLikelihood() * expertFactor, 1.0);
		transit =Math.max(kc.getLearningLikelihood() * learningFactor, 1.0);
	}
	
	
	/**@return the probability that the user will succeed at the next application of this skill**/
	public double getCorrectProb()
	{
		return learnedProb * (1 - kc.getSlip()) + (1 - learnedProb) * kc.getGuess();
	}
	
	/**@return the probability that the user has learned this skill.**/
	public double getLearnedProb()
	{
		return learnedProb;
	}

	/**observe an opportunity to apply this skill, and update the model accordingly.
	 * Used internally by some BlameAssignmentStrategies, but in general a UserKnowledgeComponent should be updated through the UserModel. **/
	public void observeSkill(boolean success) 
	{
		double learnProbGivenObservation;
		
		if(success)
		{
			learnProbGivenObservation = (learnedProb * (1 - kc.getSlip())) / 
					(learnedProb * (1 - kc.getSlip())  + ((1 - learnedProb) * kc.getGuess())) ;
		}
		else
		{
			learnProbGivenObservation = (learnedProb *  kc.getSlip()) / 
					(learnedProb *  kc.getSlip() + (1 - learnedProb) * (1 - kc.getGuess())) ;
		}

		learnedProb = learnProbGivenObservation + (1 - learnProbGivenObservation) * getTransit();
	}


	/**
	 * 
	 * @return the per-user likelihood of learning this skill after an additional attempt.
	 */
	double getTransit() 
	{
		return transit;
	}
	
	public String toString()
	{
		return String.format("Skill %s: Learned %.4f, Correct %.4f", kc.getName(), learnedProb, getCorrectProb());
	}

	public void setLearnedProb(double learnedProb)
	{
		this.learnedProb = learnedProb;
	}
	
}

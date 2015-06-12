package basilica2.tutor.knowledgetracing;

/**
 * Interface for representing alternative methods of apportioning "credit" for task success or failure among the knowledge components.
 * Used by UserModel.
 * 
 * @author dadamson
 *
 */
public interface BlameAssignmentStrategy
{
	/**
	 * Update the user knowledge component estimates for the given user and generic KCs, given task success (or not)
	 * @param success true if the user succeeded at the task without additional assistance, false otherwise.
	 * @param bob the user to update.
	 * @param components the knowledge components involved in this task.
	 */
	public void observeAttempt(boolean success, UserModel bob, KnowledgeComponent... components);

	/**
	 * Implements the failure-case KC inference suggested by http://educationaldatamining.org/EDM2011/wp-content/uploads/proc/edm2011_paper24_full_%20Koedinger.pdf
	 * @author dadamson
	 *
	 */
	public static class ConjunctiveBlameAssignmentStrategy implements BlameAssignmentStrategy
	{

		@Override
		public void observeAttempt(boolean success, UserModel bob, KnowledgeComponent... components)
		{

			if (success)
			{
				for (KnowledgeComponent kc : components)
				{
					UserKnowledgeComponent userKC = bob.getUserKC(kc);
					System.out.println("rewarding " + userKC);
					userKC.observeSkill(true);
				}
			}
			else
			{
				double pCorrect = 1.0;
				double[] updates = new double[components.length];
				for (KnowledgeComponent kc : components)
				{
					UserKnowledgeComponent userKC = bob.getUserKC(kc);
					double learnedProb = userKC.getLearnedProb();
					pCorrect *= learnedProb * (1 - kc.getSlip()) + (1 - learnedProb) * kc.getGuess();
				}

				for (int j = 0; j < components.length; j++)
				{
					KnowledgeComponent jKC = components[j];
					UserKnowledgeComponent jUserKC = bob.getUserKC(jKC);
					double jLearned = jUserKC.getLearnedProb();
					double pKnowNothingElse = 1.0;

					for(KnowledgeComponent iKC : components)
					{
						UserKnowledgeComponent iUserKC = bob.getUserKC(iKC);
						double iLearned = iUserKC.getLearnedProb();
						
						if(iKC != jKC)
							pKnowNothingElse *= 1 - (iLearned*(iKC.getSlip())+(1-iLearned)*(1-iKC.getGuess()));
						else
							System.out.println("skipping jKC "+jKC);
					}
					
					
					double pErrorGivenLearned = jKC.getSlip() + (1 - jKC.getSlip()) * (1 - pKnowNothingElse);
					double pLearnedGivenError = pErrorGivenLearned * jLearned / (1 - pCorrect);
					updates[j] = pLearnedGivenError; /*+ (1 - pLearnedGivenError) * jUserKC.getTransit();*/

					System.out.println(jKC);
					System.out.println(jKC.getName() +": p correct           = "+pCorrect);
					System.out.println(jKC.getName() +": p know nothing else = "+pKnowNothingElse);
					System.out.println(jKC.getName() +": p error given learn = "+pErrorGivenLearned);
					System.out.println(jKC.getName() +": p learn given error = "+pLearnedGivenError);
					System.out.println(jKC.getName() +": p learned given all = "+updates[j]);
				}
				
				//update all the user KCs after the math is done. 
				for (int j = 0; j < components.length; j++)
				{
					bob.getUserKC(components[j]).setLearnedProb(updates[j]);
				}

			}

		}
	}

	/**
	 * Implements the "Naive" blame assignment strategy of blaming only the weakest KC for failure -- other components are unchanged.
	 * @author dadamson
	 *
	 */
	public static class NaiveBlameAssignmentStrategy implements BlameAssignmentStrategy
	{
		@Override
		public void observeAttempt(boolean success, UserModel bob, KnowledgeComponent... components)
		{
			if (success)
			{
				for (KnowledgeComponent kc : components)
				{
					UserKnowledgeComponent userKC = bob.getUserKC(kc);
					System.out.println("rewarding " + userKC);
					userKC.observeSkill(true);
				}
			}

			else
			{
				// naive "weakest link is blamed" implementation
				UserKnowledgeComponent min = null;
				for (KnowledgeComponent kc : components)
				{
					UserKnowledgeComponent userKC = bob.getUserKC(kc);
					if (min == null || userKC.getLearnedProb() < min.getLearnedProb())
					{
						min = userKC;
					}
				}
				if (min != null)
				{
					System.out.println("blaming " + min);
					min.observeSkill(false);
				}
			}
		}

	}
}

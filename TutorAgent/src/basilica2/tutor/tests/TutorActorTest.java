package basilica2.tutor.tests;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import edu.cmu.cs.lti.basilica2.core.Agent;
import edu.cmu.cs.lti.basilica2.factory.AgentFactory;
import edu.cmu.cs.lti.basilica2.factory.FactoryBuiltAgent;
import basilica2.tutor.listeners.TutorActor;

public class TutorActorTest {
	
	protected AgentFactory myAgentFactory = new AgentFactory();

	@Before
	public void initialize()
	{
		Agent testAgent = myAgentFactory.makeAgentFromXML("agent.xml", "room_name");
		TutorActor ta = new TutorActor(testAgent);
	}
	
	//@Test
	//public void test() {
	//	fail("Not yet implemented");
	//}
	
	@Test
	  public void testAssertFalse() {
	    org.junit.Assert.assertFalse("failure - should be false", false);
	  }

}

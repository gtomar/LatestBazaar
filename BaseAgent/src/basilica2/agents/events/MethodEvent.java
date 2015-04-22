package basilica2.agents.events;

import java.io.Serializable;

import edu.cmu.cs.lti.basilica2.core.Component;
import edu.cmu.cs.lti.basilica2.core.Event;

public class MethodEvent extends Event implements Serializable 
{

	public String method;
	public String from;
	
	public MethodEvent(Component source, String text, String from)
	{
		super(source);
		this.method = text;
		this.from  = from;
        System.out.println("Inside Method");
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getMethod() {
		// TODO Auto-generated method stub
		return this.method;
	}


}

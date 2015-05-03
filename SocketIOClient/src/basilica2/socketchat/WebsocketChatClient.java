package basilica2.socketchat;

import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIO;
import io.socket.SocketIOException;

import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONException;
import org.json.JSONObject;

import basilica2.agents.components.ChatClient;
import basilica2.agents.events.CodeEvent;
import basilica2.agents.events.MessageEvent;
import basilica2.agents.events.MethodEvent;
import basilica2.agents.events.PresenceEvent;
import basilica2.agents.events.PrivateMessageEvent;
import basilica2.agents.events.ReadyEvent;
import basilica2.agents.events.WhiteboardEvent;
import edu.cmu.cs.lti.basilica2.core.Agent;
import edu.cmu.cs.lti.basilica2.core.Component;
import edu.cmu.cs.lti.basilica2.core.Event;

//TODO: MoodleChatClient.properties

public class WebsocketChatClient extends Component implements ChatClient
{	
	
	String socketURL = "http://localhost:8000";
	String agentUserName = "ROBOT";
	String agentRoomName = "ROOM";
	
	
	boolean connected = false;
	SocketIO socket;
	
	
	/* (non-Javadoc)
	 * @see basilica2.agents.components.ChatClient#disconnect()
	 */
	@Override
	public void disconnect()
	{
		socket.disconnect();
	}

	//TODO: poll for incoming messages
	//TODO: report presence events
	//TODO: report extant students at login


	public WebsocketChatClient(Agent a, String n, String pf)
	{
		super(a, n, pf);
		
		socketURL = myProperties.getProperty("socket_url", socketURL);
		agentUserName = a.getUsername();//myProperties.getProperty("agent_username", agentUserName);
		
		Logger sioLogger = java.util.logging.Logger.getLogger("io.socket");
		sioLogger.setLevel(Level.SEVERE);
		
	}

	@Override
	protected void processEvent(Event e)
	{
		if(e instanceof ReadyEvent)
		{
			ReadyEvent re = (ReadyEvent) e;
			try //almost always unready, global
			{
				shareReady(re.isReady(), re.isGlobalReset());
			}
			catch (Exception e1)
			{
				System.err.println("couldn't share image: "+re);
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		if(e instanceof WhiteboardEvent)
		{
			WhiteboardEvent me = (WhiteboardEvent) e;
			try
			{
				shareImage(me.filename);
			}
			catch (Exception e1)
			{
				System.err.println("couldn't share image: "+me);
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		else if(e instanceof DisplayHTMLEvent)
		{
			DisplayHTMLEvent me = (DisplayHTMLEvent) e;
			try
			{
				insertHTML(me.getText());
			}
			catch (Exception e1)
			{
				System.err.println("couldn't share html: "+me);
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		else if(e instanceof PrivateMessageEvent)
		{
			PrivateMessageEvent me = (PrivateMessageEvent) e;
			try
			{
				insertPrivateMessage(me.getText(), me.getDestinationUser());
			}
			catch (Exception e1)
			{
				System.err.println("couldn't send message: "+me);
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		else if(e instanceof MethodEvent)
		{
			MethodEvent m = (MethodEvent) e;
			try
			{
				System.out.println("share method stub.");
				shareSuggestion(m.getMethod());
			}
			catch (Exception e1)
			{
				System.err.println("couldn't send plan: ");
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		else if(e instanceof MessageEvent)
		{
			MessageEvent me = (MessageEvent) e;
			try
			{
				insertMessage(me.getText());
			}
			catch (Exception e1)
			{
				System.err.println("couldn't send message: "+me);
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		//TODO: private messages? "beeps"?

	}
	
	/* (non-Javadoc)
	 * @see basilica2.agents.components.ChatClient#login(java.lang.String)
	 */
	@Override
	public void login(String roomName)
	{
		agentRoomName = roomName;
		System.out.println("logging in to "+roomName+" at "+socketURL);
		try
		{
			socket = new SocketIO(socketURL);
			socket.connect(new ChatSocketCallback());
			socket.emit("adduser", agentRoomName, agentUserName, new Boolean(false));
		}
		catch (Exception e)
		{
			System.err.println("Couldn't log in to the chat server...");
			e.printStackTrace();
			
			JOptionPane.showMessageDialog(null, "Couldn't access chat server: "+ e.getMessage(), "Login Failure", JOptionPane.ERROR_MESSAGE);
			
			connected = false;
		}
	}

	

	@Override
	public String getType()
	{
		return "ChatClient";
	}
	

	protected void insertHTML(String message)
	{
		socket.emit("sendhtml", message);
	}
	
	protected void insertMessage(String message)
	{
		socket.emit("sendchat", message);
	}

	protected void insertPrivateMessage(String message, String toUser)
	{
		socket.emit("sendpm", message, toUser);
	}

	protected void shareImage(String imageURL)
	{
		socket.emit("sendimage", imageURL);
	}
	
	protected void shareSuggestion(String method)
	{
		socket.emit("sendsuggestion", method);
	}
	
	protected void shareReady(boolean ready, boolean global)
	{
		if(global)
			socket.emit("global_ready", ready?"ready":"unready");
		else
			socket.emit("ready", ready?"ready":"unready");
	}
	
	class ChatSocketCallback implements IOCallback
	{
		@Override
		public void onMessage(JSONObject json, IOAcknowledge ack)
		{
			try
			{
				System.out.println("Server said:" + json.toString(2));
			}
			catch (JSONException e)
			{
				e.printStackTrace();
			}
		}

		@Override
		public void onMessage(String data, IOAcknowledge ack)
		{
			System.out.println("Server said: " + data);
		}

		@Override
		public void onError(SocketIOException socketIOException)
		{
			System.out.println("an Error occurred...");
			socketIOException.printStackTrace();

			System.out.println("attempting to reconnect...");
			try
			{
				socket = new SocketIO(socketURL);
				socket.connect(new ChatSocketCallback());
				
				new Timer().schedule(new TimerTask()
				{
					public void run()
					{
						System.out.println("Logging back in to chat room.");
						socket.emit("adduser", agentRoomName, agentUserName, new Boolean(false));
						//socket.emit("sendchat" ,"...and I'm back!");
					}
				}, 1000L);
				
			}
			catch (MalformedURLException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}

		@Override
		public void onDisconnect()
		{
			System.out.println("Connection terminated.");
		}

		@Override
		public void onConnect()
		{
			System.out.println("Connection established");
		}

		@Override
		public void on(String event, IOAcknowledge ack, Object... args)
		{

			if(event.equals("updateusers"))
			{
				
					System.out.println("Users: "+((JSONObject) args[0]).names());
					
					Iterator<String> keys = ((JSONObject) args[0]).keys();
					while (keys.hasNext())
					{
						PresenceEvent pe = new PresenceEvent(WebsocketChatClient.this, keys.next(), PresenceEvent.PRESENT);
						WebsocketChatClient.this.broadcast(pe);
					}
				
			}
			else if(event.equals("updatechat"))
			{
				String message = (String)args[1];
				message = StringEscapeUtils.unescapeHtml4(message);
				MessageEvent me = new MessageEvent(WebsocketChatClient.this, (String)args[0], message);
				WebsocketChatClient.this.broadcast(me);
			}
			else if(event.equals("updateimage"))
			{
				String message = (String)args[1];
				WhiteboardEvent me = new WhiteboardEvent(WebsocketChatClient.this, message, (String)args[0], message);
				WebsocketChatClient.this.broadcast(me);
			}
			else if(event.equals("updatemethodstub"))
			{
				System.out.println("updatemethodstub");
			}
			else if(event.equals("updatecode"))
			{
				CodeEvent me = new CodeEvent(WebsocketChatClient.this, (String)args[0], (String)args[1], (String)args[2], (String)args[3], (String)args[4], (String)args[5]);
				System.out.println("We got : " + (String)args[5]);
				WebsocketChatClient.this.broadcast(me);
			}
			else if(event.equals("updatepresence"))
			{
				String message = (String)args[1];
				PresenceEvent pe = new PresenceEvent(WebsocketChatClient.this, (String)args[0], message.equals("join")?PresenceEvent.PRESENT:PresenceEvent.ABSENT);
				WebsocketChatClient.this.broadcast(pe);
			}
			else if(event.equals("updateready"))
			{
				String state = (String)args[1];
				ReadyEvent re = new ReadyEvent(WebsocketChatClient.this, state.equals("ready"), (String)args[0]);
				WebsocketChatClient.this.broadcast(re);
			}
			else if(event.equals("dumphistory"))
			{
				System.out.println("Ignoring historical messages.");
			}
			
			else
			{
				System.out.println("Server triggered unhandled event '" + event + "'");
			}
			
		}
	}	


}

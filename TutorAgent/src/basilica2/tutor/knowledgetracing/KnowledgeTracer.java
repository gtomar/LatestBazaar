package basilica2.tutor.knowledgetracing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import basilica2.tutor.*;

/**
 * Interface between TutorActor and BayesianKnowledgeTracing
 * nearly identical to KnowledgeTracingServer in the previous Bayesian Knowledge Tracing setup
 * @author leah.nh
 *
 */
public class KnowledgeTracer {
	
	Map<String, KnowledgeComponent> skills = new HashMap<String, KnowledgeComponent>();
	HashMap<String, UserModel> users = new HashMap<String, UserModel>();
	Gson gee = new GsonBuilder().enableComplexMapKeySerialization().create();
	
	public KnowledgeTracer()
	{
		String userFilePath = "bob_user.json";
		try {
			loadUserModels(userFilePath);
		} catch (JsonSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonIOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void loadUserModels(String userFilePath) throws JsonSyntaxException, JsonIOException, FileNotFoundException
	{
		//load the user model from a JSON file.
		File bobFile = new File(userFilePath);
		if (bobFile.exists())
		{
			System.out.println("reading Bob's skillset from file");
			users = gee.fromJson(new FileReader(bobFile), new TypeToken<HashMap<String, UserModel>>() {}.getType());
			System.out.println(users.keySet());
		}
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

/*	/public Collection<String> extractKCs(String text)
	{
		return extractor.getComponents(text);
	}*/
	
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

	public KnowledgeComponent getKC(String key)
	{
		if (!skills.containsKey(key)) 
			skills.put(key, new KnowledgeComponent(key, 0.1, 0.01, 0.05, 0.01));
		return skills.get(key);
	}

	public void clearUserModels()
	{
		skills.clear();
		users.clear();
	}
	

}

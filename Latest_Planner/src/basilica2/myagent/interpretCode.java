package basilica2.myagent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class interpretCode {
	
    public String getTopic(String diff, String method) {

	//Main method
	if (method.equals("main")) {
	    if (Pattern.matches(".*args\\[.*", diff)) {
		return "main_argument";
	    } else if (Pattern.matches(".*(c|C)ircular(p|P)rime[a-zA-Z]*\\(.*", diff)) {
		return "main_circular_primes";
	    } else if (Pattern.matches(".*= ?0;.*", diff)) {
		return "main_iter_n";
	    } else if (Pattern.matches(".*= ?2;.*", diff)) {
		return "main_iter_all";
	    }
	}
	
	//is_circular_prime method
	if (method.equals("is_circular_prime")) {
	    if (Pattern.matches(".*length\\(.*", diff)) {
		return "circprime_iteration";
	    } else if (Pattern.matches(".*(p|P)rime[a-zA-Z]+\\(.*", diff)) {
		return "circprime_isprime";
	    } else if (Pattern.matches(".*(r|R)otat[a-zA-Z]+\\(.*", diff)) {
		return "circprime_rotate";
	    }
	}

	//is_prime method
	if (method.equals("is_prime")) {
	    //The "method" section just involves identifying a method, not really writing code. Now that I think about it, maybe this is a bad way to divide things? But for now I will not try to identify that section
	    if (Pattern.matches(".*[a-z] ?(/|%) ?[a-z].*", diff)) {
		return "isprime_divide";
	    } else if (Pattern.matches(".*= ?2;.*", diff)) {
		return "isprime_iteration";
	    }
	}
	    
	//rotate method
	if (method.equals("rotate")) {
	    if (Pattern.matches(".*[a-z] ?(+) ?[a-z].*", diff)) {
		return "rotate_concat";
	    } else if (Pattern.matches(".*substring.*", diff)) {
		return "substring";
	    }
	}

	//didn't identify anything
	return method;

    }
	
	public String getMethod(String code, String diff) throws IOException {
		//possible currentSections are "main", "isCircularPrime," "isPrime," "rotate", "unknown"
		String currentSection = "";
		
		//Set up regular expressions for identifying the current method
		Pattern method = Pattern.compile(".*(public|private).* ([a-zA-Z0-9]+)\\(.*\\) ?\\{.*");
		Pattern isCircularPrimePat = Pattern.compile(".*((p|P)rim.*(c|C)irc|(c|C)irc.*(p|P)rim)");
		Pattern isPrimePat = Pattern.compile(".*(p|P)rim.*");
		Pattern rotatePat = Pattern.compile("(r|R)otat");
		
		//Set up regex to find location of diff
		BufferedReader diffReader = new BufferedReader(new StringReader(diff));
		int longestLine = 0;
		String mostImpt = "";
		for (String line = diffReader.readLine(); line !=null; line = diffReader.readLine()) {
			if (line.length() > longestLine) {
				longestLine = line.length();
				mostImpt = line;
			}
		}
		//For now, we're going to treat the longest line of the diff as the entire diff
		diff = mostImpt;
		System.out.println("Pattern: " + diff);
		//Read in lines of the code
				currentSection = "";
		BufferedReader codeReader = new BufferedReader(new StringReader(code));
		for (String line = codeReader.readLine(); line != null; line = codeReader.readLine()) {
		    //System.out.println("Looking at line: ");
		    //System.out.println(line);
			//first see if the line is a method signature
			Matcher m = method.matcher(line);
			if (m.matches()) {
			    //System.out.println("found a method");
				String methodName = m.group(2);
			    System.out.print("Found a method name: ");
			    System.out.println(methodName);
				Matcher circularPrimeName = isCircularPrimePat.matcher(methodName);
				Matcher primeName = isPrimePat.matcher(methodName);
				Matcher rotateName = rotatePat.matcher(methodName);

				if (circularPrimeName.find()) {
				    System.out.println("Method: is_circular_prime");
					currentSection = "is_circular_prime";
				} else if(primeName.find()) {
				    System.out.println("Method: is_prime");
					currentSection = "is_prime";
				} else if(rotateName.find()) {
				    System.out.println("Method: rotate");
					currentSection = "rotate";
				} else if(methodName.equals("main")) {
				    System.out.println("Method: main");
					currentSection = "main";
				}
			}
			//System.out.println("Comparing line: ");
			//System.out.println(line);
			//System.out.println("and diff: ");
			//System.out.println(diff);
			if (line.equals(diff)) {
			    //System.out.println("found my match in line");
			    String currentTopic = getTopic(diff, currentSection);
			    return currentTopic;
			    //This will still cause problems if the line is repeated verbatim in multiple places in the code
			}
		}
		
		
		
		return "unknown";
	}
	

}

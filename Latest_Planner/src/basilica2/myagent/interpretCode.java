package basilica2.myagent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class interpretCode {
	
	public static String getMethod(String code, String diff) throws IOException {
		//possible currentSections are "main", "isCircularPrime," "isPrime," "rotate", "unknown"
		String currentSection = "";
		
		//Set up regular expressions for identifying the current method
		Pattern method = Pattern.compile("(public|private).* ([a-zA-Z0-9]+)\\(.+\\) ?\\{");
		Pattern isCircularPrimePat = Pattern.compile(".*(prim.*circ|circ.*prim)");
		Pattern isPrimePat = Pattern.compile(".*prim.*");
		Pattern rotatePat = Pattern.compile("rotat");
		
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
		diff = diff.replaceAll("\\(", "\\\\(");
		diff = diff.replaceAll("\\)", "\\\\)");
		diff = diff.replaceAll("\\{", "\\\\{");
		diff = diff.replaceAll("\\}", "\\\\}");
		System.out.print("Will be looking for pattern: ");
		System.out.println(diff);
		Pattern d = Pattern.compile(diff);
		
		//Read in lines of the code
		BufferedReader codeReader = new BufferedReader(new StringReader(code));
		for (String line = codeReader.readLine(); line != null; line = codeReader.readLine()) {
		    System.out.println("Looking at line: ");
		    System.out.println(line);
			//first see if the line is a method signature
			Matcher m = method.matcher(line);
			if (m.matches()) {
				String methodName = m.group(2);
			    System.out.print("Found a method name: ");
			    System.out.println(methodName);
				Matcher circularPrimeName = isCircularPrimePat.matcher(methodName);
				Matcher primeName = isPrimePat.matcher(methodName);
				Matcher rotateName = rotatePat.matcher(methodName);
				if (circularPrimeName.matches()) {
				    System.out.println("Method: is_circular_prime");
					currentSection = "is_circular_prime";
				} else if(primeName.matches()) {
				    System.out.println("Method: is_prime");
					currentSection = "is_prime";
				} else if(rotateName.matches()) {
				    System.out.println("Method: rotate");
					currentSection = "rotate";
				} else if(methodName == "main") {
				    System.out.println("main");
					currentSection = "main";
				}
			}
			Matcher currentLine = d.matcher(line);
			if (currentLine.matches()) {
			    System.out.println("Found my match");
				//Identified the location of the diff
				//But wait, this will cause problems if the same line is repeated...
				//Or if the diff is more than one line...
				return currentSection;
			}
		}
		
		return "unknown";
	}

	

}

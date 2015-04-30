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
		Pattern method = Pattern.compile("(public|private).+([a-zA-Z0-9]+)[(].+[)] ?[{]");
		Pattern isCircularPrimePat = Pattern.compile(".*(prim.*circ|circ.*prim)");
		Pattern isPrimePat = Pattern.compile(".*prim.*");
		Pattern rotatePat = Pattern.compile("rotat");
		
		//Set up regex to find location of diff
		Pattern d = Pattern.compile(diff);
		
		//Read in lines of the code
		BufferedReader codeReader = new BufferedReader(new StringReader(code));
		for (String line = codeReader.readLine(); line != null; line = codeReader.readLine()) {
			//first see if the line is a method signature
			Matcher m = method.matcher(line);
			if (m.matches()) {
				String methodName = m.group(1);
				Matcher circularPrimeName = isCircularPrimePat.matcher(methodName);
				Matcher primeName = isPrimePat.matcher(methodName);
				Matcher rotateName = rotatePat.matcher(methodName);
				if (circularPrimeName.matches()) {
					currentSection = "isCircularPrime";
				} else if(primeName.matches()) {
					currentSection = "isPrime";
				} else if(rotateName.matches()) {
					currentSection = "rotate";
				} else if(methodName == "main") {
					currentSection = "main";
				}
			}
			Matcher currentLine = d.matcher(line);
			if (currentLine.matches()) {
				//Identified the location of the diff
				//But wait, this will cause problems if the same line is repeated...
				//Or if the diff is more than one line...
				return currentSection;
			}
		}
		
		return "unknown";
	}

	public static void main(String[] args) {
	    String code = args[0];
	    String diff = args[1];

	    String ret = "";
		try {
			ret = getMethod(code, diff);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	    System.out.println(ret);
	}

}

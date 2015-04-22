package junit.org.cloudcoder.analysis.features.java;

import static org.junit.Assert.*;

import java.io.IOException;

import org.cloudcoder.analysis.features.java.ParseToDotFile;
import org.cloudcoder.analysis.features.java.ParseToSimplifiedDotFile;
import org.junit.Test;

public class TestDotfile
{
    static final String INPUT="testing/features/java";
    static final String OUTPUT="output";

    @Test
    public void testBadCode() throws Exception {
        String filename=INPUT+"/MissingSemicolon.java";
        String outfile=OUTPUT+"/missingsemicolon.dot";
        ParseToDotFile p=new ParseToDotFile();
        p.parseToFullDotFile(filename, outfile);
    }
    
    static void simplifiedDotFile(String filename) throws IOException
    {
        String infile=INPUT+"/"+filename+".java";
        String outfile=OUTPUT+"/" +filename+".dot";
        ParseToSimplifiedDotFile p=new ParseToSimplifiedDotFile();
        p.parseToFullDotFile(infile, outfile);
    }
    
    static void dotFile(String filename) throws IOException
    {
        String infile=INPUT+"/"+filename+".java";
        String outfile=OUTPUT+"/" +filename+".dot";
        ParseToDotFile p=new ParseToDotFile();
        p.parseToFullDotFile(infile, outfile);
    }
    
    @Test
    public void testNestedForLoops() throws Exception
    {
        dotFile("NestedForLoops");
    }
    
    
    @Test
    public void testDotFile() throws Exception {
        String filename=INPUT+"/A1.java";
        String outfile=OUTPUT+"/a1.dot";
        ParseToDotFile p=new ParseToDotFile();
        p.parseToFullDotFile(filename, outfile);
    }
    
    @Test
    public void testIf1() throws Exception
    {
        //dotFile("If1");
        simplifiedDotFile("If1");
    }
    
    @Test
    public void testIfElse1() throws Exception
    {
        dotFile("IfElse");
    }
}

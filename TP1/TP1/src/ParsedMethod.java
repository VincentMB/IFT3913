import java.util.ArrayList;
import java.util.Arrays;

/**
 * Class ParsedMethod
 * Analyses and hold parsed method info
 */
public class ParsedMethod extends Parser{
    //Indicates if the code currently is in a / * comment
    private boolean inStarComment;
    //Indicates if the code currently is in javaDoc
    private boolean inJavaDoc;

    //Code
    private ArrayList<String> lines;
    //Parent class of current method
    private ParsedClass parentClass;
    //#Lines of code
    private int methodLOC;
    //#Lines of comments
    private int methodCLOC;
    //Density of comments
    private double methodDC;
    //Path of source file
    private String filePath;
    //Name of Method
    private String name;
    //McCabe complexity
    private int mcCabe;
    //Classe_BC How well commented is class
    private double methodBC;

    //ArrayList containing line numbers containing javadoc
    private ArrayList<Integer> javaDocLines;
    //ArrayList containing line numbers containing comments
    private ArrayList<Integer> commentLines;
    //ArrayList containing line numbers of code (includes comments/javadoc, excludes empty lines)
    private ArrayList<Integer> codeLines;

    //Predicates format
    private static final ArrayList<String> branches = new ArrayList<String>(
            Arrays.asList("for(","for (" ,"if(","if (","while(", "while (", "switch(", "switch ("));


    /**
     * Class constructor.
     * The constructor does the extraction since the process is automated.
     * Reads the code, then pass through the lines and roughly parses the code
     * for method analysis.
     *
     * @param lines        Code
     * @param filePath     Path of source file
     * @param name         Name of Class
     * @param parentClass  Class containing current method
     */
    public ParsedMethod(ArrayList<String> lines, String filePath, String name, ParsedClass parentClass) {
        super(lines, filePath, name, parentClass);

        this.filePath = filePath;


        this.inStarComment = false;
        this.inJavaDoc = false;
        super.setInStarComment(false);
        super.setInJavaDoc(false);
        this.commentLines = new ArrayList<Integer>();
        this.javaDocLines = new ArrayList<Integer>();
        this.codeLines = new ArrayList<Integer>();

        this.name = name;
        this.lines = lines;
        this.parentClass=parentClass;
        this.mcCabe=1;

        analyseLines();

        this.methodLOC = codeLines.size();
        this.methodCLOC = commentLines.size() + javaDocLines.size();
        this.methodDC = (this.methodLOC>0) ? (double)this.methodCLOC / this.methodLOC : 0;

        this.methodBC = this.methodDC/this.mcCabe;

    }
    //Override to save variable to current class
    @Override
    public boolean isJavaDoc(String line) {
        boolean b = super.isJavaDoc(line);
        this.inJavaDoc = super.isInJavaDoc();
        this.inStarComment = super.isInStarComment();
        return b;
    }
    //Override to save variable to current class
    @Override
    public boolean isComment(String line) {
        boolean b = super.isComment(line);
        this.inJavaDoc = super.isInJavaDoc();
        this.inStarComment = super.isInStarComment();
        return b;
    }
    /**
     * Analyses lines read by fetchLines() by looping through each.
     * Fills information about lines of codes and declarations line index
     */
    public void analyseLines() {
        int i = 0;
        for (String line : this.lines) {
            if (isJavaDoc(line)) this.javaDocLines.add(i);
            if (isComment(line)) this.commentLines.add(i);
            if (!isEmpty(line)) this.codeLines.add(i);
            //Check if/how many predicate is in method
            for (String branch : branches) {
                if (line.contains(branch)) this.mcCabe++;
            }
            i++;
        }
    }

    //---------------------------------------------//
    //-------------AUTO-GENERATED GETTERS----------//
    //---------------------------------------------//

    //Generic getters. No need for setters.

    public ArrayList<String> getLines() {
        return lines;
    }

    public ParsedClass getParentClass() {
        return parentClass;
    }

    public int getMethodLOC() {
        return methodLOC;
    }

    public int getMethodCLOC() {
        return methodCLOC;
    }

    public double getMethodDC() {
        return methodDC;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getName() {
        return name;
    }

    public int getMcCabe() {
        return mcCabe;
    }

    public double getMethodBC() {
        return methodBC;
    }
/*
    public boolean isInStarComment() {
        return inStarComment;
    }

    public boolean isInJavaDoc() {
        return inJavaDoc;
    }
*/
    public ArrayList<Integer> getJavaDocLines() {
        return javaDocLines;
    }

    public ArrayList<Integer> getCommentLines() {
        return commentLines;
    }

    public ArrayList<Integer> getCodeLines() {
        return codeLines;
    }
}

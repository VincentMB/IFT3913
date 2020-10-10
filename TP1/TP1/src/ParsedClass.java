import java.util.ArrayList;
import java.util.Arrays;

/**
 * Class ParsedClass
 * Analyses and hold parsed class info
 */
public class ParsedClass extends Parser {
    //Indicates if the code currently is in a / * comment
    private boolean inStarComment;
    //Indicates if the code currently is in javaDoc
    private boolean inJavaDoc;

    //Filter for method declaration identification
    private static final ArrayList<String> statements = new ArrayList<String>(
            Arrays.asList("for", "if", "do", "while", "switch", "catch"));

    //Code
    private ArrayList<String> lines;
    //Methods in class
    private ArrayList<ParsedMethod> methods;
    //#Lines of code
    private int classLOC;
    //#Lines of comments
    private int classCLOC;
    //Density of comments
    private double classDC;
    //Path of source file
    private String filePath;
    //Name of Class
    private String name;
    //Weighted Methods per Class
    private int WMC;
    //Classe_BC How well commented is class
    private double classBC;
    //ArrayList containing line numbers containing javadoc
    private ArrayList<Integer> javaDocLines;
    //ArrayList containing line numbers containing comments
    private ArrayList<Integer> commentLines;
    //ArrayList containing line numbers of code (includes comments/javadoc, excludes empty lines)
    private ArrayList<Integer> codeLines;
    //ArrayList containing line numbers of method declaration
    private ArrayList<Integer> methodDecLines;


    /**
    * Class constructor.
    * The constructor does the extraction since the process is automated.
    * Reads the code, then pass through the lines and roughly parses the code
    * for method decomposition and class analysis.
    *
    * @param lines        Code
    * @param filePath     Path of source file
    * @param name         Name of Class
    */
    public ParsedClass(ArrayList<String> lines, String filePath, String name){
        super(lines, filePath, name);
        this.lines=lines;
        this.filePath=filePath;
        this.name=name;
        this.inStarComment = false;
        this.inJavaDoc = false;
        super.setInStarComment(false);
        super.setInJavaDoc(false);
        this.commentLines = new ArrayList<Integer>();
        this.javaDocLines = new ArrayList<Integer>();
        this.codeLines = new ArrayList<Integer>();
        this.methodDecLines = new ArrayList<Integer>();
        this.methods = new ArrayList<ParsedMethod>();
        //Parse lines of code, find declarations
        analyseLines();
        //Generate declared methods
        generateParsedMethods();
        //Stats calculation
        this.classLOC = codeLines.size();
        this.classCLOC = commentLines.size()+javaDocLines.size();
        this.classDC = (double)this.classCLOC/this.classLOC;
        int nonWMC = 0;
        for(ParsedMethod m : this.methods){
            nonWMC+=m.getMcCabe();
        }
        //Sets to 0 if denominator is 0
        this.WMC= (this.methods.size()>0)? nonWMC/this.methods.size() : 0;
        //Sets to 0 if denominator is 0
        this.classBC = (this.WMC>0) ? this.classDC/this.WMC : 0;
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
    public void analyseLines(){
        int i=0; //iterator-index
        for(String line : this.lines){
            //JavaDoc is considered different from comment.
            //Will be added together for CLOC
            if(isJavaDoc(line)) this.javaDocLines.add(i);
            if(isComment(line)) this.commentLines.add(i);
            if(!isEmpty(line))this.codeLines.add(i);
            if(isMethodDeclaration(line)) this.methodDecLines.add(i);
            i++;
        }
    }

    /**
     * Generates ParsedMethod objects with ParsedMethod initialisation.
     * Seeks begining of class declaration INCLUDING JavaDoc,
     * then seeks end of declaration.
     * Feeds a subList of lines of code (methodCode) to ParsedMethod
     * along with the path and the extracted name of the method.
     * Then adds the new method to this classes ArrayList.
     */
    public void generateParsedMethods(){
        for(int idx : this.methodDecLines) {
            String methodName = getMethodName(this.lines.get(idx));
            int methodStart = idx - getJavaDocOffset(idx, 1);
            int methodEnd = getStatementEnd(idx,'{', '}');
            ArrayList<String> methodCode = new ArrayList<String>(this.lines.subList(methodStart, methodEnd));
            ParsedMethod newMethod = new ParsedMethod(methodCode, this.filePath,methodName,this);
            this.methods.add(newMethod);
        }
    }


    /**
     * Checks if line contains/is a method declaration
     * Returns false if line is in multiline comment or Javadoc
     *
     * !!!Would count class declaration inside single line comment!!!
     * !!!Will ignore if line starts with the end of a multiline comment!!!
     * Could be corrected by checking if // or /* before method. (~TP not about parsing)
     *
     * @param line  Current line of code analysed
     * @return      Boolean if contains class declaration
     */
    public boolean isMethodDeclaration(String line){
        if(this.inStarComment || this.inJavaDoc) return false;
        if (line.contains("(") && line.contains(")") && line.contains("{") && (!line.contains("."))) {

            String name = getMethodName(line);
            String id = name.split("_")[0];
            if(!this.statements.contains(id)) {return true;}
        }
        return false;

    }

    /**
     * Extracts the method name&attributes from line of code
     * Extracts the id part, removing whitespace
     * (could be refactored using .split & .trim(), probably simpler)
     * Extracts attributes in the same way, splitting type and attribute name
     * and keeping only the type.
     * Adds attributes to name to form id_attributeType1_..._attributeTypeN
     * @param line  Line of declaration
     * @return      method name (String)
     */
    public String getMethodName(String line){
        String name="";
        if (line.contains("(") && line.contains(")") && line.contains("{") && (!line.contains("."))) {
            //Get method name
            char[] lineChars = line.toCharArray();

            //End of name substring
            int i = line.indexOf('(')-1;
            while (Character.isWhitespace(lineChars[i])) i--;
            int nameEnd = i+1;

            //Start of name substring
            while (!Character.isWhitespace(lineChars[i])) i--;
            int nameStart = i+1;

            String id = line.substring(nameStart, nameEnd);
            name+=id;

            //Get attribute list
            //Start of attribute substring
            i = line.indexOf('(') + 1;

            if(lineChars[i]!=')') {
                while (Character.isWhitespace(lineChars[i])) i++;
                int attributeStart = i;
                //end of attribute substring
                i = line.indexOf(')') - 1;
                while (Character.isWhitespace(lineChars[i])) i--;
                int attributeEnd = i;
                String attributes = line.substring(attributeStart, attributeEnd);

                //Split attributes
                String[] attributeArray = attributes.split(",");
                ArrayList<String> typeArray = new ArrayList<String>();

                for (String a : attributeArray) {
                    //Isolate type
                    String[] type = a.trim().split("\\s+"); //Splitting by whitespace
                    name += ("_" + type[0]);
                }
            }

        }
        return name;
    }


    /**
     * Finds the end of a "statement", or in this case class/method declaration
     * Must be formed like so (on single or multiline): {...}, (...) etc.
     * Uses a stack push/pop principle (adds when opening, substracts when closing)
     * "Statement" ends when count = 0
     *
     * !!!Can get errors/break if uneven amount of open or close char!!!
     * !!!in comments or strings/chars!!!
     *
     * @param start     Index of line of beginning of statement
     * @param open      char of open statement      ie: '{'
     * @param close     char of closing statement     : '}'
     *
     * @return          Index of line of end of statement
     */
    public int getStatementEnd(int start, char open, char close){
        //counter
        int count=0;
        int i=start;
        //Take line
        String line = this.lines.get(i);
        char[] lineChars = line.toCharArray();
        //Check if open or close char(s) in line
        for(char c : lineChars){
            //+1 if open
            if (c == open) count++;
            //-1 if close
            if (c== close) count--;
        }
        //loop same logic
        while(count!=0) {
            try {
                line = this.lines.get(++i);
            }
            catch(Exception e){e.getCause();}

            lineChars = line.toCharArray();
            for (char c : lineChars) {
                if (c == open) count++;
                if (c == close) count--;
            }
        }
        //Count should be 0 before EOF, if code well formed
        return i;
    }

    /**
     * Checks how many lines of JavaDoc is before declaration
     * in order to include it in class/method definition.
     * Allows a certain amount of empty lines while still
     * considering attached
     * Checks up to maxBacktrack lines up for a non-empty line
     * If this line registered as a JavaDoc line, continues until
     * the next line isn't a Javadoc line.
     * The offset is the total lines passed.
     * @param decLine       Index of the line of the declaration
     * @param maxBacktrack  Maximum amout of empty lines
     * @return              Amount of lines before decl. to include
     */
    public int getJavaDocOffset(int decLine, int maxBacktrack){
        int offset=0;
        int i;
        int lineID = decLine-1;
        for(i = 0; i<maxBacktrack; i++){
            if(isEmpty(this.lines.get(lineID)) && lineID>0) lineID--;
        }
        if(this.javaDocLines.contains(lineID)) offset=i;
        while(this.javaDocLines.contains(lineID)){
            offset++;
            if(lineID>0) {
                lineID--;
            } else break;
        }
        return offset;
    }


    //---------------------------------------------//
    //-------------AUTO-GENERATED GETTERS----------//
    //---------------------------------------------//

    //Generic getters. No need for setters.
/*
    public boolean isInStarComment() {
        return inStarComment;
    }

    public boolean isInJavaDoc() {
        return inJavaDoc;
    }
*/
    public ArrayList<String> getLines() {
        return lines;
    }

    public ArrayList<ParsedMethod> getMethods() {
        return methods;
    }

    public int getClassLOC() {
        return classLOC;
    }

    public int getClassCLOC() {
        return classCLOC;
    }

    public double getClassDC() {
        return classDC;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getName() {
        return name;
    }

    public int getWMC() {
        return WMC;
    }

    public double getClassBC() {
        return classBC;
    }

    public ArrayList<Integer> getJavaDocLines() {
        return javaDocLines;
    }

    public ArrayList<Integer> getCommentLines() {
        return commentLines;
    }

    public ArrayList<Integer> getCodeLines() {
        return codeLines;
    }

    public ArrayList<Integer> getMethodDecLines() {
        return methodDecLines;
    }
}

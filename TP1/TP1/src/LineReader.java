import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

/**
 * Class LineReader
 * Extracts information from sigular .java source file
 * Should work on most code. Partial code in single-line comments
 * can break certain methods.
 */
class LineReader extends Parser {
    //Path of source file
    private String filePath;
    //Indicates if the code currently is in a / * comment
    private static boolean inStarComment;
    //Indicates if the code currently is in javaDoc
    private static boolean inJavaDoc;
    //ArrayList for lines of code
    private ArrayList<String> lines;
    //ArrayList containing line numbers containing javadoc
    private ArrayList<Integer> javaDocLines;
    //ArrayList containing line numbers containing comments
    private ArrayList<Integer> commentLines;
    //ArrayList containing line numbers of code (includes comments/javadoc, excludes empty lines)
    private ArrayList<Integer> codeLines;
    //ArrayList containing line numbers of class declaration
    private ArrayList<Integer> classDecLines;
    //ArrayList containing ParsedClass object (Classes declarated in this file)
    private ArrayList<ParsedClass> classes;



    /**
     * Class constructor.
     * The constructor does the extraction since the process is automated.
     * Reads the file, then pass through the lines and roughly parses the code
     * for classes decomposition.
     *
     * @param filePath     Path of source file
     */
    public LineReader(String filePath){
        super(filePath);
        //Initialisation of parameters
        this.filePath = filePath;
        this.inStarComment = false;
        this.inJavaDoc = false;
        super.setInStarComment(false);
        super.setInJavaDoc(false);
        this.commentLines = new ArrayList<Integer>();
        this.javaDocLines = new ArrayList<Integer>();
        this.codeLines = new ArrayList<Integer>();
        this.classDecLines = new ArrayList<Integer>();
        this.classes = new ArrayList<ParsedClass>();
        //Read lines from file
        this.lines = fetchLines();
        //Parse lines of code, find declarations
        analyseLines();
        //Generate declared classes
        generateParsedClasses();
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
     * Create a buffered reader which reads lines of source file.
     * @return  ArrayList containing separated lines in file
     */
    public ArrayList<String> fetchLines(){
        ArrayList<String> lines = new ArrayList<>();
        try{
            FileReader fr = new FileReader(filePath);
            BufferedReader r = new BufferedReader(fr);
            String str =r.readLine();

            //Adding lines to ArrayList
            while(str != null){
                lines.add(str);
                str = r.readLine();
            }
            r.close();
        }
        catch(Exception e){
            e.getStackTrace();
        }
        return lines;
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
            else if(isComment(line)) this.commentLines.add(i);
            else if(!isEmpty(line)) this.codeLines.add(i);
            if(isClassDeclaration(line)) this.classDecLines.add(i);
            i++;
        }
    }

    /**
     * Generates ParsedClass objects with ParsedClass initialisation.
     * Seeks begining of class declaration INCLUDING JavaDoc,
     * then seeks end of declaration.
     * Feeds a subList of lines of code (classCode) to ParsedClass
     * along with the path and the extracted name of the class.
     * Then adds the new class to this classes ArrayList.
     */
    public void generateParsedClasses(){
        //Loops through class declarations
        for(int idx : this.classDecLines) {
            //Extract class name
            String className = getClassName(this.lines.get(idx));
            //Get first LOC including preceding Javadoc (allowing up to one empty line between)
            int classStart = idx-getJavaDocOffset(idx, 1);
            //Get end of class declaration
            int classEnd = getStatementEnd(idx,'{', '}');
            //Create ArrayList of class code
            ArrayList<String> classCode = new ArrayList<String>(this.lines.subList(classStart, classEnd));
            //Construct ParsedClass
            ParsedClass newClass = new ParsedClass(classCode,this.filePath,className);
            //Save it to this ArrayList
            this.classes.add(newClass);
        }
    }

    /**
     * Checks if line contains/is a class declaration
     * Also includes enums and interfaces (abstract classes as well)
     * Returns false if line is in multiline comment or Javadoc
     *
     * !!!Would count class declaration inside single line comment!!!
     * !!!Will ignore if line starts with the end of a multiline comment!!!
     * Could be corrected by checking if // or /* before class. (~TP not about parsing)
     *
     * @param line  Current line of code analysed
     * @return      Boolean if contains class declaration
     */
    public boolean isClassDeclaration(String line){
        //Check if commented, if so, return false
        if (this.inJavaDoc || this.inStarComment) return false;
        //Check for class/abstract class
        else if (line.contains("class") &&
                line.contains("{")) {return true;}
        //Check for interface
        else if (line.contains("interface") &&
                line.contains("{")) {return true;}
        //Check for enum
        else if (line.contains("enum") &&
                line.contains("{")) {return true;}
        else return false;
    }

    /**
     * Extracts the class name from line of code
     * Checks what type of "Class" (class, enum or interface)
     * Extracts the name part, removing whitespace
     * (could be refactored using .split & .trim(), probably simpler)
     *
     * @param line  Line of declaration
     * @return      Class name (String)
     */
    public String getClassName(String line){

        String name="";
        String type="";
        int offset;
        //Check type of declaration
        if(line.contains("class")) type = "class";
        else if(line.contains("enum")) type = "enum";
        else if(line.contains("interface")) type = "interface";
        //Length of word of the type
        offset = type.length();

        //Makes sure it is one of the types
        if (offset>0 && line.contains("{")){
            char[] lineChars = line.toCharArray();
            int i = line.indexOf(type) + offset;
            //Trimming, then gets index of start and end of name in String line
            while (Character.isWhitespace(lineChars[i])) {
                i++;
            }
            int nameStart = i;

            while (!Character.isWhitespace(lineChars[i]) && lineChars[i]!='{') {
                i++;
            }
            int nameEnd = i;
            //Take the calculated substring
            name = line.substring(nameStart, nameEnd);
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

    public String getFilePath() {
        return filePath;
    }
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

    public ArrayList<Integer> getJavaDocLines() {
        return javaDocLines;
    }

    public ArrayList<Integer> getCommentLines() {
        return commentLines;
    }

    public ArrayList<Integer> getCodeLines() {
        return codeLines;
    }

    public ArrayList<Integer> getClassDecLines() {
        return classDecLines;
    }

    public ArrayList<ParsedClass> getClasses() {
        return classes;
    }
}
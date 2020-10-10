import java.util.ArrayList;

/**
 * Paremt class for parser classes.
 * Includes common methods for line counting
 */
public abstract class Parser {
    //Path of source file
    private String filePath;
    //Indicates if the code currently is in a / * comment
    private static boolean inStarComment;
    //Indicates if the code currently is in javaDoc
    private static boolean inJavaDoc;

    //Empty Constructors
    public Parser(String filePath){
    }

    public Parser(ArrayList<String> lines, String filePath, String name){
    }

    public Parser(ArrayList<String> lines, String filePath, String name, ParsedClass parentClass){
    }
    /**
     * Checks if the code is currently in JavaDoc comment
     * Checks for beginning of JavaDoc
     * Checks for end of multiline comments/JavaDoc
     * Adjusts class values appropriately
     *
     * @param line  Current line of code analysed
     * @return      Boolean if line is JavaDoc
     */
    public boolean isJavaDoc(String line){
        boolean lineIsJavaDoc = false;
        if(this.inJavaDoc){
            lineIsJavaDoc = true;
        }
        if(line.contains("/**")){
            lineIsJavaDoc = true;
            inJavaDoc = true;
        }
        if(line.contains("*/") && this.inJavaDoc){
            inJavaDoc = false;
            inStarComment = false;
        }
        return lineIsJavaDoc;
    }

    /**
     * Excludes lines in Javadoc to prevent double counting
     * Checks if the code is currently in multiline comment
     * Checks for beginning of comments (single or multi)
     * Checks for end of multiline comments
     * Adjusts class values appropriately
     *
     * @param line  Current line of code analysed
     * @return      Boolean if line is a comment
     */
    public boolean isComment(String line){
        //Assume false
        boolean lineHasComment = false;
        //Prevent double counting
        if (inJavaDoc) {
            return false;
        }
        //Check if already in comment
        if (inStarComment) {
            lineHasComment = true;
        }
        //Single line comments
        if(line.contains("//")){
            lineHasComment = true;
        }
        //Multiline comments
        if(line.contains("/*")){
            lineHasComment = true;
            inStarComment = true;
        }
        //End of comments
        if(line.contains("*/") && this.inStarComment){
            inJavaDoc = false;
            inStarComment = false;
        }
        return lineHasComment;
    }

    /**
     * Checks if line only contains whitespace
     *
     * @param line  Current line of code analysed
     * @return      Boolean if line is empty
     */
    public boolean isEmpty(String line){
        char[] lineChars = line.toCharArray();
        if(lineChars.length == 0){return true;}

        boolean lineIsEmpty = true;
        for (char c : lineChars){
            if (!Character.isWhitespace(c)){
                return false;
            }
        }
        return lineIsEmpty;
    }

    public String getFilePath() {
        return filePath;
    }

    public static void setInStarComment(boolean inStarComment) {
        Parser.inStarComment = inStarComment;
    }

    public static void setInJavaDoc(boolean inJavaDoc) {
        Parser.inJavaDoc = inJavaDoc;
    }

    public static boolean isInStarComment() {
        return inStarComment;
    }

    public static boolean isInJavaDoc() {
        return inJavaDoc;
    }
}

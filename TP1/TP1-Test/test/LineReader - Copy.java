import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Set;

class LineRader {

    private String path;
    private boolean inClass;
    private boolean inMethod;
    private boolean inStarComment;
    private boolean inJavaDoc;


    private ArrayList<String> lines;
    private Set<String> statements = Set.of();
    //asdf

    public LineRader(String path){
        ArrayList<String> lines = new ArrayList<>();
        this.path = path;
        this.inStarComment = false;
        this.inJavaDoc = false;
        try{
            FileReader fr = new FileReader(path);
            BufferedReader r = new BufferedReader(fr);
            String str =r.readLine();

            while(str != null){
                lines.add(str);
                str = r.readLine();
            }
            r.close();
        }
        catch(Exception e){
            e.getStackTrace();
        }
        this.lines = lines;
    }

    public void testingMethod(String a,  int b,boolean   c){

    }

    public boolean isComment(String line){
        boolean lineHasComment = false;
        if (this.inStarComment) {
            lineHasComment = true;
        }



        char[] lineChars = line.toCharArray();
        char c;
        for (int i = 0; i<lineChars.length; i++){
            c = lineChars[i];
            if (c == '/'){
                if(i+1<lineChars.length){
                    if (lineChars[i+1] == '/') {
                        lineHasComment = true;
                        i++;
                    }
                    else if (lineChars[i+1] == '*') {
                        lineHasComment = true;
                        this.inStarComment = true;
                        i++;
                    }
                }
            }
            if(c == '*'){
                if(i+1<lineChars.length){
                    if (lineChars[i+1] == '/') {
                        this.inStarComment = false;
                        i++;
                    }
                }
            }
        }
        return lineHasComment;
    }
/**
 * This is a test JAVADOC
 * */
    public boolean isJavaDoc(String line){
        boolean lineIsJavaDoc = false;
        if(this.inJavaDoc){
            lineIsJavaDoc = true;
        }
        char[] lineChars = line.toCharArray();
        char c;
        for (int i = 0; i<lineChars.length; i++){
            c = lineChars[i];
            if (c == '/'){
                if(i+1<lineChars.length){
                    if (lineChars[i+1] == '*') {
                        if (lineChars[i+2] == '*') {
                            lineIsJavaDoc = true;
                            this.inJavaDoc = true;
                            i += 2;
                        }
                    }
                }
            }
            if(c == '*'){
                if(i+1<lineChars.length){
                    if (lineChars[i+1] == '/') {
                        this.inJavaDoc = false;
                        i++;
                    }
                }
            }
        }
        return lineIsJavaDoc;
    }
//add
    public boolean isEmpty(String line){
        char[] lineChars = line.toCharArray(); //5
        if(lineChars.length == 0){return true;}

        boolean lineIsEmpty = true;
        for (char c : lineChars){
            if (c != ' ' || c != '\t'){
                return false;
            }
        }
        return lineIsEmpty;
    }
    //This methods assumes that the declaration line
    // does not start with a comment
    //Will count if method declaration is commented with //
    public boolean isMethodDeclaration(String line){
        char[] lineChars = line.toCharArray();
        if(this.inStarComment || this.inJavaDoc) return false;
        if (line.contains("(") && line.contains(")") && line.contains("")) {
            int i = line.indexOf('(')-1;
            while(lineChars[i]==' ' || lineChars[i]=='\t'){
                i--;
            }
            int nameEnd = i;
            while(lineChars[i]!=' ' || lineChars[i]!='\t'){
                i--;
            }
            int nameStart = i;
            String name = line.substring(nameStart, nameEnd);
            if (!this.statements.contains(name)) return true;
        }
        return false;

    }
/*testing*/
    /*still
    testing
     */
     public boolean isClassDeclaration(String line){
         char[] lineChars = line.toCharArray();
         if (this.inClass || this.inJavaDoc || this.inStarComment) return false;
         if (line.contains("cla")) {return true;}
         else {return false;}
     }
}

   enum    testEnum  {

}

    interface   testInterface {

}
/**
 * Author:      Vincent Mainville-Blanchard
 * For:         IFT3913
 * Assignment : TP1
 * Due date :   09/10/2020
 */

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Program takes a single path in argument
 * Finds all .java files id directory and sub-directories
 * Parses and analyse the code
 * Writes classes and methods quality metrics in
 * classes.csv and methodes.csv
 */
public class Main {
    private ArrayList<ParsedClass> classes;
    private ArrayList<ParsedMethod> methods;
    public static void main(String[] args) {
        //String originPath = "C:\\Users\\Vincent\\Documents\\IFT3913\\jfreechart-master"; FOR Q4
        if(args.length==1) {
            String originPath = args[0];
            //List of java files in path
            List<Path> files = new ArrayList<Path>();
            //ArrayLists for classes and methods in path
            ArrayList<ParsedClass> classes = new ArrayList<ParsedClass>();
            ArrayList<ParsedMethod> methods = new ArrayList<ParsedMethod>();
            //Get all java files
            files = fetchFiles(originPath);
            //Parse files & extract classes/methods info
            parseClassesAndMethods(files, classes, methods);
            //create CSV files
            createCSV();
            //Write info to CSVs
            writeToCSV(classes, methods);
        } else System.out.println("Veuillez entrer un et un seul argument comme chemin d'origine");
    }

    /**
     * Scans all files in directory and subdirectories for
     * .java file and write path into list.
     *
     * @param originPath    Path in which to search files
     * @return              List of .java files path
     */
    public static List<Path> fetchFiles(String originPath){
        List<Path> files = new ArrayList<Path>();
        //Fetching files
        try{
            //Source: https://stackoverflow.com/questions/48563709/java-8-get-files-from-folder-subfolder
            //User:   Eugene     Edited by:keuleJ
            Path configFilePath = FileSystems.getDefault()
                    .getPath(originPath);

            files = Files.walk(configFilePath)
                    .filter(s -> s.toString().endsWith(".java"))
                    .map(Path::toAbsolutePath).sorted().collect(Collectors.toList());

        }

        catch (Exception e){
            e.printStackTrace();
            System.out.println("Erreur lors de la lecture des fichiers.");
        }
        return files;
    }

    /**
     * Initialise lineReader for all files in list
     * lineReader analyses file and initialises ParsedClasses
     * ParsedClass anaylses class and initialises ParsedMedthods
     * ParsedMethod analyses method.
     *
     * @param files     List of files path
     * @param classes   ArrayList for classes
     * @param methods   ArrayList for methods
     */
    public static void parseClassesAndMethods(List<Path> files,ArrayList<ParsedClass> classes,
                                              ArrayList<ParsedMethod> methods){
        //Gathering infos from files
        try {
            for (Path path : files) {
                //System.out.println(path);
                //Creates lineReader that recursively creates classes and methods
                LineReader lineReader = new LineReader(path.toString());
                //add class to classes
                for (ParsedClass c : lineReader.getClasses()) {
                    classes.add(c);
                    //add method to methods
                    for (ParsedMethod m : c.getMethods()) {
                        methods.add(m);
                    }
                }
            }
        } catch (Exception e){
            e.printStackTrace();
            System.out.println("Erreur lors de l'analyse des fichiers");
        }
    }

    /**
     * Simply creates two csv files
     */
    public static void createCSV() {
        try {
            File classFile = new File("classes.csv");
            classFile.createNewFile();
            File methodFile = new File("methodes.csv");
            methodFile.createNewFile();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erreur lors de la creation des fichiers .csv.");
        }
    }

    /**
     * Writes classes and methods infos in respective .csv files
     * Classes:  chemin, class, classe_LOC, classe_CLOC, classe_DC, WMC, classe_BC
     * Methods:  chemin, class, methode, methode_LOC, methode_CLOC, methode_DC, CC, methode_BC
     *
     * @param classes   ArrayList of classes
     * @param methods   ArrayList of methods
     */
    public static void writeToCSV(ArrayList<ParsedClass> classes, ArrayList<ParsedMethod> methods){
        try {

            FileWriter classWriter = new FileWriter("classes.csv");
            //Headers
            classWriter.write("chemin, class, classe_LOC, classe_CLOC, classe_DC, WMC, classe_BC");

            for (ParsedClass c : classes){
                //Create line String for each class
                String s = c.getFilePath()+", "+c.getName()+", "+c.getClassLOC()+", "+c.getClassCLOC()+", "+
                        c.getClassDC()+", "+c.getWMC()+", "+c.getClassBC();
                classWriter.write("\n");
                //Write to file
                classWriter.write(s);
            }
            classWriter.flush();
            classWriter.close();

            FileWriter methodWriter = new FileWriter("methodes.csv");
            //Headers
            methodWriter.write("chemin, class, methode, methode_LOC, methode_CLOC, methode_DC, CC, methode_BC");
            for (ParsedMethod m : methods){
                //Create line String for each class
                String s = m.getFilePath()+", "+m.getParentClass().getName()+", "+m.getName()+", "+m.getMethodLOC()+", "+
                        m.getMethodCLOC()+", "+ m.getMethodDC()+", "+m.getMcCabe()+", "+m.getMethodBC();
                methodWriter.write("\n");
                //Write to file
                methodWriter.write(s);
            }
            methodWriter.flush();
            methodWriter.close();
            System.out.println("Fin.");

        } catch (IOException e) {
            System.out.println("Erreur lors de l'ecriture des fichiers.");
            e.printStackTrace();
        }
    }

    //---------------------------------------------//
    //-------------------TESTING-------------------//
    //---------------------------------------------//
/*
    public static void test(){
        String originPath = "C:\\Users\\Vincent\\Documents\\IFT3913\\TP1-Test";

        //List of java files in path
        List<Path> files = new ArrayList<Path>();
        //ArrayLists for classes and methods in path
        ArrayList<ParsedClass> classes = new ArrayList<ParsedClass>();
        ArrayList<ParsedMethod> methods = new ArrayList<ParsedMethod>();
        //Get all java files
        files = fetchFiles(originPath);
        //Parse files & extract classes/methods info
        parseClassesAndMethods(files, classes, methods);

        unitTest(files.get(0).toString().equals("C:\\Users\\Vincent\\Documents\\IFT3913\\TP1-Test\\a.java"));
        unitTest(classes.get(0).getName().equals("LineRader"));
        unitTest(!classes.get(0).getName().equals("Blob"));
        unitTest(classes.get(0).getMethods().get(1).getName().equals("testingMethod_String_int_boolean"));
        unitTest(classes.get(0).getMethods().get(2).getMethodCLOC()==1);
        unitTest(classes.get(0).getMethods().get(3).getMethodCLOC()==7);

    }

    public static boolean unitTest(boolean testStatement){
        boolean testResult = true;
        if (!testStatement) testResult = false;
        System.out.println(testResult);
        return testResult;
    }

*/

}

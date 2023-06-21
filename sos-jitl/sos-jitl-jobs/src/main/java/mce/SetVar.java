package mce;

import java.io.FileWriter;
import java.io.IOException;
 
public class SetVar {
 
    protected void setVar(String name, String value) throws IOException {
        try {
            String tmpFileName = System.getenv("JS7_RETURN_VALUES");
            FileWriter fw = new FileWriter(tmpFileName, true);
            fw.write(name + "=" + value + System.lineSeparator());
            fw.close();

        } catch (IOException ioe) {
            System.err.println("IOException: " + ioe.getMessage());
        }
 
    }
 
    protected String getVar(String name){
       return System.getenv(name);
    }
 
    public static void main(String[] args) throws IOException {
 
        SetVar setAndGetVariables = new SetVar();
        System.out.println("var1=" +  setAndGetVariables.getVar("VAR1"));
        setAndGetVariables.setVar("var1", "newValue");
        setAndGetVariables.setVar("var2", "newValue2");
    }
 
}
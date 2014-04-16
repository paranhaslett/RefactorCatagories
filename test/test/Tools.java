package test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import com.paranhaslett.refactorcategory.ast.MyProgram;

import AST.BytecodeParser;
import AST.CompilationUnit;

public class Tools {
  public static void main(String[] args) {
    CompilationUnit cu = setupProgram("/home/paran/Documents/Test/Jasm/src/jasm/Main.java");
    try {
      FileWriter few = new FileWriter(new File("/home/paran/Main.java"));
      System.out.println(cu.toString());
      few.write(cu.toString());
      few.close();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
    
    public static CompilationUnit setupProgram(String filename) {
      MyProgram prog = new MyProgram();
      prog.state().reset();
      prog.initBytecodeReader(new BytecodeParser());
      try {
        InputStream in = new FileInputStream(filename);
        CompilationUnit cu = prog.getJavaParser().parse(in, filename);
        cu.setParent(prog);
        return cu;
      } catch (FileNotFoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (beaver.Parser.Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      return null;
    }


}

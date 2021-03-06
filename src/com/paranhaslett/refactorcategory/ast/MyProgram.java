package com.paranhaslett.refactorcategory.ast;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import beaver.Parser.Exception;

import AST.CompilationUnit;
import AST.JavaParser;
import AST.Program;

public class MyProgram extends Program{
  private parser.JavaParser jparser = new parser.JavaParser();
  public JavaParser getJavaParser() {
    return new JavaParser() {
      
      public CompilationUnit parse(java.io.InputStream is, String fileName)
          throws java.io.IOException, beaver.Parser.Exception {
        return jparser.parse(is, fileName);
      }
    };
  }
  
  public CompilationUnit getCompilationUnit(String name, byte[] content){
    ByteArrayInputStream bis = new ByteArrayInputStream(content);
    try {
      CompilationUnit cu = getJavaParser().parse(bis, name);
      cu.setParent(this);
      return cu;
    } catch (IOException | Exception e) {
      e.printStackTrace();
    }
    return null;
  }
}

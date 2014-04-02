package com.paranhaslett.refactorcategory.ast;

import AST.CompilationUnit;
import AST.JavaParser;

public class MyProgram {
  public JavaParser getJavaParser() {
    return new JavaParser() {
      public CompilationUnit parse(java.io.InputStream is, String fileName)
          throws java.io.IOException, beaver.Parser.Exception {

        return new parser.JavaParser().parse(is, fileName);
      }
    };
  }
}

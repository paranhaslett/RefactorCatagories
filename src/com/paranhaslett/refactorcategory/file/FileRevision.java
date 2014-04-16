package com.paranhaslett.refactorcategory.file;

import AST.BytecodeParser;

import com.paranhaslett.refactorcategory.ast.MyProgram;
import com.paranhaslett.refactorcategory.model.Revision;

public class FileRevision implements Revision {
  MyProgram program;

  @Override
  public String getName() {
    return "Text Revision";
  }

  @Override
  public MyProgram getProgram() {
    return program;
  }

  @Override
  public void setProgram() {
    this.program = new MyProgram();  
    program.state().reset();
    program.initBytecodeReader(new BytecodeParser());
  }

}

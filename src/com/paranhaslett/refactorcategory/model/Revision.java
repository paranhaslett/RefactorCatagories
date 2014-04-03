package com.paranhaslett.refactorcategory.model;

import com.paranhaslett.refactorcategory.ast.MyProgram;


public interface Revision {
  String getName();
  MyProgram getProgram();
  void setProgram();
}

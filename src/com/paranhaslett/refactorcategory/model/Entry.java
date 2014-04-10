package com.paranhaslett.refactorcategory.model;

import java.io.IOException;

import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.errors.MissingObjectException;

import com.paranhaslett.refactorcategory.Range;
import com.paranhaslett.refactorcategory.ast.Ast;

public interface Entry {
  
  RawText getRawText(Range<Long> range);
  
  RawText getRawText();
  
  String getPath();
  
  void open() throws MissingObjectException, IOException;
  
  Ast getCompilationUnit(Revision revision, String name);
  
  byte[] getContent();

}

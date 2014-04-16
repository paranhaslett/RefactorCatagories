package com.paranhaslett.refactorcategory.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.jgit.diff.RawText;

import com.paranhaslett.refactorcategory.Range;
import com.paranhaslett.refactorcategory.ast.Ast;
import com.paranhaslett.refactorcategory.model.Entry;
import com.paranhaslett.refactorcategory.model.Revision;

public class FileEntry implements Entry {
  private String path;
  private File file;
  private byte[] content;
  private RawText rawText;

  public FileEntry(String path) {
    this.path = path;
    this.file = new File(path);
  }

  @Override
  public RawText getRawText(Range<Long> range) {
    return null;
  }

  @Override
  public RawText getRawText() {
    return rawText;
  }

  @Override
  public String getPath() {
    return path;
  }

  @Override
  public void open() {
    try {
      InputStream inputStream = new FileInputStream(file);
      long length = file.length();
      content = new byte[(int) length];
      inputStream.read(content);
      inputStream.close();
      this.rawText = new RawText(content);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Override
  public Ast getCompilationUnit(Revision revision, String name) {
    Revision fileRevision = (FileRevision) revision;
    return new Ast(fileRevision.getProgram().getCompilationUnit(name, content));
  }

  @Override
  public byte[] getContent() {
    return content;
  }

}

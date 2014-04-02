package com.paranhaslett.refactorcategory.git;

import org.eclipse.jgit.diff.DiffEntry.Side;
import org.eclipse.jgit.diff.RawText;

import com.paranhaslett.refactorcategory.Range;
import com.paranhaslett.refactorcategory.model.FileEntry;

public class GitFileEntry implements FileEntry {

  GitEntry gitEntry;
  String path;
  Side side;
  RawText rawText;
  
  public GitEntry getGitEntry() {
    return gitEntry;
  }
  public String getPath() {
    return path;
  }
  public Side getSide() {
    return side;
  }
  public void setGitEntry(GitEntry gitEntry) {
    this.gitEntry = gitEntry;
  }
  public void setPath(String path) {
    this.path = path;
  }
  public void setSide(Side side) {
    this.side = side;
  }
  
  @Override
  public String getRawText(Range<Long> range) {
    // TODO Auto-generated method stub
    return null;
  }

 

}

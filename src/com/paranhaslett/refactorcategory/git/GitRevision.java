package com.paranhaslett.refactorcategory.git;

import java.util.List;

import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import com.paranhaslett.refactorcategory.Difference;
import com.paranhaslett.refactorcategory.model.Revision;

public class GitRevision implements Revision{
  RevCommit revCommit;

  public void setRevCommit(RevCommit revCommit) {
    this.revCommit = revCommit;
  }

  @Override
  public List<Difference> getFileEntries(Difference difference) {
    
    
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getName() {
    return revCommit.getShortMessage().substring(0, 20);
  }

  

}

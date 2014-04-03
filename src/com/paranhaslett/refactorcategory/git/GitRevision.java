package com.paranhaslett.refactorcategory.git;

import java.io.IOException;

import org.eclipse.jgit.diff.DiffEntry.Side;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import com.paranhaslett.refactorcategory.model.Revision;

public class GitRevision implements Revision {
  RevCommit revCommit;
  Side side;

  public void setRevCommit(RevCommit revCommit) {
    this.revCommit = revCommit;
  }
  
  @Override
  public String getName() {
    return revCommit.getShortMessage().substring(0, 10);
  }
  
  public Side getSide() {
    return side;
  }
  
  public void setSide(Side side) {
    this.side = side;
  }
  
  public CanonicalTreeParser getCtp(ObjectReader reader) throws IncorrectObjectTypeException, IOException{
    RevTree rt = revCommit.getTree();
    CanonicalTreeParser ctp = new CanonicalTreeParser(); 
    ctp.reset(reader, rt); 
    return ctp;
  }

  public GitRevision(RevCommit revCommit) {
    super();
    this.revCommit = revCommit;
  }

}

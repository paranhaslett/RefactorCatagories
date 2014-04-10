package com.paranhaslett.refactorcategory.git;

import org.eclipse.jgit.diff.ContentSource;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;

import com.paranhaslett.refactorcategory.model.Repo;

public class GitRepo implements Repo {

  private static GitRepo repo = null;
  
  ObjectReader reader;
  ContentSource.Pair source;
  Repository repository;

  public static Repo getRepo() {
    if (repo == null) {
      repo = new GitRepo();
    }
    return repo;
  }

  private GitRepo() {
  }
  
  void makeReader(){
    reader=repository.newObjectReader();
  }

  public void release() {
    if (reader != null)
      reader.release();
  }

  public void setRepo(Repository repository) {
    this.repository=repository;
    
  }

}

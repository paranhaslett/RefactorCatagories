package com.paranhaslett.refactorcategory;

import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.lib.Repository;

public class Difference implements Cloneable{
  public enum Language {
    COMMENT, PLAIN_TEXT, VALID_JAVA
  }

  public enum Type {
    BINARY, COPY, DELETE, EMPTY, INSERT, MODIFY, MOVE, RENAMED, REPLACE, VISIBILITY_REDUCTION, EQUIVALENT
  }

  Edit edit;
  Language language;
  CodeBlock newCb;
  CodeBlock oldCb;
  Repository Repo;
  double score;
  Type type;

  public Edit getEdit() {
    return edit;
  }

  public Language getLanguage() {
    return language;
  }

  public CodeBlock getNewCb() {
    return newCb;
  }

  public CodeBlock getOldCb() {
    return oldCb;
  }

  public Repository getRepo() {
    return Repo;
  }

  public double getScore() {
    return score;
  }

  public Type getType() {
    return type;
  }

  public void setEdit(Edit edit) {
    this.edit = edit;
  }

  public void setLanguage(Language language) {
    this.language = language;
  }

  public void setNewCb(CodeBlock newCb) {
    this.newCb = newCb;
  }

  public void setOldCb(CodeBlock oldCb) {
    this.oldCb = oldCb;
  }

  public void setRepo(Repository repo) {
    Repo = repo;
  }

  public void setScore(double score) {
    this.score = score;
  }

  public void setType(Type type) {
    this.type = type;
  }

  @Override
  public String toString() {
    // TODO Auto-generated method stub
    return super.toString();
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    // TODO Auto-generated method stub
    return super.clone();
  }
  
  
  
  
}

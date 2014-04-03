package com.paranhaslett.refactorcategory;

import java.util.List;

import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;

import com.paranhaslett.refactorcategory.model.Repo;

public class Difference implements Cloneable{
  public enum Language {
    COMMENT, PLAIN_TEXT, VALID_JAVA
  }

  public enum Type {
    BINARY, COPY, DELETE, EMPTY, INSERT, MODIFY, MOVE, RENAMED, REPLACE, VISIBILITY_REDUCTION, EQUIVALENT
  }

  Language language;
  CodeBlock newCb;
  CodeBlock oldCb;
  Repo Repo;
  double score;
  Type type;
  
  public Difference(CodeBlock oldCb, CodeBlock newCb){
    this.newCb = newCb;
    this.oldCb = oldCb;
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

  public Repo getRepo() {
    return Repo;
  }

  public double getScore() {
    return score;
  }

  public Type getType() {
    return type;
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

  public void setRepo(Repo repo) {
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

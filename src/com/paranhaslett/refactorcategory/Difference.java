package com.paranhaslett.refactorcategory;

import com.paranhaslett.refactorcategory.model.Repo;

public class Difference implements Cloneable{
  public enum Language {
    UNKNOWN, COMMENT, WHITESPACE, JAVA, BINARY
  }

  public enum Type {
    UNKNOWN, BINARY, COPY, DELETE, EMPTY, INSERT, MODIFY, MOVE, RENAMED, REPLACE, VISIBILITY_REDUCTION, EQUIVALENT
  }
  
  public enum Legality {
    UNKNOWN, DUBIOUS, LEGAL, ILLEGAL
  }

  private Language language = Language.UNKNOWN;
  private CodeBlock newCb;
  private CodeBlock oldCb;
  private Legality legality;
  private Repo Repo;
  private double score;
  private Type type = Type.UNKNOWN;
  
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

  public Legality getLegality() {
    return legality;
  }

  public void setLegality(Legality legality) {
    this.legality = legality;
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
    StringBuilder sb = new StringBuilder();
    sb.append(language).append(":").append(type).append(":");
    sb.append(oldCb).append(" to ").append(newCb);
    return sb.toString();
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    Difference difference = (Difference)super.clone();
    difference.setNewCb((CodeBlock)difference.newCb.clone());
    difference.setOldCb((CodeBlock)difference.oldCb.clone());
    return difference;
  }
  
}

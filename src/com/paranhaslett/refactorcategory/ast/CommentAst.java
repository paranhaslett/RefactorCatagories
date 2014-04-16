package com.paranhaslett.refactorcategory.ast;

import java.util.ArrayList;
import java.util.List;

import com.paranhaslett.refactorcategory.Range;
import com.paranhaslett.refactorcategory.Ranges;

public class CommentAst extends Ast implements Cloneable {
  private String text;
  long start;
  long end;

  public CommentAst(String text, long start, long end) {
    super(null);
    this.text = text;
    this.start = start;
  }

  @Override
  public int hashCode() {
    return text.hashCode();
  }

  public boolean dumpEquals(CommentAst ast) {   
    return ast.text.equals(text);
  }

  public List<Ast> getChildren() {
    List<Ast> result = new ArrayList<Ast>();
    result.add(this);
    return result;
  }

  public Long getStart() {
    return start;
  }

  public Range<Long> getRange() {
    return new Range<Long>(start, end);
  }

  public Ranges<Long> getRanges() {
    Ranges<Long> result = new Ranges<Long>();
    result.add(start, end);
    return result;
  }

  public boolean isEmpty() {
    return text==null||text.equals("");
  }

  public void setEnd(long end) {
    this.end = end;

  }

  public void setStart(long start) {
    this.start = start;

  }

  @Override
  public String toString() {
    return text;
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }

}

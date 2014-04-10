package com.paranhaslett.refactorcategory.ast;

import java.util.ArrayList;
import java.util.List;

import AST.ASTNode;

import com.paranhaslett.refactorcategory.Range;
import com.paranhaslett.refactorcategory.Ranges;

public class Ast implements Cloneable {
  ASTNode<ASTNode> astNode;

  public Ast(ASTNode<ASTNode> astNode) {
    this.astNode = astNode;
  }

  public Ast() {

  }

  // com.paranhaslett.refactorcategory.ast.Ast.content() content()

  @Override
  public int hashCode() {
    // TODO Auto-generated method stub
    return super.hashCode();
  }

  /*
   * This is only very crude as it figures out the equivilence for the drill
   * down stage only
   */
  public boolean dumpEquals(Ast ast) {
    if (astNode != null && ast.astNode != null) {
      if (astNode.dumpString().equals(ast.astNode.dumpString())) {
        return true;
      }
    }
    return false;
  }

  public ASTNode<ASTNode> getAstNode() {
    return astNode;
  }

  public List<Ast> getChildren() {
    int numChild = astNode.getNumChild();
    List<Ast> result = new ArrayList<Ast>();
    for (int i = 0; i < numChild; i++) {
      Ast child = new Ast();
      child.setAstNode(astNode.getChild(i));
      result.add(child);
    }
    return result;
  }

  public Long getStart() {
    return (long) astNode.getStart();
  }

  public Range<Long> getRange() {
    return new Range<Long>((long) astNode.getStart(), (long) astNode.getEnd());
  }

  public Ranges<Long> getRanges() {
    Ranges<Long> result = new Ranges<Long>();
    result.add((long) astNode.getStart(), (long) astNode.getEnd());
    return result;
  }

  public boolean isEmpty() {
    return astNode == null;
  }

  public void setAstNode(ASTNode<ASTNode> astNode) {
    this.astNode = astNode;
  }

  public void setEnd(long end) {
    astNode.setEnd((int) end);

  }

  public void setStart(long start) {
    astNode.setStart((int) start);

  }

  @Override
  public String toString() {
    // TODO Auto-generated method stub
    return super.toString();
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }

}

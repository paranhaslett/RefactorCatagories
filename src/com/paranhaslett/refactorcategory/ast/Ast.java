package com.paranhaslett.refactorcategory.ast;

import AST.ASTNode;

import com.paranhaslett.refactorcategory.Range;

public class Ast {
  ASTNode<ASTNode> astNode;

  // com.paranhaslett.refactorcategory.ast.Ast.content() content()

  @Override
  public int hashCode() {
    // TODO Auto-generated method stub
    return super.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    // TODO Auto-generated method stub
    return super.equals(obj);
  }

  public ASTNode<ASTNode> getAstNode() {
    return astNode;
  }

  public Ast getChild(int i) {
    Ast child = new Ast();
    child.astNode.getChild(i);
    return child;
  }

  public long getEnd() {
    return (long) astNode.getEnd();
  }

  public int getNumChild() {
    // TODO Auto-generated method stub
    return 0;
  }

  public Range<Long> getRange() {
    return new Range<Long>((long) astNode.getStart(), (long) astNode.getEnd());
  }

  public long getStart() {
    return (long) astNode.getStart();
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
}

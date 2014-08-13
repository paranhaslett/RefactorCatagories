package com.paranhaslett.refactorcategory.ast;

import java.util.ArrayList;
import java.util.List;

import AST.ASTNode;

import com.paranhaslett.refactorcategory.Range;

public class Ast implements Cloneable {
  private ASTNode<ASTNode> astNode;

  public Ast(ASTNode<ASTNode> astNode) {
    this.astNode = astNode;
  }

  // com.paranhaslett.refactorcategory.ast.Ast.content() content()

  @Override
  public int hashCode() {
    return astNode.dumpString().hashCode();
  }

  /*
   * This is only very crude as it figures out the equivalence for the drill
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
  
  public boolean equalTypes(Ast ast){
    if (astNode != null && ast.astNode != null) {
      String classA = getAstType(this);
      String classB = getAstType(ast);
      if (classA.equals(classB)) {
        return true;
      }
    }
    return false;
  }

  public List<Ast> getChildren() {
    int numChild = astNode.getNumChild();
    List<Ast> result = new ArrayList<Ast>();
    for (int i = 0; i < numChild; i++) {
      Ast child = new Ast(astNode.getChild(i));
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
  
  public String prettyPrint(){
    if (!(astNode instanceof AST.List)
        && !(astNode instanceof AST.Opt)){
      return astNode.toString();
    } 
    return null;
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
    return astNode.dumpString();
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }
  
  private String getAstType(Ast ast){
    Class<?> enclosingClass = ast.astNode.getClass().getEnclosingClass();
    if (enclosingClass != null) {
      return ast.astNode.getClass().getEnclosingClass().getName();
    } else 
      return ast.astNode.getClass().getName();
    }
  
  }


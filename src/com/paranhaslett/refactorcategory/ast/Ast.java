package com.paranhaslett.refactorcategory.ast;

import java.util.ArrayList;
import java.util.List;

import AST.ASTNode;

import com.paranhaslett.refactorcategory.Range;
import com.paranhaslett.refactorcategory.Ranges;

public class Ast implements Cloneable{
  ASTNode<ASTNode> astNode;
  
  public Ast(ASTNode<ASTNode> astNode){
    this.astNode =astNode;
  }
  
  public Ast(){
    
  }

  // com.paranhaslett.refactorcategory.ast.Ast.content() content()


  @Override
  public int hashCode() {
    // TODO Auto-generated method stub
    return super.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if(obj instanceof Ast){
      Ast ast = (Ast)obj;
      if(astNode != null && ast.astNode!=null){
        return astNode.dumpString().equals(ast.astNode.dumpString());
      }
    }
    // TODO Auto-generated method stub
    return super.equals(obj);
  }

  public ASTNode<ASTNode> getAstNode() {
    return astNode;
  }
  
  public List<Ast> getChildren() {
    int numChild = astNode.getNumChild();
    List<Ast> result = new ArrayList<Ast>();
    for (int i=0; i <numChild; i++){
      Ast child = new Ast();
      try{
       child.setAstNode(astNode.getChild(i));
      } catch (NullPointerException npe){
        npe.printStackTrace();
        System.out.println("Debug");
        child.setAstNode(new ASTNode());
      }
      result.add(child);
    }   
    return result;
  }
  
  public Long getStart(){
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

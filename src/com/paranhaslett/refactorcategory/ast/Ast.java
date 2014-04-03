package com.paranhaslett.refactorcategory.ast;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import AST.ASTNode;
import beaver.Parser.Exception;

import com.paranhaslett.refactorcategory.Range;

public class Ast {
  ASTNode<ASTNode> astNode;
  MyProgram program;
  
  //com.paranhaslett.refactorcategory.ast.Ast.content()  content()


  public void getCompilationUnit(String name, byte[] content){
    ByteArrayInputStream bis = new ByteArrayInputStream(content);
    try {
      astNode = program.getJavaParser().parse(bis, name);
    } catch (IOException | Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } 
  }

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

  public MyProgram getProgram() {
    return program;
  }

  public Range<Long> getRange(){
    return new Range<Long>((long)astNode.getStart(),(long)astNode.getEnd());
  }

  public long getStart(){
    return (long) astNode.getStart();
  }
  
  public boolean isEmpty() {
    return astNode == null;
  }
  
  public void setAstNode(ASTNode<ASTNode> astNode) {
    this.astNode = astNode;
  }
  public void setEnd(long end) {
    astNode.setEnd((int)end);
    
  }
  public void setProgram() {
    // TODO Auto-generated method stub
    
  }
  
  public void setProgram(MyProgram program) {
    this.program = program;
  }
  public void setStart(long start) {
    astNode.setStart((int)start);
    
  }
  
  @Override
  public String toString() {
    // TODO Auto-generated method stub
    return super.toString();
  }
 }
 
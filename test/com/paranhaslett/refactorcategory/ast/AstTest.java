package com.paranhaslett.refactorcategory.ast;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import test.Tools;

import AST.ASTNode;
import AST.BytecodeParser;
import AST.CompilationUnit;
import AST.FieldDecl;
import AST.MethodDecl;

public class AstTest {

  @Before
  public void setUp() throws Exception {

  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testHashCode() {
    MethodDecl mda = new MethodDecl();
    mda.setID("testMethodDeclA");
    MethodDecl mdb = new MethodDecl();
    mdb.setID("testMethodDeclA");
    MethodDecl mdc = new MethodDecl();
    mdc.setID("testMethodDeclC");
    Ast asta = new Ast(mda);
    Ast astb = new Ast(mdb);
    Ast astc = new Ast(mdc);
    assertEquals(asta.hashCode(), astb.hashCode());
    assertNotEquals(asta.hashCode(), astc.hashCode());
  }

  @Test
  public void testAst() {
    Ast asta = new Ast(null);
    MethodDecl mda = new MethodDecl();
    mda.setID("testMethodDeclA");
    Ast astb = new Ast(mda);

  }

  @Test
  public void testDumpEquals() {
    MethodDecl mda = new MethodDecl();
    mda.setID("testMethodDeclA");
    MethodDecl mdb = new MethodDecl();
    mdb.setID("testMethodDeclA");
    MethodDecl mdc = new MethodDecl();
    mdc.setID("testMethodDeclC");
    Ast asta = new Ast(mda);
    Ast astb = new Ast(mdb);
    Ast astc = new Ast(mdc);
    assertTrue(asta.dumpEquals(astb));
    assertFalse(asta.dumpEquals(astc));
  }

  @Test
  public void testEqualTypes() {
    FieldDecl fda = new FieldDecl();
    MethodDecl mdb = new MethodDecl();
    mdb.setID("testMethodDeclA");
    MethodDecl mdc = new MethodDecl();
    mdc.setID("testMethodDeclC");
    Ast asta = new Ast(fda);
    Ast astb = new Ast(mdb);
    Ast astc = new Ast(mdc);
    assertFalse(asta.equalTypes(astb));
    assertTrue(asta.equalTypes(astc));
  }

  @Test
  public void testGetChildren() {
    CompilationUnit cu = Tools.setupProgram("/home/paran/Main.java");
    Ast asta = new Ast(cu);
    List <Ast> children = asta.getChildren();
    assertTrue(children.size()==2);
    for (Ast child:children){
      assertTrue(child.equalTypes(new Ast(new AST.List())));
    }
  }

  @Test
  public void testGetStart() {
    CompilationUnit cu = Tools.setupProgram("/home/paran/Main.java");
    Ast asta = new Ast(cu);
    List <Ast> children = asta.getChildren();
    Ast second = children.get(1);
    long pos = ASTNode.makePosition(14, 1);
    assertEquals((long)second.getStart(), pos);
  }

  @Test
  public void testGetRange() {
    CompilationUnit cu = Tools.setupProgram("/home/paran/Main.java");
    Ast asta = new Ast(cu);
    List <Ast> children = asta.getChildren();
    Ast second = children.get(1);
    long pos = ASTNode.makePosition(14, 1);
    assertEquals((long)second.getRange().getStart(), pos);
  }

  @Test
  public void testIsEmpty() {
    MethodDecl mdb = new MethodDecl();
    mdb.setID("testMethodDeclA");
    MethodDecl mdc = new MethodDecl();
    mdc.setID("testMethodDeclC");
    Ast asta = new Ast(null);
    Ast astb = new Ast(mdb);
    Ast astc = new Ast(mdc);
    assertTrue(asta.isEmpty());
    assertFalse(astb.isEmpty());
    assertFalse(astc.isEmpty());
  }

  @Test
  public void testSetAstNode() {
    MethodDecl mdb = new MethodDecl();
    mdb.setID("testMethodDeclA");
    Ast asta = new Ast(null);
    Ast astb = new Ast(mdb);
    assertFalse(asta.dumpEquals(astb));
    asta.setAstNode(mdb);
    assertTrue(asta.dumpEquals(astb));
  }

  @Test
  public void testSetEnd() {
    fail("Not yet implemented");
  }

  @Test
  public void testSetStart() {
    fail("Not yet implemented");
  }

  @Test
  public void testToString() {
    fail("Not yet implemented");
  }

  @Test
  public void testClone() {
    fail("Not yet implemented");
  }

}

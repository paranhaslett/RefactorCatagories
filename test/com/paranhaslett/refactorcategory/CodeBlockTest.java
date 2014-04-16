package com.paranhaslett.refactorcategory;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.Side;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import test.Tools;
import AST.ASTNode;
import AST.CompilationUnit;

import com.paranhaslett.refactorcategory.ast.Ast;
import com.paranhaslett.refactorcategory.file.FileEntry;
import com.paranhaslett.refactorcategory.file.FileRevision;
import com.paranhaslett.refactorcategory.git.GitEntry;
import com.paranhaslett.refactorcategory.git.GitRevision;
import com.paranhaslett.refactorcategory.model.Entry;
import com.paranhaslett.refactorcategory.model.Revision;

public class CodeBlockTest {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testHashCode() {
    fail("Not yet implemented");
  }

  @Test
  public void testClone() {
    fail("Not yet implemented");
  }

  @Test
  public void testDumpEquals() {
    fail("Not yet implemented");
  }

  @Test
  public void testGetAst() {
    fail("Not yet implemented");
  }

  @Test
  public void testGetBlock() {
    fail("Not yet implemented");
  }

  @Test
  public void testGetBlocks() {
    fail("Not yet implemented");
  }

  @Test
  public void testGetEntry() {
    fail("Not yet implemented");
  }

  @Test
  public void testGetRevision() {
    fail("Not yet implemented");
  }

  @Test
  public void testSetAst() {
    fail("Not yet implemented");
  }

  @Test
  public void testSetBlock() {
    fail("Not yet implemented");
  }

  @Test
  public void testSetBlocks() {
    fail("Not yet implemented");
  }

  @Test
  public void testSetEntry() {
    fail("Not yet implemented");
  }

  @Test
  public void testSetRevision() {
    fail("Not yet implemented");
  }

  @Test
  public void testGetRawText() {
    Revision fileRevision = new FileRevision();
    fileRevision.setProgram();
    Entry fileEntry = new FileEntry("/home/paran/Main.java");
    CodeBlock cb = new CodeBlock();
    cb.setRevision(fileRevision);
    cb.setEntry(fileEntry);
    try {
      fileEntry.open();

      Ast ast = fileEntry.getCompilationUnit(fileRevision,
          "/home/paran/Main.java");// Tools.setupProgram("/home/paran/Main.java");
      cb.setAst(ast);
      cb.setBlock(ast.getRange());
      testAllChildren(cb);
    } catch (IOException iox) {
      // this should not occur for file based as it is already handled within
      // fileEntry.getCompilationUnit();
      iox.printStackTrace();
    }
  }

  private void testAllChildren(CodeBlock cb) {
    if (cb.getBlock().getStart()<1 || cb.getBlock().getEnd()<1){
      return;
    }
    List<Ast> children = cb.getAst().getChildren();
    System.out.println("========================");
    System.out.println(cb.getAst().prettyPrint());
    System.out.println("------------------------");
    System.out.println(cb.getRawText());
    
    if (cb.getAst().prettyPrint() != null){
      whatIsDifferent(cb.getAst().prettyPrint(), cb.getRawText());
    
      assertEquals(cb.getAst().prettyPrint(), cb.getRawText());
    }
    for (Ast child : children) {
      try {
        CodeBlock chcb = (CodeBlock) cb.clone();
        chcb.setAst(child);
        chcb.setBlock(child.getRange());
        System.out.println("Range" + child.getRange());
        testAllChildren(chcb);
      } catch (CloneNotSupportedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }
  
  private boolean whatIsDifferent(String stra, String strb){
    boolean result = false;
    
   
    if(stra.length() !=strb.length()){
      System.out.println("Different length");
      System.out.println(stra.length());
      System.out.println(strb.length());
      for (int i = strb.length()-1; i<stra.length(); i++){
        System.out.print( stra.charAt(i));
      }
    } else {
      for (int i = 0; i<strb.length(); i++){
        if (!stra.substring(i, i+1).equals(strb.substring(i,i+1))){
          System.out.print(stra.charAt(i) + " not " + strb.charAt(i));
          result = true;
        } else {
          System.out.print(stra.charAt(i) + "" + strb.charAt(i));
        }
      }
    }
    System.out.println(result);
    return result;
  }

  @Test
  public void testToString() {
    fail("Not yet implemented");
  }

}

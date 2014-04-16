package com.paranhaslett.refactorcategory;

import AST.ASTNode;

import com.paranhaslett.refactorcategory.ast.Ast;
import com.paranhaslett.refactorcategory.model.Entry;
import com.paranhaslett.refactorcategory.model.Revision;

public class CodeBlock implements Cloneable {
  private Ast ast;
  private Range<Long> block;
  private Ranges<Long> blocks;

  private Entry entry;
  private Revision revision;

  @Override
  public Object clone() throws CloneNotSupportedException {
    CodeBlock clone = (CodeBlock) super.clone();
    if (block != null) {
      clone.block = (Range<Long>) (block.clone());
    }
    if (ast != null) {
      clone.ast = (Ast) (ast.clone());
    }
    return super.clone();
  }

  public boolean dumpEquals(CodeBlock cb) {
    if (ast != null && cb != null && cb.ast != null) {
      // It is a java segment so do am AST comparison
      return cb.ast.dumpEquals(ast);
    } else {
      // It is a text or comment segment so do a RawText comparison
      return getRawText().equals(cb.getRawText());
    }
  }

  public Ast getAst() {
    return ast;
  }

  public Range<Long> getBlock() {
    return block;
  }

  public Ranges<Long> getBlocks() {
    return blocks;
  }

  public Entry getEntry() {
    return entry;
  }

  public Revision getRevision() {
    return revision;
  }

  @Override
  public int hashCode() {
    // TODO Auto-generated method stub
    return super.hashCode();
  }

  public void setAst(Ast ast) {
    this.ast = ast;
  }

  public void setBlock(Range<Long> block) {
    this.block = block;
  }

  public void setBlocks(Ranges<Long> blocks) {
    this.blocks = blocks;
  }

  public void setEntry(Entry entry) {
    this.entry = entry;
  }

  public void setRevision(Revision revision) {
    this.revision = revision;
  }
  
  public String getRawText() {
    int startLine = ASTNode.getLine(block.getStart().intValue()) - 1;
    int endLine = ASTNode.getLine(block.getEnd().intValue());
    int startColumn = ASTNode.getColumn(block.getStart().intValue())-1;
    int endColumn = ASTNode.getColumn(block.getEnd().intValue());

    //System.out.println("[" + startLine + ":" + startColumn + " - " + endLine
    //    + ":" + endColumn + "]");
    String lines = entry.getRawText().getString(startLine, endLine, false);
    //System.out.println(lines.intern());
    int startOfLastRow = lines.length();
    if (lines.endsWith("\n")) {
      startOfLastRow -= 2;
      //lines = lines.substring(0, lines.length() - 1);
    }
    startOfLastRow = lines.lastIndexOf('\n', startOfLastRow) + 1;
    if (startOfLastRow == -1) {
      startOfLastRow = 0;
    }
    String substring = null;
    try {
      substring = lines.substring(startColumn, startOfLastRow + endColumn);
    } catch (StringIndexOutOfBoundsException sioobe) {
      sioobe.printStackTrace();
    }
    return substring;
  } 

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    if (revision != null) {
      sb.append(revision.getName().substring(0, 10));
      sb.append(":");
      if (entry != null) {
        String path = entry.getPath();
        int index = path.lastIndexOf("/");
        sb.append(path.substring(index));
        sb.append(":");
        if (block != null) {
          sb.append(block);
        }
      }
    }
    return sb.toString();
  }
}

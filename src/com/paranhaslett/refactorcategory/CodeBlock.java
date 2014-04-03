package com.paranhaslett.refactorcategory;

import com.paranhaslett.refactorcategory.ast.Ast;
import com.paranhaslett.refactorcategory.model.Entry;
import com.paranhaslett.refactorcategory.model.Revision;

public class CodeBlock implements Cloneable {
  Ast ast;
  Range<Long> block;
  Entry entry;
  Revision revision;

  public Ast getAst() {
    return ast;
  }

  public Range<Long> getBlock() {
    return block;
  }

  public Entry getEntry() {
    return entry;
  }

  public Revision getRevision() {
    return revision;
  }

  public void setAst(Ast ast) {
    this.ast = ast;
  }

  public void setBlock(Range<Long> block) {
    this.block = block;
  }

  public void setEntry(Entry entry) {
    this.entry = entry;
  }

  public void setRevision(Revision revision) {
    this.revision = revision;
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    CodeBlock clone = (CodeBlock) super.clone();
    if (block != null) {
      clone.block = new Range<Long>(block);
    }
    clone.ast = new Ast();
    return super.clone();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof CodeBlock) {
      CodeBlock cb = (CodeBlock) obj;
      if (ast != null) {
        // It is a java segment so do am AST comparison
        return cb.ast.equals(ast);
      } else {
        // It is a text or comment segment so do a RawText comparison
        return entry.getRawText(block).equals(cb.entry.getRawText(cb.block));
      }
    }
    return false;
  }

  @Override
  public int hashCode() {
    // TODO Auto-generated method stub
    return super.hashCode();
  }

  @Override
  public String toString() {
    // TODO Auto-generated method stub
    return super.toString();
  }
}

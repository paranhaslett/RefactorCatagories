package com.paranhaslett.refactorcategory.compare;

import java.util.List;

import org.eclipse.jgit.diff.Sequence;

import com.paranhaslett.refactorcategory.CodeBlock;


public class CodeBlockSequence extends Sequence {
  List<CodeBlock> children;
  

  public CodeBlockSequence(List<CodeBlock> children) {
   this.children = children;
  }

  @Override
  public int size() {
    return children.size();
  }

}

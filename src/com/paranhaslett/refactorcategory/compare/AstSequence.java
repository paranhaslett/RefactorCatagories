package com.paranhaslett.refactorcategory.compare;

import java.util.List;

import org.eclipse.jgit.diff.Sequence;

import com.paranhaslett.refactorcategory.ast.Ast;

public class AstSequence extends Sequence {
  List<Ast> children;

  public AstSequence(List<Ast> children) {
    this.children = children;
  }

  @Override
  public int size() {
    return children.size();
  }

}

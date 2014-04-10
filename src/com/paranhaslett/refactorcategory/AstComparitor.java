package com.paranhaslett.refactorcategory;

import org.eclipse.jgit.diff.SequenceComparator;

import com.paranhaslett.refactorcategory.ast.Ast;

public abstract class  AstComparitor extends SequenceComparator<AstSequence> {
  public static final AstComparitor DEFAULT = new AstComparitor() {

    @Override
    public boolean equals(AstSequence a, int ai, AstSequence b,
        int bi) {
      Ast asta = a.children.get(ai);
      Ast astb = b.children.get(bi);
      return asta.dumpEquals(astb);
    }

    @Override
    public int hash(AstSequence seq, int ptr) {
      return seq.children.get(ptr).hashCode();
    }

  };
}

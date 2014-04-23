package com.paranhaslett.refactorcategory.compare;

import org.eclipse.jgit.diff.SequenceComparator;

import com.paranhaslett.refactorcategory.ast.Ast;

public abstract class  AstComparitor extends SequenceComparator<AstSequence> {
  public static final AstComparitor DEFAULT = new AstComparitor() {

    @Override
    public boolean equals(AstSequence a, int ai, AstSequence b,
        int bi) {
      Ast asta = a.children.get(ai);
      Ast astb = b.children.get(bi);
      //TODO get a better comparison
      return asta.equalTypes(astb) && asta.dumpEquals(astb);
    }

    @Override
    public int hash(AstSequence seq, int ptr) {
      return seq.children.get(ptr).hashCode();
    }

  };
}

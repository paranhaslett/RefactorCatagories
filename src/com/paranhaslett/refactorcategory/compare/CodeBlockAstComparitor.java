package com.paranhaslett.refactorcategory.compare;

import org.eclipse.jgit.diff.SequenceComparator;

import com.paranhaslett.refactorcategory.CodeBlock;

public abstract class CodeBlockAstComparitor extends
    SequenceComparator<CodeBlockAstSequence> {

  public static final CodeBlockAstComparitor DEFAULT = new CodeBlockAstComparitor() {

    @Override
    public boolean equals(CodeBlockAstSequence a, int ai, CodeBlockAstSequence b,
        int bi) {
      CodeBlock cba = a.children.get(ai);
      CodeBlock cbb = b.children.get(bi);
      return cba.dumpEquals(cbb);
    }

    @Override
    public int hash(CodeBlockAstSequence seq, int ptr) {
      return seq.children.get(ptr).hashCode();
    }

  };

}
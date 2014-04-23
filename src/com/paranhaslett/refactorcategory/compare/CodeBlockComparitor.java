package com.paranhaslett.refactorcategory.compare;

import org.eclipse.jgit.diff.SequenceComparator;

import com.paranhaslett.refactorcategory.CodeBlock;

public abstract class CodeBlockComparitor extends
    SequenceComparator<CodeBlockSequence> {

  public static final CodeBlockComparitor DEFAULT = new CodeBlockComparitor() {

    @Override
    public boolean equals(CodeBlockSequence a, int ai, CodeBlockSequence b,
        int bi) {
      CodeBlock cba = a.children.get(ai);
      CodeBlock cbb = b.children.get(bi);
      return cba.dumpEquals(cbb);
    }

    @Override
    public int hash(CodeBlockSequence seq, int ptr) {
      return seq.children.get(ptr).hashCode();
    }

  };

}
package com.paranhaslett.refactorcategory.compare;

import org.eclipse.jgit.diff.SequenceComparator;

import com.paranhaslett.refactorcategory.CodeBlock;

public abstract class CodeBlockTextComparitor extends
    SequenceComparator<CodeBlockTextSequence> {

  public static final CodeBlockTextComparitor DEFAULT = new CodeBlockTextComparitor() {

    @Override
    public boolean equals(CodeBlockTextSequence a, int ai, CodeBlockTextSequence b,
        int bi) {
      CodeBlock cba = a.children.get(ai);
      CodeBlock cbb = b.children.get(bi);
      return cba.getRawText().equals(cbb.getRawText());
    }

    @Override
    public int hash(CodeBlockTextSequence seq, int ptr) {
      return seq.children.get(ptr).hashCode();
    }

  };

}
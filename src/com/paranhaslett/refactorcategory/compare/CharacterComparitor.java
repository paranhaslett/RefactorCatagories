package com.paranhaslett.refactorcategory.compare;

import org.eclipse.jgit.diff.SequenceComparator;

public abstract class  CharacterComparitor extends SequenceComparator<CharacterSequence> {
  public static final CharacterComparitor DEFAULT = new CharacterComparitor() {

    @Override
    public boolean equals(CharacterSequence a, int ai, CharacterSequence b,
        int bi) {
      char charA = a.children[ai];
      char charB = b.children[bi];
      if (Character.isWhitespace(charA) && Character.isWhitespace(charB)){
        return true;
      }
      return charA == charB;
    }

    @Override
    public int hash(CharacterSequence seq, int ptr) {
      return (int)seq.children[ptr];
    }

  };
}

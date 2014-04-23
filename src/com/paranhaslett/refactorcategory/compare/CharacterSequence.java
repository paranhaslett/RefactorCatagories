package com.paranhaslett.refactorcategory.compare;

import org.eclipse.jgit.diff.Sequence;

public class CharacterSequence extends Sequence {
  char[] children;

  public CharacterSequence(String children) {
    this.children = children.toCharArray();
  }

  @Override
  public int size() {
    return children.length;
  }

  public char get(int index) {
    return children[index];
  }

}

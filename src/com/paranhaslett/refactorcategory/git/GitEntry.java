package com.paranhaslett.refactorcategory.git;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.internal.JGitText;
import org.eclipse.jgit.lib.AbbreviatedObjectId;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.MutableObjectId;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.treewalk.TreeWalk;

import com.paranhaslett.refactorcategory.CodeBlock;
import com.paranhaslett.refactorcategory.Difference;
import com.paranhaslett.refactorcategory.Difference.Type;

public class GitEntry extends DiffEntry {

  /** Magical SHA1 used for file adds or deletes */
  static final AbbreviatedObjectId A_ZERO = AbbreviatedObjectId
      .fromObjectId(ObjectId.zeroId());

  private GitFileEntry newSide;
  private GitFileEntry oldSide;

  public GitFileEntry getNewSide() {
    return newSide;
  }

  public void setNewSide(GitFileEntry newSide) {
    this.newSide = newSide;
  }

  public GitFileEntry getOldSide() {
    return oldSide;
  }

  public void setOldSide(GitFileEntry oldSide) {
    this.oldSide = oldSide;
  }

  public static List<Difference> scans(TreeWalk walk) throws IOException {
    if (walk.getTreeCount() != 2)
      throw new IllegalArgumentException(
          JGitText.get().treeWalkMustHaveExactlyTwoTrees);

    List<Difference> r = new ArrayList<Difference>();
    MutableObjectId idBuf = new MutableObjectId();
    while (walk.next()) {
      Difference difference = new Difference();
      GitEntry entry = new GitEntry();
      entry.newSide = new GitFileEntry();
      entry.oldSide = new GitFileEntry();
      entry.newSide.setSide(Side.NEW);
      entry.oldSide.setSide(Side.OLD);

      CodeBlock newCb = new CodeBlock();
      CodeBlock oldCb = new CodeBlock();
      newCb.setEntry(entry.newSide);
      oldCb.setEntry(entry.oldSide);

      difference.setNewCb(newCb);
      difference.setOldCb(oldCb);

      walk.getObjectId(idBuf, 0);
      entry.oldId = AbbreviatedObjectId.fromObjectId(idBuf);

      walk.getObjectId(idBuf, 1);
      entry.newId = AbbreviatedObjectId.fromObjectId(idBuf);

      entry.oldMode = walk.getFileMode(0);
      entry.newMode = walk.getFileMode(1);
      entry.newPath = entry.oldPath = walk.getPathString();

      if (entry.oldMode == FileMode.MISSING) {
        entry.oldPath = DiffEntry.DEV_NULL;
        entry.changeType = ChangeType.ADD;
        difference.setType(Type.INSERT);
        r.add(difference);
      } else if (entry.newMode == FileMode.MISSING) {
        entry.newPath = DiffEntry.DEV_NULL;
        entry.changeType = ChangeType.DELETE;
        difference.setType(Type.DELETE);
        r.add(difference);
      } else if (!entry.oldId.equals(entry.newId)) {
        entry.changeType = ChangeType.MODIFY;
        difference.setType(Type.MODIFY);
        r.add(difference);

      } else if (entry.oldMode != entry.newMode) {
        entry.changeType = ChangeType.MODIFY;
        difference.setType(Type.MODIFY);
        r.add(difference);
      }

      if (walk.isSubtree()) {
        walk.enterSubtree();
      }
    }
    return r;
  }

  public void setNewId(AbbreviatedObjectId id) {
    newId = id;
  }

  public void setOldId(AbbreviatedObjectId id) {
    oldId = id;
  }

}

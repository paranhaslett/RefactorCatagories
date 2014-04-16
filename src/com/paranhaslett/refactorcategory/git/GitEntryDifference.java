package com.paranhaslett.refactorcategory.git;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.diff.ContentSource;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.Side;
import org.eclipse.jgit.diff.RenameDetector;
import org.eclipse.jgit.internal.JGitText;
import org.eclipse.jgit.lib.AbbreviatedObjectId;
import org.eclipse.jgit.lib.NullProgressMonitor;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.WorkingTreeIterator;

import com.paranhaslett.refactorcategory.Difference;
import com.paranhaslett.refactorcategory.Difference.Type;
import com.paranhaslett.refactorcategory.model.Entry;

public class GitEntryDifference {
  /** Magic return content indicating it is empty or no content present. */
  static final byte[] EMPTY = new byte[] {};

  /** Magic return indicating the content is binary. */
  static final byte[] BINARY = new byte[] {};

  /** Magical SHA1 used for file adds or deletes */
  static final AbbreviatedObjectId A_ZERO = AbbreviatedObjectId
      .fromObjectId(ObjectId.zeroId());

  private RenameDetector renameDetector;

  public GitEntryDifference() {
    super();
  }

  public static List<Difference> getEntries(Difference difference)
      throws IOException {

    ((GitRepo) GitRepo.getRepo()).makeReader();

    CanonicalTreeParser oldCtp = ((GitRevision) difference.getOldCb()
        .getRevision()).getCtp(((GitRepo) GitRepo.getRepo()).reader);
    CanonicalTreeParser newCtp = ((GitRevision) difference.getNewCb()
        .getRevision()).getCtp(((GitRepo) GitRepo.getRepo()).reader);

    TreeWalk walk = new TreeWalk(((GitRepo) GitRepo.getRepo()).reader);
    walk.addTree(oldCtp);
    walk.addTree(newCtp);
    walk.setRecursive(true);

    ((GitRepo) GitRepo.getRepo()).source = new ContentSource.Pair(
        source(oldCtp), source(newCtp));

    if (walk.getTreeCount() != 2)
      throw new IllegalArgumentException(
          JGitText.get().treeWalkMustHaveExactlyTwoTrees);

    List<DiffEntry> files = DiffEntry.scan(walk);

    GitEntryDifference gbe = new GitEntryDifference();
    gbe.setDetectRenames(true);
    files = gbe.detectRenames(files);

    // convert diffEntry into Differences
    List<Difference> results = new ArrayList<Difference>();
    for (DiffEntry ent : files) {
      Entry oldEnt = new GitEntry(ent.getOldId(),
          ent.getOldPath(), Side.OLD, ent);
      Entry newEnt = new GitEntry(ent.getNewId(),
          ent.getNewPath(), Side.NEW, ent);
      try {
        Difference diff = (Difference) difference.clone();
        switch (ent.getChangeType()) {
        case ADD:
          diff.setType(Type.INSERT);
          break;
        case DELETE:
          diff.setType(Type.DELETE);
          break;
        case MODIFY:
          diff.setType(Type.MODIFY);
          break;
        case RENAME:
          diff.setType(Type.RENAMED);
          break;
        case COPY:
          diff.setType(Type.COPY);
          break;
        }

        diff.getNewCb().setEntry(newEnt);
        diff.getOldCb().setEntry(oldEnt);
        results.add(diff);
      } catch (CloneNotSupportedException e) {
        // This should not happen as clone of Difference and CodeBlock are valid
        e.printStackTrace();
      }
    }

    return results;

  }

  private static ContentSource source(AbstractTreeIterator iterator) {
    if (iterator instanceof WorkingTreeIterator) {
      return ContentSource.create((WorkingTreeIterator) iterator);
    }
    return ContentSource.create(((GitRepo) GitRepo.getRepo()).reader);
  }

  public void setDetectRenames(boolean on) {
    if (on && renameDetector == null) {
      renameDetector = new RenameDetector(
          ((GitRepo) GitRepo.getRepo()).repository);
    } else if (!on)
      renameDetector = null;
  }

  /** @return the rename detector if rename detection is enabled. */
  public RenameDetector getRenameDetector() {
    return renameDetector;
  }

  /** @return true if rename detection is enabled. */
  public boolean isDetectRenames() {
    return renameDetector != null;
  }

  private List<DiffEntry> detectRenames(List<DiffEntry> files)
      throws IOException {
    renameDetector.reset();
    renameDetector.addAll(files);
    return renameDetector.compute(((GitRepo) GitRepo.getRepo()).reader,
        NullProgressMonitor.INSTANCE);
  }

}

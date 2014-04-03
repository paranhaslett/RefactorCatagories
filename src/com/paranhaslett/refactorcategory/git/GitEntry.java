package com.paranhaslett.refactorcategory.git;

import java.io.IOException;
import java.util.Collection;

import org.eclipse.jgit.diff.DiffEntry.Side;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.LargeObjectException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.AbbreviatedObjectId;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.MutableObjectId;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.storage.pack.PackConfig;
import org.eclipse.jgit.treewalk.TreeWalk;

import com.paranhaslett.refactorcategory.Range;
import com.paranhaslett.refactorcategory.model.Entry;

public class GitEntry implements Entry {

  GitBothEntries bothEntries;
  String path;
  RawText rawText;
  AbbreviatedObjectId id;
  FileMode mode;
  Side side;

  public AbbreviatedObjectId getId() {
    return id;
  }

  public void setId(AbbreviatedObjectId id) {
    this.id = id;
  }

  public GitBothEntries getBoth() {
    return bothEntries;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  @Override
  public String getRawText(Range<Long> range) {
    // TODO Auto-generated method stub
    return null;
  }

  public void open() throws MissingObjectException, IOException {

    if (mode == FileMode.MISSING || mode.getObjectType() != Constants.OBJ_BLOB) {

    } else {

      if (!id.isComplete()) {
        Collection<ObjectId> ids = ((GitRepo) GitRepo.getRepo()).reader
            .resolve(id);
        if (ids.size() == 1) {
          id = AbbreviatedObjectId.fromObjectId(ids.iterator().next());
        } else if (ids.size() == 0)
          throw new MissingObjectException(id, Constants.OBJ_BLOB);
        else
          throw new AmbiguousObjectException(id, ids);
      }

      try {
        ObjectLoader ldr = ((GitRepo) GitRepo.getRepo()).source.open(side,
            bothEntries);

        byte[] bytes = ldr.getBytes(PackConfig.DEFAULT_BIG_FILE_THRESHOLD);
        rawText = new RawText(bytes);

      } catch (LargeObjectException.ExceedsLimit overLimit) {
        rawText = new RawText(GitBothEntries.BINARY);

      } catch (LargeObjectException.ExceedsByteArrayLimit overLimit) {
        rawText = new RawText(GitBothEntries.BINARY);

      } catch (LargeObjectException.OutOfMemory tooBig) {
        rawText = new RawText(GitBothEntries.BINARY);

      } catch (LargeObjectException tooBig) {
        tooBig.setObjectId(id.toObjectId());
        throw tooBig;
      }
    }
  }

  public void setup(TreeWalk walk, int sideNum, GitBothEntries bothEnt,
      MutableObjectId idBuf) {
    walk.getObjectId(idBuf, sideNum);
    id = AbbreviatedObjectId.fromObjectId(idBuf);
    mode = walk.getFileMode(sideNum);
    path = walk.getPathString();
    switch (sideNum) {
    case 0:
      side = Side.OLD;
      break;
    case 1:
      side = Side.NEW;
      break;
    }
    this.bothEntries = bothEnt;
  }

  @Override
  public RawText getRawText() {
    return rawText;
  }
}

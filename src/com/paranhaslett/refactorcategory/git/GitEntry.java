package com.paranhaslett.refactorcategory.git;

import java.io.IOException;
import java.util.Collection;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.Side;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.LargeObjectException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.AbbreviatedObjectId;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.storage.pack.PackConfig;

import AST.ASTNode;

import com.paranhaslett.refactorcategory.Range;
import com.paranhaslett.refactorcategory.ast.Ast;
import com.paranhaslett.refactorcategory.ast.MyProgram;
import com.paranhaslett.refactorcategory.model.Entry;
import com.paranhaslett.refactorcategory.model.Revision;

public class GitEntry implements Entry {
  private AbbreviatedObjectId id;
  private String path;
  private RawText rawText;
  private byte[] content;
  private Side side;
  private DiffEntry diffEntry;
  
  

  public GitEntry(AbbreviatedObjectId id, String path,
      Side side, DiffEntry diffEntry) {
    this.id = id;
    this.path = path;
    this.side = side;
    this.diffEntry = diffEntry;
  }

  public AbbreviatedObjectId getId() {
    return id;
  }

  public String getPath() {
    return path;
  }

  @Override
  public RawText getRawText() {
    return rawText;
  }

  @Override
  public RawText getRawText(Range<Long> range) {
    int startLine = ASTNode.getLine(range.getStart().intValue())-1;
    int endLine = ASTNode.getLine(range.getEnd().intValue());
    int startColumn = ASTNode.getColumn(range.getStart().intValue()-1);
    int endColumn = ASTNode.getColumn(range.getEnd().intValue());
    String lines = rawText.getString(startLine, endLine, false);
    int lastindex = lines.lastIndexOf('\n', lines.length());
    if (lastindex != -1) {
      lastindex = lines.lastIndexOf('\n', lastindex -1);
    }
    if (lastindex == -1) {
      lastindex = 0;
    }
    String substring = lines.substring(startColumn, lastindex + endColumn);
    RawText result = new RawText(substring.getBytes());
    return result;
  }

  public void open() throws MissingObjectException, IOException {

    if (diffEntry.getMode(side) == FileMode.MISSING 
        || diffEntry.getMode(side).getObjectType() != Constants.OBJ_BLOB) {
      content = GitEntryDifference.EMPTY;
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
            diffEntry);

        content = ldr.getBytes(PackConfig.DEFAULT_BIG_FILE_THRESHOLD);
        rawText = new RawText(content);

      } catch (LargeObjectException.ExceedsLimit
          | LargeObjectException.ExceedsByteArrayLimit
          | LargeObjectException.OutOfMemory overLimit) {
        content = GitEntryDifference.BINARY;

      } catch (LargeObjectException tooBig) {
        tooBig.setObjectId(id.toObjectId());
        throw tooBig;
      }
    }

    rawText = new RawText(content);
  }

  public void setId(AbbreviatedObjectId id) {
    this.id = id;
  }

  public void setPath(String path) {
    this.path = path;
  }

  @Override
  public Ast getCompilationUnit(Revision revision, String name) {
    GitRevision gitRevision = (GitRevision) revision;
    MyProgram myProg = gitRevision.getProgram();
    //myProg.state().reset();
    return new Ast(myProg.getCompilationUnit(name, content));
  }

  @Override
  public byte[] getContent() {
    return content;
  }

}

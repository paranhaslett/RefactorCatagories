package com.paranhaslett.refactorcategory.git;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.eclipse.jgit.diff.ContentSource;
import org.eclipse.jgit.diff.DiffEntry.Side;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.LargeObjectException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.AbbreviatedObjectId;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.storage.pack.PackConfig;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.WorkingTreeIterator;

import com.paranhaslett.refactorcategory.Difference;
import com.paranhaslett.refactorcategory.model.FileEntry;

public class GitHelper {
  
  /** Magic return content indicating it is empty or no content present. */
  private static final byte[] EMPTY = new byte[] {};

  /** Magic return indicating the content is binary. */
  private static final byte[] BINARY = new byte[] {};

  private static GitHelper gitHelper=null;
  private ObjectReader reader;
  private ContentSource.Pair source;
  
  public ObjectReader getReader() {
    return reader;
  }

  public void setReader(ObjectReader reader) {
    this.reader = reader;
  }

  private GitHelper(){};
  
  public static GitHelper getGitHelper(){
   if (gitHelper == null){
     gitHelper = new GitHelper();
   }
   return gitHelper;
    
  }
  
  public byte[] open(FileEntry fileentry) throws MissingObjectException, IOException{
    GitFileEntry entry = (GitFileEntry)fileentry;
    Side side = entry.getSide();
    GitEntry gent = entry.getGitEntry();
    
    if (gent.getMode(side) == FileMode.MISSING)
      return EMPTY;

  if (gent.getMode(side).getObjectType() != Constants.OBJ_BLOB)
      return EMPTY;

  AbbreviatedObjectId id = gent.getId(side);
  if (!id.isComplete()) {
      Collection<ObjectId> ids = reader.resolve(id);
      if (ids.size() == 1) {
          id = AbbreviatedObjectId.fromObjectId(ids.iterator().next());
          switch (entry.getSide()) {
          case OLD:
              gent.setOldId(id);
              break;
          case NEW:
              gent.setNewId(id);
              break;
          }
      } else if (ids.size() == 0)
          throw new MissingObjectException(id, Constants.OBJ_BLOB);
      else
          throw new AmbiguousObjectException(id, ids);
  }

  try {
      ObjectLoader ldr = source.open(side, gent);
      return ldr.getBytes(PackConfig.DEFAULT_BIG_FILE_THRESHOLD);

  } catch (LargeObjectException.ExceedsLimit overLimit) {
      return BINARY;

  } catch (LargeObjectException.ExceedsByteArrayLimit overLimit) {
      return BINARY;

  } catch (LargeObjectException.OutOfMemory tooBig) {
      return BINARY;

  } catch (LargeObjectException tooBig) {
      tooBig.setObjectId(id.toObjectId());
      throw tooBig;
  }
  }


  public void release(){
    if (reader != null)
      reader.release();
  }
  
  public List<Difference> scan(AbstractTreeIterator a, AbstractTreeIterator b)
      throws IOException {
 

  TreeWalk walk = new TreeWalk(reader);
  walk.addTree(a);
  walk.addTree(b);
  walk.setRecursive(true);


  source = new ContentSource.Pair(source(a), source(b));

  List<Difference> files = GitEntry.scans(walk);
 

  return files;
}
  
void setSource(AbstractTreeIterator a, AbstractTreeIterator b){
    
  }
 //com.paranhaslett.refactorcategory.GitHelper.setSource(Pair) setSource(Pair)



private ContentSource source(AbstractTreeIterator iterator) {
        if (iterator instanceof WorkingTreeIterator)
            return ContentSource.create((WorkingTreeIterator) iterator);
        return ContentSource.create(reader);
    }






}

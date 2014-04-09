package com.paranhaslett.refactorcategory.strategy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;

import com.paranhaslett.refactorcategory.Difference;
import com.paranhaslett.refactorcategory.Difference.Type;
import com.paranhaslett.refactorcategory.git.GitEntryDifference;
import com.paranhaslett.refactorcategory.git.GitRepo;
import com.paranhaslett.refactorcategory.model.Entry;

public class RevisionDrillDown implements DrillDown {

  @Override
  public List<Difference> drilldown(Difference difference) throws IOException,
      GitAPIException {

    List<Difference> result = new ArrayList<Difference>();

    System.out.println(difference.getOldCb().getRevision().getName() + " to "
        + difference.getNewCb().getRevision().getName());

    // Setup the both revisions as programs
    difference.getOldCb().getRevision().setProgram();
    difference.getNewCb().getRevision().setProgram();

    List<Difference> modify = new ArrayList<Difference>();
    List<Difference> inserts = new ArrayList<Difference>();
    List<Difference> deletes = new ArrayList<Difference>();

    try {
      List<Difference> filesDiff = GitEntryDifference.getEntries(difference);

      // Try all the modifies first then deal with the inserts and deletes
      // see if it is java equivalent
      for (Difference diff : filesDiff) {

        Entry oldEnt = diff.getOldCb().getEntry();
        Entry newEnt = diff.getNewCb().getEntry();

        if (oldEnt.getPath().endsWith(".class")
            || newEnt.getPath().endsWith(".class")) {
          diff.setType(Type.BINARY);
        }
        switch (diff.getType()) {
        case COPY:
        case RENAMED:
        case MODIFY:
          modify.addAll(new EntryDrillDown().drilldown(diff));
          break;
        case DELETE:
          diff.setScore(100);
          deletes.add(diff);
          break;
        case INSERT:
          diff.setScore(100);
          inserts.add(diff);
          break;
        default:
          result.add(diff);

        }
      }
      result.addAll(modify);
      /** Debug **/
      // result.addAll(deletes);
      // result.addAll(inserts);
      /** Debug ends */

      // split up the matchups into different file types
      List<String> filetypes = new ArrayList<String>();
      List<List<Difference>> insertdiffs = new ArrayList<List<Difference>>();
      List<List<Difference>> deletediffs = new ArrayList<List<Difference>>();
      for (Difference diff : inserts) {
        Entry ent = diff.getNewCb().getEntry();
        String pathExt = getPathExt(ent);
        if (filetypes.contains(pathExt)) {
          int index = filetypes.indexOf(pathExt);
          insertdiffs.get(index).add(diff);
        } else {
          filetypes.add(pathExt);
          List<Difference> newInsRow = new ArrayList<Difference>();
          List<Difference> newDelRow = new ArrayList<Difference>();
          newInsRow.add(diff);
          insertdiffs.add(newInsRow);
          deletediffs.add(newDelRow);
        }

      }

      for (Difference diff : deletes) {
        Entry ent = diff.getOldCb().getEntry();
        String pathExt = getPathExt(ent);
        if (filetypes.contains(pathExt)) {
          int index = filetypes.indexOf(pathExt);
          deletediffs.get(index).add(diff);
        } else {
          filetypes.add(pathExt);
          List<Difference> newInsRow = new ArrayList<Difference>();
          List<Difference> newDelRow = new ArrayList<Difference>();
          newDelRow.add(diff);
          insertdiffs.add(newInsRow);
          deletediffs.add(newDelRow);
        }
      }

      // match up all remaining adds and deletes in different files

      for (int i = 0; i < filetypes.size(); i++) {
        List<Difference> insMatchs = insertdiffs.get(i);
        List<Difference> delMatchs = deletediffs.get(i);
        // result.addAll(fdd.matchup(insMatchs, delMatchs));
      }

    } catch (IOException e) {
      throw new JGitInternalException(e.getMessage(), e);
    } finally {
      ((GitRepo) GitRepo.getRepo()).release();
    }
    return result;
  }

  private String getPathExt(Entry ent) {

    System.out.println(ent.getPath());
    String path = ent.getPath();
    int index = path.lastIndexOf('.');
    if (index == -1) {
      index = path.lastIndexOf('/');
    }
    if (index == -1) {
      index = 0;
    }
    String pathExt = path.substring(index, path.length());
    return pathExt;
  }

}

package com.paranhaslett.refactorcategory.strategy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;

import com.paranhaslett.refactorcategory.Difference;
import com.paranhaslett.refactorcategory.Difference.Type;
import com.paranhaslett.refactorcategory.ast.Ast;
import com.paranhaslett.refactorcategory.git.GitHelper;

public class RevisionDrillDown implements DrillDown {

  @Override
  public List<Difference> drilldown(Difference difference) throws IOException,
      GitAPIException {

    List<Difference> result = new ArrayList<Difference>();
    FileEntryDrillDown fdd = new FileEntryDrillDown();

    // Setup the both revisions as programs
    Ast oldAst = new Ast();
    oldAst.setProgram();
    difference.getOldCb().setAst(oldAst);

    Ast newAst = new Ast();
    newAst.setProgram();
    difference.getNewCb().setAst(newAst);

    try {

      List<Difference> filesDiff = difference.getOldCb().getRevision()
          .getFileEntries(difference);

      List<Difference> inserts = new ArrayList<Difference>();
      List<Difference> deletes = new ArrayList<Difference>();

      // Try all the modifies first then deal with the inserts and deletes
      // see if it is java equivalent
      for (Difference diff : filesDiff) {
        if(diff.getOldCb().getEntry().getPath().endsWith(".class")||diff.getNewCb().getEntry().getPath().endsWith(".class")){
          diff.setType(Type.BINARY);
        }
        System.out.println(diff.getOldCb().getEntry().getPath() + " to " + diff.getNewCb().getEntry().getPath() );
        switch (diff.getType()) {
        case MODIFY:      
          result.addAll(fdd.drilldown(diff));
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
      
      /** Debug **/
       //result.addAll(deletes);
      // result.addAll(inserts);
      /**Debug ends */
      
      
      //split up the matchups into different file types
      List<String> filetypes = new ArrayList<String>();
      List<List<Difference>> insertdiffs = new ArrayList<List<Difference>>();
      List<List<Difference>> deletediffs = new ArrayList<List<Difference>>();
      for (Difference diff : inserts) {
        String path = diff.getNewCb().getEntry().getPath();
        String pathExt = path.substring(path.lastIndexOf('.'), path.length());
        if (filetypes.contains(pathExt)){
          int index =filetypes.indexOf(pathExt);
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
        String path = diff.getOldCb().getEntry().getPath();
        String pathExt = path.substring(path.lastIndexOf('.'), path.length());
        if (filetypes.contains(pathExt)){
          int index =filetypes.indexOf(pathExt);
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
      
      for (int i=0; i<filetypes.size(); i++){
        List<Difference> insMatchs = insertdiffs.get(i);
        List<Difference> delMatchs = deletediffs.get(i);
        result.addAll(fdd.matchup(insMatchs, delMatchs));
      } 

    } catch (IOException e) {
      throw new JGitInternalException(e.getMessage(), e);
    } finally {
      GitHelper.getGitHelper().release();
    }
    return result;
  }

 
}

package com.paranhaslett.refactorcategory.strategy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.diff.RawText;

import com.paranhaslett.refactorcategory.Difference;
import com.paranhaslett.refactorcategory.Difference.Type;
import com.paranhaslett.refactorcategory.ast.Ast;
import com.paranhaslett.refactorcategory.git.GitRepo;
import com.paranhaslett.refactorcategory.model.Entry;

public class RevisionDrillDown implements DrillDown {

  @Override
  public List<Difference> drilldown(Difference difference) throws IOException,
      GitAPIException {
    System.out.println(difference.getOldCb().getRevision().getName() + " to " + difference.getNewCb().getRevision().getName() );
    

    List<Difference> result = new ArrayList<Difference>();
    TextDrillDown tdd = new TextDrillDown();

    // Setup the both revisions as programs
    Ast oldAst = new Ast();
    oldAst.setProgram();
    difference.getOldCb().setAst(oldAst);

    Ast newAst = new Ast();
    newAst.setProgram();
    difference.getNewCb().setAst(newAst);

    try {

      List<Difference> filesDiff = ((GitRepo)GitRepo.getRepo()).getCurrentRevision().getFileEntries();

      List<Difference> inserts = new ArrayList<Difference>();
      List<Difference> deletes = new ArrayList<Difference>();

      // Try all the modifies first then deal with the inserts and deletes
      // see if it is java equivalent
      for (Difference diff : filesDiff) {
        
        Entry oldEnt = diff.getOldCb().getEntry();
        Entry newEnt = diff.getNewCb().getEntry();
        System.out.println(oldEnt.getPath() + " to " + newEnt.getPath() );
        
        
        if(newEnt.getPath().endsWith(".class")||newEnt.getPath().endsWith(".class")){
          diff.setType(Type.BINARY);
        } else {
          newEnt.open();
          /*rawText = new RawText(bytes);
          if (path.endsWith(".java")){
            ast= new Ast();
            ast.getCompilationUnit(path, bytes);
          }*/
          oldEnt.open();
        }
        switch (diff.getType()) {
        case MODIFY:
          result.addAll(tdd.drilldown(diff));
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
        Entry oldEnt = diff.getOldCb().getEntry();
        Entry newEnt = diff.getNewCb().getEntry();
        System.out.println(oldEnt.getPath() + " insert " + newEnt.getPath() );
        
        String path = newEnt.getPath();
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
        Entry oldEnt = diff.getOldCb().getEntry();
        Entry newEnt = diff.getNewCb().getEntry();
        System.out.println(oldEnt.getPath() + " delete " + newEnt.getPath() );
        
        String path = oldEnt.getPath();
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
        //result.addAll(fdd.matchup(insMatchs, delMatchs));
      } 

    } catch (IOException e) {
      throw new JGitInternalException(e.getMessage(), e);
    } finally {
      ((GitRepo)GitRepo.getRepo()).release();
    }
    return result;
  }

  private String getPathExt(Entry ent) {
    
    System.out.println(ent.getPath());
    String path = ent.getPath();
    int index = path.lastIndexOf('.');
    if (index == -1){
      index = path.lastIndexOf('/');
    }
    if (index == -1){
      index = 0;
    }
    String pathExt = path.substring(index, path.length());
    return pathExt;
  }

 
}

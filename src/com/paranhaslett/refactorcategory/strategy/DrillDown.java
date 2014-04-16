package com.paranhaslett.refactorcategory.strategy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.MissingObjectException;

import com.paranhaslett.refactorcategory.Config;
import com.paranhaslett.refactorcategory.Difference;
import com.paranhaslett.refactorcategory.Difference.Type;

public abstract class DrillDown {
  
  /* Collate the results (see if they are all the same type)
   * if they are all equivalences make one
   * if there is a delete/insert/move remove all equivalences
   */ 
  List<Difference> collate(Difference thisDiff, List<Difference> drilldown) {
    List <Difference> results = new ArrayList <Difference>();
    for (Difference diff:drilldown){
      if (diff.getType()!= Type.EQUIVALENT){
        results.add(diff);
      }
    }
    if (results.size() == 0){
      results.add(thisDiff);
    }
    return results;
  }

  abstract List<Difference> drilldown(Difference difference) throws IOException, GitAPIException;
  
  List<Difference> getBestScores(List<List<Difference>> grid,
      List<Difference> inserts, List<Difference> deletes) {
    List<Difference> result = new ArrayList<Difference>();
    if (grid.size() == 0 || grid.get(0).size() == 0) {
      result.addAll(inserts);
      result.addAll(deletes);
      return result;
    }
    Difference best = null;
    int bestInsert = 0;
    int bestDelete = 0;
    for (int ins = 1; ins < grid.size(); ins++) {
      for (int del = 1; del < grid.get(ins).size(); del++) {
        Difference score = grid.get(ins).get(del);
        if (best == null || best.getScore() > score.getScore()) {
          best = score;
          bestInsert = ins;
          bestDelete = del;
        }
      }
    }
    if (best != null) {
      if (best.getScore() >= Config.scoreUnit) {
        result.addAll(inserts);
        result.addAll(deletes);
        return result;
      }

      grid.remove(bestInsert);
      inserts.remove(bestInsert);
      deletes.remove(bestDelete);
      for (int ins = 0; ins < grid.size(); ins++) {
        grid.get(ins).remove(bestDelete);
      }
      if (grid.size() > 0 && grid.get(0).size() > 0) {
        result.addAll(getBestScores(grid, inserts, deletes));
      }
      result.add(best);
    }
    return result;
  }
  
  List<Difference> matchup(List<Difference> inserts, List<Difference> deletes) throws IOException, GitAPIException {    
    List<List<Difference>> grid = new ArrayList<List<Difference>>();

    // find all potential matches for deletes and inserts
    for (Difference insert:inserts) {
      List<Difference> rows = new ArrayList<Difference>();

      for (Difference delete:deletes) {
        Difference diff = createDiff(insert, Type.MOVE, 0.0);
        diff.setNewCb(insert.getNewCb());
        diff.setOldCb(delete.getOldCb());

        List<Difference> scoreList = drilldown(diff);
        double score = 0.0;
        if (scoreList.size() > 0) {
          for (Difference scoreDiff : scoreList) {
            score += scoreDiff.getScore();
          }
          score = score / scoreList.size();
        }
        diff.setScore(score);

        rows.add(diff);

      }

      grid.add(rows);
    }
    return getBestScores(grid, inserts, deletes);
  }

  Difference createDiff(Difference difference, Type type, double score) {
    Difference diff = null;
    try {
      diff = (Difference) difference.clone();
      diff.setType(type);
      diff.setScore(score);
    } catch (CloneNotSupportedException e) {
      // This should not happen as clone of Difference and CodeBlock are valid
      e.printStackTrace();
    }
    return diff;
  }
  
  List<Difference> matchup(List<Difference> differences)
      throws MissingObjectException, IOException, GitAPIException {

    // split up differences according to type
    List<Difference> inserts = new ArrayList<Difference>();
    List<Difference> deletes = new ArrayList<Difference>();

    for (Difference diff : differences) {
      if (diff.getType() == Type.INSERT) {
        inserts.add(diff);
      } else {
        if (diff.getType() == Type.DELETE) {
          deletes.add(diff);
        }
      }
    }
   
    return matchup(inserts, deletes);
  }
  
  List<Difference> filter(List<Difference> differences, Type type) {
    // split up differences according to type
    List<Difference> results = new ArrayList<Difference>();
    for (Difference diff : differences) {
      if (diff.getType() == type) {
        results.add(diff);
      } 
    }
    return results;
  }
}

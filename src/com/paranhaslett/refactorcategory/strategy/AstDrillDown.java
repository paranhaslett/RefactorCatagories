package com.paranhaslett.refactorcategory.strategy;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.diff.DiffAlgorithm;
import org.eclipse.jgit.diff.DiffAlgorithm.SupportedAlgorithm;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;

import com.paranhaslett.refactorcategory.AstComparitor;
import com.paranhaslett.refactorcategory.AstSequence;
import com.paranhaslett.refactorcategory.CodeBlock;
import com.paranhaslett.refactorcategory.Difference;
import com.paranhaslett.refactorcategory.Difference.Type;
import com.paranhaslett.refactorcategory.Range;
import com.paranhaslett.refactorcategory.ast.Ast;

public class AstDrillDown implements DrillDown {

  @Override
  public List<Difference> drilldown(Difference difference) {
    List<Difference> result = new ArrayList<Difference>();

    CodeBlock oldCb = difference.getOldCb();
    CodeBlock newCb = difference.getNewCb();

    Ast oldAst = oldCb.getAst();
    Ast newAst = newCb.getAst();

    if (!newAst.dumpEquals(oldAst)) {
      try {
        Difference diff = (Difference) difference.clone();
        diff.getNewCb().setAst(newAst);
        diff.getOldCb().setAst(oldAst);
        diff.setType(Type.MODIFY);
        result.add(diff);
        return result;
      } catch (CloneNotSupportedException e) {
        e.printStackTrace();
      }
    }

    // get all the children
    List<Ast> oldChildren = oldAst.getChildren();
    List<Ast> newChildren = newAst.getChildren();

    if (oldChildren.size() == 0) {
      if (newChildren.size() == 0) {
        Difference diff = createDiff(difference, oldAst, newAst,
            Type.EQUIVALENT);
        result.add(diff);
      } else {
        Difference diff = createDiff(difference, oldAst, newAst, Type.INSERT);
        result.add(diff);
      }
    } else {
      if (newChildren.size() == 0) {
        Difference diff = createDiff(difference, oldAst, newAst, Type.DELETE);
        result.add(diff);
      } else {
        return rearrangeChildren(oldChildren, newChildren, difference);
      }
    }

    return result;
  }

  private Difference createDiff(Difference difference, Ast oldAst, Ast newAst,
      Type type) {
    Difference diff = null;
    try {
      diff = (Difference) difference.clone();
      diff.getNewCb().setAst(newAst);
      diff.getOldCb().setAst(oldAst);
      diff.getNewCb().setBlock(newAst.getRange());
      diff.getOldCb().setBlock(oldAst.getRange());
      diff.setType(type);
    } catch (CloneNotSupportedException e) {
      e.printStackTrace();
    }
    return diff;
  }

  private List<Difference> rearrangeChildren(List<Ast> oldChildren,
      List<Ast> newChildren, Difference difference) {

    AstSequence oldSeq = new AstSequence(oldChildren);
    AstSequence newSeq = new AstSequence(newChildren);

    EditList editList = DiffAlgorithm
        .getAlgorithm(SupportedAlgorithm.HISTOGRAM).diff(AstComparitor.DEFAULT,
            oldSeq, newSeq);

    int oldindex = 0;
    int newindex = 0;

    List<Difference> others = new ArrayList<Difference>();
    List<Difference> inserts = new ArrayList<Difference>();
    List<Difference> deletes = new ArrayList<Difference>();

    while (oldindex < oldChildren.size() && newindex < newChildren.size()) {

      Ast oldCmp = oldChildren.get(oldindex);
      Ast newCmp = newChildren.get(newindex);

      boolean isInEditList = false;

      for (Edit edit : editList) {
        Range<Integer> editA = new Range<Integer>(edit.getBeginA(),
            edit.getEndA());
        Range<Integer> editB = new Range<Integer>(edit.getBeginB(),
            edit.getEndB());

        if (!editA.isEmpty() && editA.contains(oldindex)) {
          Difference childDiff = createDiff(difference, oldCmp, newCmp,
              Type.DELETE);
          //System.out.println("DELETE:" + oldCmp.toString());
          oldindex++;
          deletes.add(childDiff);
          isInEditList = true;
        }
        if (!editB.isEmpty() && editB.contains(newindex)) {
          Difference childDiff = createDiff(difference, oldCmp, newCmp,
              Type.INSERT);
          //System.out.println("INSERT:" + newCmp.toString());
          inserts.add(childDiff);
          newindex++;
          isInEditList = true;
        }
        if (isInEditList) {
          break;
        }
      }

      if (!isInEditList) {
        newindex++;
        oldindex++;
        Difference childDiff = createDiff(difference, oldCmp, newCmp,
            Type.EQUIVALENT);
        others.addAll(drilldown(childDiff));
      }
    }

    others.addAll(matchup(inserts, deletes));
    return others;
  }

  public List<Difference> matchup(List<Difference> inserts,
      List<Difference> deletes) {

    List<List<Difference>> grid = new ArrayList<List<Difference>>();

    try {
      // find all potential matches for deletes and inserts
      for (int ins = 0; ins < inserts.size(); ins++) {
        List<Difference> rows = new ArrayList<Difference>();

        for (int del = 0; del < deletes.size(); del++) {
          long score = 0;

          Difference diff = (Difference) inserts.get(ins).clone();

          diff.setOldCb(deletes.get(del).getOldCb());
          diff.setNewCb(inserts.get(ins).getNewCb());
          diff.setType(Type.MOVE);

          List<Difference> scoreList = drilldown(diff);

          for (Difference scoreDiff : scoreList) {
            score += scoreDiff.getOldCb().getBlock().getEnd()
                - scoreDiff.getOldCb().getBlock().getStart();
            score += scoreDiff.getNewCb().getBlock().getEnd()
                - scoreDiff.getNewCb().getBlock().getStart();
          }

          long base = diff.getOldCb().getBlock().getEnd()
              - diff.getOldCb().getBlock().getStart();
          base += diff.getNewCb().getBlock().getEnd()
              - diff.getNewCb().getBlock().getStart();
          if (base != 0) {
            diff.setScore(score / base);

          } else {
            diff.setScore(score * 2);
          }
          rows.add(diff);

        }

        grid.add(rows);
      }
    } catch (CloneNotSupportedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return getBestScores(grid, inserts, deletes);
  }

  private List<Difference> getBestScores(List<List<Difference>> grid, List<Difference> inserts, List<Difference> deletes) {
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
      if (best.getScore() >= 1) {
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


}

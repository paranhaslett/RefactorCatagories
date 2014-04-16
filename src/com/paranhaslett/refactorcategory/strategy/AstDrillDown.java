package com.paranhaslett.refactorcategory.strategy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffAlgorithm;
import org.eclipse.jgit.diff.DiffAlgorithm.SupportedAlgorithm;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;

import com.paranhaslett.refactorcategory.AstComparitor;
import com.paranhaslett.refactorcategory.AstSequence;
import com.paranhaslett.refactorcategory.CodeBlock;
import com.paranhaslett.refactorcategory.Config;
import com.paranhaslett.refactorcategory.Difference;
import com.paranhaslett.refactorcategory.Difference.Type;
import com.paranhaslett.refactorcategory.Range;
import com.paranhaslett.refactorcategory.ast.Ast;

public class AstDrillDown extends DrillDown {

  @Override
  List<Difference> drilldown(Difference difference) throws IOException, GitAPIException {
    List<Difference> result = new ArrayList<Difference>();

    CodeBlock oldCb = difference.getOldCb();
    CodeBlock newCb = difference.getNewCb();

    Ast oldAst = oldCb.getAst();
    Ast newAst = newCb.getAst();

    if (!newAst.dumpEquals(oldAst)) {
      Difference diff;
      if (newAst.equalTypes(oldAst)) {
        diff = createDiff(difference, oldAst, newAst, Type.RENAMED,
            2 * Config.scoreUnit);
      } else {
        diff = createDiff(difference, oldAst, newAst, Type.MODIFY,
            2 * Config.scoreUnit);
      }
      result.add(diff);
      return result;
    }

    // get all the children
    List<Ast> oldChildren = oldAst.getChildren();
    List<Ast> newChildren = newAst.getChildren();

    if (oldChildren.size() == 0) {
      if (newChildren.size() == 0) {
        Difference diff = createDiff(difference, oldAst, newAst,
            Type.EQUIVALENT, 0.0);
        result.add(diff);
      } else {
        Difference diff = createDiff(difference, oldAst, newAst, Type.INSERT,
            Config.scoreUnit);
        result.add(diff);
      }
    } else {
      if (newChildren.size() == 0) {
        Difference diff = createDiff(difference, oldAst, newAst, Type.DELETE,
            Config.scoreUnit);
        result.add(diff);
      } else {
        Difference diff = createDiff(difference, oldAst, newAst, Type.MODIFY,
            Config.scoreUnit * 2);
        result = collate(diff, multipleElements(oldChildren, newChildren,
            difference));
      }
    }

    return result;
  }

  List<Difference> multipleElements(List<Ast> oldChildren,
      List<Ast> newChildren, Difference difference) throws IOException, GitAPIException {

    AstSequence oldSeq = new AstSequence(oldChildren);
    AstSequence newSeq = new AstSequence(newChildren);

    EditList editList = DiffAlgorithm
        .getAlgorithm(SupportedAlgorithm.HISTOGRAM).diff(AstComparitor.DEFAULT,
            oldSeq, newSeq);

    int oldindex = 0;
    int newindex = 0;

    List<Difference> others = new ArrayList<Difference>();
    List<Difference> modifies = new ArrayList<Difference>();
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
        if (!editA.isEmpty() && !editB.isEmpty()
            && editA.getEnd() - editA.getStart() == 1
            && editB.getEnd() - editB.getStart() == 1
            && editA.contains(oldindex) && editB.contains(newindex)) {
          Difference childDiff = createDiff(difference, oldCmp, newCmp,
              Type.MODIFY, Config.scoreUnit * 2);
          oldindex++;
          newindex++;
          modifies.add(childDiff);
          // TODO check for rename modification
          isInEditList = true;
        } else {

          if (!editA.isEmpty() && editA.contains(oldindex)) {
            Difference childDiff = createDiff(difference, oldCmp, newCmp,
                Type.DELETE, Config.scoreUnit);
            // System.out.println("DELETE:" + oldCmp.toString());
            oldindex++;
            deletes.add(childDiff);
            isInEditList = true;
          }
          if (!editB.isEmpty() && editB.contains(newindex)) {
            Difference childDiff = createDiff(difference, oldCmp, newCmp,
                Type.INSERT, Config.scoreUnit);
            // System.out.println("INSERT:" + newCmp.toString());
            inserts.add(childDiff);
            newindex++;
            isInEditList = true;
          }
        }
        if (isInEditList) {
          break;
        }
      }

      if (!isInEditList) {
        newindex++;
        oldindex++;
        Difference childDiff = createDiff(difference, oldCmp, newCmp,
            Type.EQUIVALENT, 0.0);
        List<Difference> childDiffs = collate(childDiff,
            drilldown(childDiff));
        others.addAll(childDiffs);
      }
    }

    others.addAll(matchup(inserts, deletes));
    return others;
  }


  Difference createDiff(Difference difference, Ast oldAst, Ast newAst,
      Type type, double score) {
    Difference diff = null;
    try {
      diff = (Difference) difference.clone();
      diff.getNewCb().setAst(newAst);
      diff.getOldCb().setAst(oldAst);
      diff.getNewCb().setBlock(newAst.getRange());
      diff.getOldCb().setBlock(oldAst.getRange());
      diff.setType(type);
      diff.setScore(score);
    } catch (CloneNotSupportedException e) {
      // This should not happen as clone of Difference and CodeBlock are valid
      e.printStackTrace();
    }
    return diff;
  }

 

}

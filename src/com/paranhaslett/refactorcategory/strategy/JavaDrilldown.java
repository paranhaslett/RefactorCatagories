package com.paranhaslett.refactorcategory.strategy;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.diff.DiffAlgorithm;
import org.eclipse.jgit.diff.DiffAlgorithm.SupportedAlgorithm;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;

import com.paranhaslett.refactorcategory.CodeBlock;
import com.paranhaslett.refactorcategory.CodeBlockComparitor;
import com.paranhaslett.refactorcategory.CodeBlockSequence;
import com.paranhaslett.refactorcategory.Difference;
import com.paranhaslett.refactorcategory.Difference.Language;
import com.paranhaslett.refactorcategory.Difference.Type;
import com.paranhaslett.refactorcategory.Range;
import com.paranhaslett.refactorcategory.ast.Ast;

public class JavaDrilldown implements DrillDown {

  public static final boolean doModify = false;

  @Override
  public List<Difference> drilldown(Difference difference) {

    List<Difference> result = new ArrayList<Difference>();

    CodeBlock oldCb = difference.getOldCb();
    CodeBlock newCb = difference.getNewCb();

    Ast oldAst = oldCb.getAst();
    Ast newAst = newCb.getAst();

    if (oldAst == null || newAst == null
        || !oldAst.equals(newAst)) {
      result.add(difference);
      return result;
    }

    /** find out if current node is in both edit block ranges **/

    Range<Long> oldAstR = oldAst.getRange();
    Range<Long> newAstR = newAst.getRange();

    if (!oldAstR.intersects(oldCb.getBlock())) {
      if (!newAstR.intersects(newCb.getBlock())) {
        // this one is not interesting
        // return an empty result
        return result;
      }
      difference.setType(Type.INSERT);
      if (oldCb.getBlock().contains(oldAstR)) {
        // this one IS interesting
        // return insert difference
        result.add(difference);
        return result;
      }
    } else {
      if (!newAstR.intersects(newCb.getBlock())) {
        difference.setType(Type.DELETE);
        if (newCb.getBlock().contains(newAstR)) {
          // this one IS interesting
          // return delete difference
          result.add(difference);
          return result;
        }
      }
    }

    /* Get all the children differences */

    List<CodeBlock> newChildren = getChildren(newCb);
    List<CodeBlock> oldChildren = getChildren(oldCb);

    // match up the AST children using longest common subsequence

    CodeBlockSequence oldSeq = new CodeBlockSequence(oldChildren);
    CodeBlockSequence newSeq = new CodeBlockSequence(newChildren);

    EditList editList = DiffAlgorithm
        .getAlgorithm(SupportedAlgorithm.HISTOGRAM).diff(
            CodeBlockComparitor.DEFAULT, oldSeq, newSeq);
    int oldindex = 0;
    int newindex = 0;
    while (oldindex < oldChildren.size() && newindex < newChildren.size()) {
      try {
        Difference childDiff = (Difference) difference.clone();
        CodeBlock oldCmp = oldChildren.get(oldindex);
        CodeBlock newCmp = newChildren.get(newindex);

        childDiff.setOldCb(oldCmp);
        childDiff.setNewCb(newCmp);

        if (oldCmp.getAst()==null||newCmp.getAst()==null||oldCmp.getAst().isEmpty()|| newCmp.getAst().isEmpty()) {
          childDiff.setLanguage(Language.COMMENT);
        }

        if (oldCmp.equals(newCmp)) {
          oldindex++;
          newindex++;
          childDiff.setType(Type.EQUIVALENT);

          if (oldCmp.getAst()!=null && newCmp.getAst()!=null && !oldCmp.getAst().isEmpty() && !newCmp.getAst().isEmpty()) {

            List<Difference> equivDiff = drilldown(childDiff);
            if (equivDiff.isEmpty()) {
              result.add(childDiff);
            } else {
              result.addAll(equivDiff);

            }
          } else {
            result.add(childDiff);
          }

        } else {

          // find out which edit we are in
          for (Edit edit : editList) {
            Range<Integer> editA = new Range<Integer>(edit.getBeginA(),
                edit.getEndA());
            Range<Integer> editB = new Range<Integer>(edit.getBeginB(),
                edit.getEndB());

            if (doModify && editA.contains(oldindex)
                && editB.contains(newindex)) {
              oldindex += edit.getEndA() - edit.getBeginA();
              newindex += edit.getEndB() - edit.getBeginB();
              childDiff.setType(Type.MODIFY);
              result.add(childDiff);
            } else {

              if (editA.contains(oldindex)) {

                childDiff.setType(Type.DELETE);
                result.add(childDiff);
                oldindex++;
              }
              if (editB.contains(newindex)) {
                childDiff = (Difference) difference.clone();
                childDiff.setType(Type.INSERT);
                result.add(childDiff);
                newindex++;

              }
            }

          }
        }
      } catch (CloneNotSupportedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    return result;
  }

  private List<CodeBlock> getChildren(CodeBlock cb) {
    List<CodeBlock> children = new ArrayList<CodeBlock>();

    long minStart = cb.getAst().getStart();

    // sort out any JastAddJ numbering problems
    Ast previous = null;

    for (int i = 0; i < cb.getAst().getNumChild(); i++) {
      Ast child = cb.getAst().getChild(i);
      if (child.getStart() == 0) {
        child.setStart(minStart);
      } else {
        if (previous != null && previous.getEnd() == 0) {
          previous.setEnd(minStart);
        }
        minStart = child.getEnd();
      }
      previous = child;

    }
    if (previous != null && previous.getEnd() == 0) {
      previous.setEnd(cb.getAst().getEnd());
    }

    minStart = cb.getAst().getStart();

    for (int i = 0; i < cb.getAst().getNumChild(); i++) {

      Ast child = cb.getAst().getChild(i);

      try {

        Range<Long> javaAst = new Range<Long>((long) child.getStart(),
            (long) (child.getEnd()));

        if (javaAst.intersects(cb.getBlock())) {
          CodeBlock javaCb = (CodeBlock) cb.clone();
          javaAst = javaAst.getIntersection(cb.getBlock());
          javaCb.setBlock(javaAst);
          javaCb.setAst(child);
          children.add(javaCb);

        }
        if (minStart < child.getStart() - 1) {
          Range<Long> commentAst = new Range<Long>(minStart,
              (long) (child.getStart() - 1));

          if (commentAst.intersects(cb.getBlock())) {
            CodeBlock commentCb = (CodeBlock) cb.clone();
            commentAst = commentAst.getIntersection(cb.getBlock());
            commentCb.setBlock(commentAst);
            commentCb.setAst(null);
            children.add(commentCb);

          }
        }

      } catch (CloneNotSupportedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      minStart = child.getEnd() + 1;
    }

    return children;
  }

  public List<Difference> matchup(List<Difference> differences) {

    // split up differences according to type
    List<Difference> inserts = new ArrayList<Difference>();
    List<Difference> deletes = new ArrayList<Difference>();
    List<Difference> others = new ArrayList<Difference>();

    for (Difference diff : differences) {
      if (diff.getType() == Type.INSERT) {
        inserts.add(diff);
      } else {
        if (diff.getType() == Type.DELETE) {
          deletes.add(diff);
        } else {
          others.add(diff);
        }
      }
    }

    List<List<Difference>> grid = new ArrayList<List<Difference>>();

    // find all potential matches for deletes and inserts
    for (int ins = 0; ins < inserts.size(); ins++) {
      List<Difference> rows = new ArrayList<Difference>();
      for (int del = 0; del < deletes.size(); del++) {
        long score = 0;
        try {
          Difference diff = (Difference) inserts.get(ins).clone();

          diff.setOldCb(deletes.get(del).getOldCb());
          diff.setNewCb(inserts.get(ins).getNewCb());
          diff.setType(Type.MODIFY);

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
        } catch (CloneNotSupportedException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
      grid.add(rows);
    }

    others.addAll(getBestScores(grid));
    return others;
  }

  private List<Difference> getBestScores(List<List<Difference>> grid) {
    List<Difference> result = new ArrayList<Difference>();
    Difference lowest = null;
    int lowestRow = 0;
    int lowestCol = 0;
    for (int ins = 0; ins < grid.size(); ins++) {
      for (int del = 0; del < grid.get(ins).size(); del++) {
        Difference score = grid.get(ins).get(del);
        if (lowest == null || lowest.getScore() > score.getScore()) {
          lowest = score;
          lowestRow = ins;
          lowestCol = del;
        }
      }
    }
    if (lowest != null) {

      grid.remove(lowestRow);
      for (int ins = 0; ins < grid.size(); ins++) {
        grid.get(ins).remove(lowestCol);
      }
      if (grid.size() > 0 && grid.get(0).size() > 0) {
        result.addAll(getBestScores(grid));
      }
      result.add(lowest);
    }
    return result;
  }
  
  

}

package com.paranhaslett.refactorcategory.strategy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffAlgorithm;
import org.eclipse.jgit.diff.DiffAlgorithm.SupportedAlgorithm;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;

import com.paranhaslett.refactorcategory.CodeBlock;
import com.paranhaslett.refactorcategory.Config;
import com.paranhaslett.refactorcategory.Difference;
import com.paranhaslett.refactorcategory.Difference.Language;
import com.paranhaslett.refactorcategory.Difference.Type;
import com.paranhaslett.refactorcategory.Range;
import com.paranhaslett.refactorcategory.compare.CharacterComparitor;
import com.paranhaslett.refactorcategory.compare.CharacterSequence;
import com.paranhaslett.refactorcategory.compare.CodeBlockTextComparitor;
import com.paranhaslett.refactorcategory.compare.CodeBlockTextSequence;

public class TextDrillDown extends DrillDown {

  @Override
  public List<Difference> drilldown(Difference difference) throws IOException,
      GitAPIException {

    CodeBlock oldCb = difference.getOldCb();
    CodeBlock newCb = difference.getNewCb();

    String oldStr = oldCb.getRawText();
    String newStr = newCb.getRawText();

    List<Difference> results = new ArrayList<Difference>();

    /* split up white space */
    if (isWhitespaces(oldStr) && isWhitespaces(newStr)) {
      difference.setLanguage(Language.WHITESPACE);
      if (oldStr.equals(newStr)) {
        difference.setType(Type.EQUIVALENT);
        difference.setScore(0.0);
      } else {
        difference.setType(Type.MODIFY);
        difference.setScore(0.0);
      }
      results.add(difference);
      return results;
    }
    if (isWhitespaces(oldStr)) {
      difference.setType(Type.DELETE);
      difference.setScore(Config.scoreUnit);
      results.add(difference);
      return results;
    }
    if (isWhitespaces(newStr)) {
      difference.setType(Type.INSERT);
      difference.setScore(Config.scoreUnit);
      results.add(difference);
      return results;
    }
    
    if(oldStr.equals(newStr)){
      difference.setType(Type.EQUIVALENT);
      difference.setScore(0);
      results.add(difference);
      return results;
    }

    CharacterSequence oldCs = new CharacterSequence(oldStr);
    CharacterSequence newCs = new CharacterSequence(newStr);

    EditList editList = DiffAlgorithm
        .getAlgorithm(SupportedAlgorithm.HISTOGRAM).diff(
            CharacterComparitor.DEFAULT, oldCs, newCs);

    double totalScore = 0;

    for (Edit edit : editList) {
      if (edit.getType() == Edit.Type.INSERT
          || edit.getType() == Edit.Type.REPLACE) {
        totalScore += Config.scoreUnit * (edit.getEndB() - edit.getBeginB());
      }
      if (edit.getType() == Edit.Type.DELETE
          || edit.getType() == Edit.Type.REPLACE) {
        totalScore += Config.scoreUnit * (edit.getEndA() - edit.getBeginA());
      }

    }

    difference.setScore(totalScore / (oldStr.length() + newStr.length()));
    results.add(difference);

    return results;
  }

  public static boolean isWhitespaces(String str) {
    if (str == null || str.equals("")) {
      return true;
    }
    Pattern pat = Pattern.compile("\\s*");
    Matcher mat = pat.matcher(str);
    if (mat.matches()) {
      return true;
    }
    return false;
  }

  private boolean isContainsSingleLine(String str) {
    Pattern pat = Pattern.compile(".*\\\\\\\\.*\n.*");
    Matcher mat = pat.matcher(str);
    if (mat.matches()) {
      return true;
    }
    return false;
  }

  public List<Difference> drilldown(Difference difference,
      List<CodeBlock> oldChildren, List<CodeBlock> newChildren)
      throws IOException, GitAPIException {

    // match up the AST children using longest common subsequence

    CodeBlockTextSequence oldSeq = new CodeBlockTextSequence(oldChildren);
    CodeBlockTextSequence newSeq = new CodeBlockTextSequence(newChildren);

    EditList editList = DiffAlgorithm
        .getAlgorithm(SupportedAlgorithm.HISTOGRAM).diff(
            CodeBlockTextComparitor.DEFAULT, oldSeq, newSeq);

    int oldindex = 0;
    int newindex = 0;

    List<Difference> modifies = new ArrayList<Difference>();
    List<Difference> inserts = new ArrayList<Difference>();
    List<Difference> deletes = new ArrayList<Difference>();
    List<Difference> results = new ArrayList<Difference>();

    while (oldindex < oldChildren.size() && newindex < newChildren.size()) {

      CodeBlock oldCmp = oldChildren.get(oldindex);
      CodeBlock newCmp = newChildren.get(newindex);

      String oldStr = oldCmp.getRawText();
      String newStr = newCmp.getRawText();

      boolean isInEditList = false;

      for (Edit edit : editList) {
        Range<Integer> editA = new Range<Integer>(edit.getBeginA(),
            edit.getEndA());
        Range<Integer> editB = new Range<Integer>(edit.getBeginB(),
            edit.getEndB());

        if (!editA.isEmpty() && !editB.isEmpty()) {
          Difference childDiff;

          if (isWhitespaces(oldStr)) {
            if (isWhitespaces(newStr)) {
              childDiff = createDiff(difference, oldCmp, newCmp, Type.MODIFY, 0);
              childDiff.setLanguage(Language.WHITESPACE);
            } else {
              childDiff = createDiff(difference, oldCmp, newCmp, Type.DELETE,
                  Config.scoreUnit);
            }
          } else {
            if (isWhitespaces(newStr)) {
              childDiff = createDiff(difference, oldCmp, newCmp, Type.MODIFY, 0);
              childDiff = createDiff(difference, oldCmp, newCmp, Type.INSERT,
                  Config.scoreUnit);
            } else {
              childDiff = createDiff(difference, oldCmp, newCmp, Type.MODIFY,
                  2 * Config.scoreUnit);
            }
          }
          modifies.add(childDiff);
          oldindex++;
          newindex++;
          isInEditList = true;
        } else {

          if ((!editB.isEmpty() || isWhitespaces(newStr))
              && editA.contains(oldindex)) {
            Difference childDiff = createDiff(difference, oldCmp, newCmp,
                Type.DELETE, Config.scoreUnit);
            // System.out.println("DELETE:" + oldCmp.getRawText());
            oldindex++;
            deletes.add(childDiff);
            isInEditList = true;
          }
          if ((!editA.isEmpty() || isWhitespaces(oldStr))
              && editB.contains(newindex)) {
            Difference childDiff = createDiff(difference, oldCmp, newCmp,
                Type.INSERT, Config.scoreUnit);
            // System.out.println("INSERT:" + newCmp.getRawText());
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
      }
    }

    results.addAll(matchup(inserts, deletes));
    results.addAll(modifies);
    return results;
  }

  private boolean isInsertOrDelete(CodeBlock cb, Range<Integer> edit,
      Integer value) {
    if (cb.getAst() == null) {
      if (isWhitespaces(cb.getRawText())) {
        return false;
      }
    }
    if (edit.isEmpty()) {
      return false;

    }
    return edit.contains(value);
  }

  Difference createDiff(Difference difference, CodeBlock oldCmp,
      CodeBlock newCmp, Type type, double score) {
    Difference childDiff = super.createDiff(difference, type, score);
    childDiff.setOldCb(oldCmp);
    childDiff.setNewCb(newCmp);
    childDiff.setLanguage(Language.COMMENT);
    return childDiff;
  }
}

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
import com.paranhaslett.refactorcategory.compare.CharacterComparitor;
import com.paranhaslett.refactorcategory.compare.CharacterSequence;

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

    // TODO single line, multiline comment, javadoc, other

    // System.out.println(":" + oldStr + ":");
    // System.out.println(":" + newStr + ":");

    CharacterSequence oldCs = new CharacterSequence(oldStr);
    CharacterSequence newCs = new CharacterSequence(newStr);

    //long oldPos = oldCb.getBlock().getStart();
    //long newPos = newCb.getBlock().getStart();

    EditList editList = DiffAlgorithm
        .getAlgorithm(SupportedAlgorithm.HISTOGRAM).diff(
            CharacterComparitor.DEFAULT, oldCs, newCs);

    //int oldindex = 0;
    //int newindex = 0;

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
    
    difference.setScore(totalScore/(oldStr.length() + newStr.length()));
    results.add(difference);
  /*
    while (oldindex < oldCs.size() && newindex < newCs.size()) {

      char oldCmp = oldCs.get(oldindex);
      char newCmp = newCs.get(newindex);

      for (Edit edit : editList) {
        Range<Integer> editA = new Range<Integer>(edit.getBeginA(),
            edit.getEndA());
        Range<Integer> editB = new Range<Integer>(edit.getBeginB(),
            edit.getEndB());

        Difference childDiff;

        if (!editA.isEmpty() && editA.contains(oldindex)) {
          oldindex++;

          if (!editB.isEmpty() && editB.contains(newindex)) {
            newindex++;
            if (Character.isWhitespace(oldCmp)) {
              if (Character.isWhitespace(newCmp)) {
                childDiff = createDiff(difference, Type.MODIFY, 0.0);
                childDiff.setLanguage(Language.WHITESPACE);
              } else {
                childDiff = createDiff(difference, Type.INSERT,
                    Config.scoreUnit);
              }
            } else {
              if (Character.isWhitespace(newCmp)) {
                childDiff = createDiff(difference, Type.DELETE,
                    Config.scoreUnit);
              } else {
                childDiff = createDiff(difference, Type.MODIFY,
                    Config.scoreUnit * 2);
              }
            }
          } else {
            if (Character.isWhitespace(newCmp)) {
              childDiff = createDiff(difference, Type.INSERT, 0.0);
              childDiff.setLanguage(Language.WHITESPACE);
            } else {
              childDiff = createDiff(difference, Type.INSERT, Config.scoreUnit);
            }
          }

        } else {
          newindex++;
          if (!editB.isEmpty() && editB.contains(newindex)) {
            if (Character.isWhitespace(oldCmp)) {
              childDiff = createDiff(difference, Type.DELETE, 0.0);
              childDiff.setLanguage(Language.WHITESPACE);
            } else {
              childDiff = createDiff(difference, Type.DELETE, Config.scoreUnit);
            }
          } else {
            oldindex++;
            childDiff = createDiff(difference, Type.EQUIVALENT, 0.0);
          }
        }
        results.add(childDiff);
      }
    } */
    return results;
  }

  private boolean isWhitespaces(String str) {
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

}


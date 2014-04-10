package com.paranhaslett.refactorcategory.strategy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffAlgorithm;
import org.eclipse.jgit.diff.DiffAlgorithm.SupportedAlgorithm;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.diff.RawTextComparator;

import AST.ASTNode;

import com.paranhaslett.refactorcategory.Calculator;
import com.paranhaslett.refactorcategory.CodeBlock;
import com.paranhaslett.refactorcategory.Difference;
import com.paranhaslett.refactorcategory.Difference.Language;
import com.paranhaslett.refactorcategory.Range;
import com.paranhaslett.refactorcategory.ast.Ast;
import com.paranhaslett.refactorcategory.model.Entry;

public class EntryDrillDown implements DrillDown {

  @Override
  public List<Difference> drilldown(Difference difference) throws IOException,
      GitAPIException {
    List<Difference> results = new ArrayList<Difference>();

    CodeBlock oldCb = difference.getOldCb();
    CodeBlock newCb = difference.getNewCb();

    Entry oldEnt = oldCb.getEntry();
    Entry newEnt = newCb.getEntry();

    System.out.println(oldEnt.getPath() + " to " + newEnt.getPath());

    newEnt.open();
    oldEnt.open();

    String oldPath = oldEnt.getPath();
    String newPath = newEnt.getPath();

    RawText oldRaw = oldCb.getEntry().getRawText();
    RawText newRaw = newCb.getEntry().getRawText();

    oldCb.setBlock(convertEditRange(0, oldRaw.size()));
    newCb.setBlock(convertEditRange(0, newRaw.size()));

    EditList editList = DiffAlgorithm
        .getAlgorithm(SupportedAlgorithm.HISTOGRAM).diff(
            RawTextComparator.WS_IGNORE_ALL, oldRaw, newRaw);

    // Convert Edit List into lists of differences
    List<Difference> modifies = new ArrayList<Difference>();
    List<Difference> inserts = new ArrayList<Difference>();
    List<Difference> deletes = new ArrayList<Difference>();
    
    for (Edit edit : editList) {
      try {
        Difference diff = (Difference) difference.clone();

        Range<Long> oldRange = convertEditRange(edit.getBeginA(),
            edit.getEndA());
        Range<Long> newRange = convertEditRange(edit.getBeginB(),
            edit.getEndB());

        diff.getOldCb().setBlock(oldRange);
        diff.getNewCb().setBlock(newRange);
        
        switch(edit.getType()){
        case INSERT:
          diff.setType(Difference.Type.INSERT);
          inserts.add(diff);
          break;
        case DELETE:
          diff.setType(Difference.Type.DELETE);
          deletes.add(diff);
          break;
        case REPLACE:
          diff.setType(Difference.Type.MODIFY);
          modifies.add(diff);
          break;
        default:
          break;
        }
      } catch (CloneNotSupportedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    //do the modifies first
    
    if (modifies.size() > 0 && oldPath.endsWith(".java")
        && newPath.endsWith(".java")) {
      Ast oldAst = oldEnt.getCompilationUnit(oldCb.getRevision(), oldPath);
      Ast newAst = newEnt.getCompilationUnit(newCb.getRevision(), newPath);
      oldCb.setAst(oldAst);
      newCb.setAst(newAst);
      
      Calculator calc = Calculator.getCalc();
      calc.addOldDifferences(modifies);
      

      for (Difference diff : modifies) {
        try {
          diff.getOldCb().setAst((Ast) oldAst.clone());
          diff.getNewCb().setAst((Ast) newAst.clone());
          diff.setLanguage(Language.VALID_JAVA);
          System.out.println("------");
          List<Difference> javaDiffs = new JavaDrillDown().drilldown(diff);
          results.addAll(javaDiffs);
          calc.addNewDifferences(javaDiffs);
        } catch (CloneNotSupportedException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }
    return results;
  }

  private Range<Long> convertEditRange(int start, int end) {
    long rangeStart = (long) ASTNode.makePosition(start, 1);
    long rangeEnd = (long) ASTNode.makePosition(end, 1);
    return new Range<Long>(rangeStart, rangeEnd);
  }

}

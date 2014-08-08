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
import com.paranhaslett.refactorcategory.Difference.Type;
import com.paranhaslett.refactorcategory.Range;
import com.paranhaslett.refactorcategory.ast.Ast;
import com.paranhaslett.refactorcategory.model.Entry;

public class EntryDrillDown extends DrillDown {

  @Override
  public List<Difference> drilldown(Difference difference) throws IOException,
      GitAPIException {
    
    List<Difference> results = new ArrayList<Difference>();

    CodeBlock oldCb = difference.getOldCb();
    CodeBlock newCb = difference.getNewCb();

    Entry oldEnt = oldCb.getEntry();
    Entry newEnt = newCb.getEntry();

    newEnt.open();
    oldEnt.open();

    String oldPath = oldEnt.getPath();
    String newPath = newEnt.getPath();
    
    //System.out.println(oldPath);
    //System.out.println(newPath);
    
    if (oldPath.endsWith("TypeAnalysis.java") && oldCb.getRevision().getName().startsWith("Minor bug fix f")){
      System.out.println("Debug");
    }

    RawText oldRaw = oldCb.getEntry().getRawText();
    RawText newRaw = newCb.getEntry().getRawText();

    oldCb.setBlock(convertEditRange(1, oldRaw.size()));
    newCb.setBlock(convertEditRange(1, newRaw.size()));
    
    Ast oldAst = null;
    Ast newAst = null;
    
    if (oldPath.endsWith(".java")){
      oldAst = oldEnt.getCompilationUnit(oldCb.getRevision(), oldPath);
      //oldAst = new Ast(new ASTNode<>());
      oldCb.setAst(oldAst);       
    }
    
    if (newPath.endsWith(".java")) {
      newAst = newEnt.getCompilationUnit(newCb.getRevision(), newPath);
      //newAst = new Ast(new ASTNode<>());
      newCb.setAst(newAst);
    }
    
    if (oldAst != null && newAst != null){    
      difference.setLanguage(Language.JAVA);
    }
    
    //List<Difference> textDiff = new TextDrillDown().drilldown(difference);

    EditList editList = DiffAlgorithm
        .getAlgorithm(SupportedAlgorithm.HISTOGRAM).diff(
            RawTextComparator.WS_IGNORE_ALL, oldRaw, newRaw);

    // Convert Edit List into lists of differences
   // List<Difference> modifies = new ArrayList<Difference>();
    List<Difference> inserts = new ArrayList<Difference>();
    List<Difference> deletes = new ArrayList<Difference>();

    for (Edit edit : editList) {

      Difference diff = createDiff(difference, Type.UNKNOWN, 0.0);

      Range<Long> oldRange = convertEditRange(edit.getBeginA() + 1, edit.getEndA() + 1);
      Range<Long> newRange = convertEditRange(edit.getBeginB() + 1, edit.getEndB() + 1);

      diff.getOldCb().setBlock(oldRange);
      diff.getNewCb().setBlock(newRange);
      
      //System.out.println(edit.getType());
      
     
     

      switch (edit.getType()) {
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
        if (oldAst != null && newAst != null){       
          // do the modifies first
          Calculator calc = Calculator.getCalc();
          calc.addOldDifference(diff);
          List<Difference> javaDiffs = new JavaDrillDown().drilldown(diff);
          results.addAll(javaDiffs);
          calc.addNewDifferences(javaDiffs);
        }
        break;
      default:
        break;
      }
    } 
    
    /*List<Difference> modifies = filter(textDiff,Type.MODIFY);
    for (Difference diff:modifies){
      if (oldAst != null && newAst != null){       
        // do the modifies first
        Calculator calc = Calculator.getCalc();
        calc.addOldDifference(diff);
        List<Difference> javaDiffs = new JavaDrillDown().drilldown(diff);
        results.addAll(javaDiffs);
        calc.addNewDifferences(javaDiffs);
      }
    }
    
    List<Difference> inserts = filter(textDiff,Type.INSERT);
    List<Difference> deletes = filter(textDiff,Type.DELETE); */
    
    results.addAll(new CodeBlockDrillDown().matchup(inserts, deletes));
    return results;
  }

  private Range<Long> convertEditRange(int start, int end) {
    long rangeStart = (long) ASTNode.makePosition(start, 1);
    long rangeEnd = (long) ASTNode.makePosition(end, 1);
    return new Range<Long>(rangeStart, rangeEnd);
  }
}

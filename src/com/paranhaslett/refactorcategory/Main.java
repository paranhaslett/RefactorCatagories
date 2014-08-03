package com.paranhaslett.refactorcategory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.errors.GitAPIException;

import com.paranhaslett.refactorcategory.strategy.RepoDrillDown;

public class Main {

  /**
   * @param args
   */
  public static void main(String[] args) {
    Main tester = new Main();
    //tester.doTest("/home/paran/Documents/Test/Jasm/.git");
    //tester.doTest("/home/paran/Documents/Test/jpp/.git");
    //tester.doTest("/home/paran/Documents/Test/ast-java/.git");
    tester.doTest("/home/paran/Documents/Test/java-object-diff/.git");
    //tester.doTest("/home/paran/Documents/Test/diffj/.git");
    //tester.doTest("/home/paran/Documents/Test/jacoco/.git");
    //tester.doTest("/home/paran/Documents/Test/lombok/.git");
    //tester.doTest("/home/paran/Documents/Test/antlr4/.git");
    //tester.doTest("/home/paran/Documents/Test/clojure/.git");
    //tester.doTest("/home/paran/Documents/Test/jgit/.git");
    
  }

  private void doTest(String repoName){
    RepoDrillDown rdd = new RepoDrillDown();
    rdd.setRepo(repoName);
    List<Difference> results = new ArrayList<Difference>();
    try {
      results.addAll(rdd.drilldown(null));
    } catch (IOException e) {
      e.printStackTrace();
    } catch (GitAPIException e) {
      e.printStackTrace();
    }
    
    Calculator calc = Calculator.getCalc();
    calc.printReport();
  }
}

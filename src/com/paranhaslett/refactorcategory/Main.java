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
    tester.doTest("/home/paran/Documents/Test/Jasm/.git");
    tester.doTest("/home/paran/Documents/Test/lombok/.git");
    
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

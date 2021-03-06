package com.paranhaslett.refactorcategory;

import java.util.ArrayList;
import java.util.List;

import com.paranhaslett.refactorcategory.Difference.Language;
import com.paranhaslett.refactorcategory.Difference.Type;

public class Calculator {
  
  private List<Difference> oldDifferences = new ArrayList<Difference>();
  private List<Difference> newDifferences = new ArrayList<Difference>();
  
  
  private static Calculator calc = null;
  
  private Calculator(){}
  
  public static Calculator getCalc(){
    if (calc == null){
      calc = new Calculator();
    }
    return calc;
  }
  
  public void addOldDifferences(List<Difference> differences){
    oldDifferences.addAll(differences);
  }
  
  public void addOldDifference(Difference difference){
    oldDifferences.add(difference);
  }
  
public void addNewDifferences(List<Difference> differences){
    /* de-reference ASTs as they take up too much memory*/
    for(Difference diff: differences){
      CodeBlock cb = diff.getOldCb();
      if (cb != null){
        cb.setAst(null);
      }
      cb = diff.getNewCb();
      if (cb != null){
        cb.setAst(null);
      }
    }
    newDifferences.addAll(differences);
  }

  public void printReport(){
    
    
      for(Language lang: Language.values()){    
        for(Type type:Type.values()){
          int oldcount = 0;         
          for (Difference diff:oldDifferences){
            if(diff.getLanguage() == lang){
              if(diff.getType() == type){
              
                if (oldcount < 1 ){
                  System.out.println();
                  System.out.println("GIT DIFF:" +lang + ":" + type);
                  System.out.println("====================================");
                }
                if (oldcount < 3){ 
                  System.out.println("-----" + diff.getOldCb() + "-----");
                  System.out.println(diff.getOldCb().getRawText());
                  System.out.println(">>>>>" +  diff.getNewCb() + ">>>>>");
                  System.out.println(diff.getNewCb().getRawText());
                  System.out.println("------------------------------------");
                }
                oldcount++;
              }
            }
          }
          int newcount =0;
          for (Difference diff:newDifferences){
            if(diff.getLanguage() == lang){
              if(diff.getType() == type){    
               if (newcount < 1 ){
                  System.out.println();
                  System.out.println("REFACTOR CATEGORIES:" +lang + ":" + type);
                }
                if (newcount < 3){
                  System.out.println("-----" + diff.getOldCb() + "-----");
                  if(diff.getOldCb().getBlock().getStart() < 1 || diff.getOldCb().getBlock().getEnd() < 1 ){
                    System.out.println(diff);
                  } else {
                    System.out.println(diff.getOldCb().getRawText());
                  }
                  System.out.println(">>>>>" +  diff.getNewCb() + ">>>>>");
                  if(diff.getNewCb().getBlock().getStart() < 1 || diff.getNewCb().getBlock().getEnd() < 1){
                    System.out.println(diff);
                  } else {
                    System.out.println(diff.getNewCb().getRawText());
                  }
                  System.out.println("------------------------------------");
                }
                newcount++;
              }
            }
          }
          if (oldcount >0 || newcount>0){
            System.out.println("Git diff =" + oldcount + "  Refactor Categories =" + newcount);
          }
        }
      }   
    }
  }

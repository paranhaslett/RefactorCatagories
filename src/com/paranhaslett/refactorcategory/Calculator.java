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
  
public void addNewDifferences(List<Difference> differences){
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
                  System.out.println("OLD:" +lang + ":" + type);
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
                  System.out.println("NEW:" +lang + ":" + type);
                }
                if (newcount < 3){
                  System.out.println("-----" + diff.getOldCb() + "-----");
                  if(diff.getOldCb().getBlock().getStart() < 1){
                    System.out.println(diff);
                  } else {
                    System.out.println(diff.getOldCb().getRawText());
                  }
                  System.out.println(">>>>>" +  diff.getNewCb() + ">>>>>");
                  if(diff.getNewCb().getBlock().getStart() < 1){
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

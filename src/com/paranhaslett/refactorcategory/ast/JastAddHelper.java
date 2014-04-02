package com.paranhaslett.refactorcategory.ast;

public class JastAddHelper {

 private JastAddHelper jastAddHelper = null;
 
 private JastAddHelper(){}
 
 
  JastAddHelper getJastAddHelper(){
    if(jastAddHelper == null){
      jastAddHelper = new JastAddHelper();
    }
    return jastAddHelper;
  }
  
  /*
  com.paranhaslett.refactorcategory.JastAddHelper.setupProgram()
  com.paranhaslett.refactorcategory.JastAddHelper.setupProgram().new JavaParser() {...}
  com.paranhaslett.refactorcategory.JastAddHelper.setupProgram().new JavaParser() {...}.parse(InputStream, String)
  JastAddHelper.java
  */

}

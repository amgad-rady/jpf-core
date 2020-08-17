/*
 * Copyright (C) 2014, United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 *
 * The Java Pathfinder core (jpf-core) platform is licensed under the
 * Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0. 
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */
package gov.nasa.jpf.jvm.bytecode;

import gov.nasa.jpf.vm.BootstrapMethodInfo;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.FunctionObjectFactory;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.LoadOnJPFRequired;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.Types;
import gov.nasa.jpf.vm.VM;

import java.util.ArrayList;

/**
 * @author Nastaran Shafiei <nastaran.shafiei@gmail.com>
 * 
 * Invoke dynamic method. It allows dynamic linkage between a call site and a method implementation.
 *
 * ..., [arg1, [arg2 ...]]  => ...
 */
public class INVOKEDYNAMIC extends Instruction {
  
  // index of a bootstrap method (index to the array bootstrapMethods[] declared in ClassInfo
  // containing this bytecode instruction)
  int bootstrapMethodIndex;
  
  // Free variables are those that are not defined within the lamabda body and 
  // are captured from the lexical scope. Note that for instance lambda methods 
  // the first captured variable always represents "this"
  String[] freeVariableTypeNames;
  byte[] freeVariableTypes;
  int freeVariableSize;
  
  String functionalInterfaceName;
  
  String samMethodName;
  
  int funcObjRef = MJIEnv.NULL;
  
  ElementInfo lastFuncObj = null;
  
  public INVOKEDYNAMIC () {}

  protected INVOKEDYNAMIC (int bmIndex, String methodName, String descriptor){
    bootstrapMethodIndex = bmIndex;
    samMethodName = methodName;
    freeVariableTypeNames = Types.getArgumentTypeNames(descriptor);
    freeVariableTypes = Types.getArgumentTypes(descriptor);
    functionalInterfaceName = Types.getReturnTypeSignature(descriptor);
    freeVariableSize = Types.getArgumentsSize(descriptor);
  }

  @Override
  public int getByteCode () {
    return 0xBA;
  }
  
  @Override
  public String toString() {
    String args = "";
    for(String type: freeVariableTypeNames) {
      if(args.length()>0) {
        type += ','+ type;
      }
      args += type;
    }
    return "invokedynamic " + bootstrapMethodIndex + " " + 
    samMethodName + '(' + args +"):" + functionalInterfaceName;
  }

  /**
   * For now, INVOKEDYNAMIC works only in the context of lambda expressions.
   * Executing this returns an object that implements the functional interface 
   * and contains a method which captures the behavior of the lambda expression.
   */
  @Override
  public Instruction execute (ThreadInfo ti) {
    StackFrame frame = ti.getModifiableTopFrame();
    
    ElementInfo ei = ti.getHeap().get(funcObjRef);
    
    if(ei==null || ei!=lastFuncObj || freeVariableTypes.length>0) {
      ClassInfo fiClassInfo;

      // First, resolve the functional interface
      try {
        fiClassInfo = ti.resolveReferencedClass(functionalInterfaceName);
      } catch(LoadOnJPFRequired lre) {
        return ti.getPC();
      }

      if (fiClassInfo.initializeClass(ti)) {
        return ti.getPC();
      }
      
      ClassInfo enclosingClass = this.getMethodInfo().getClassInfo();
      
      BootstrapMethodInfo bmi = enclosingClass.getBootstrapMethodInfo(bootstrapMethodIndex);
      
      VM vm = VM.getVM();
      FunctionObjectFactory funcObjFactory = vm.getFunctionObjectFacotry();
      
      Object[] freeVariableValues = frame.getArgumentsValues(ti, freeVariableTypes);

      //toString code injection here
      //if funcObjFactory is in an initial state, lock it and determine the count
      if (funcObjFactory.toStringIndicator == null && funcObjFactory.indicatorIdx == 0) {
        System.err.println("First call");
        funcObjFactory.toStringIndicator = funcObjFactory.indicatorToStringCalls(freeVariableValues);
      }

      //if funcObjFactory has been initialized, start adding stackframes
      if (funcObjFactory.toStringIndicator != null && funcObjFactory.indicatorIdx < freeVariableValues.length) {
        System.err.println("Indicator: " + funcObjFactory.indicatorIdx);
        funcObjFactory.indicatorIdx++;
        System.err.println("Indicator incremented to: " + funcObjFactory.indicatorIdx);
        if (funcObjFactory.toStringIndicator[funcObjFactory.indicatorIdx - 1] == 1) {
          funcObjFactory.count++;
          return funcObjFactory.injectCallToString((ElementInfo) freeVariableValues[funcObjFactory.indicatorIdx - 1], ti);
        } else {
          return ti.getPC();
        }
      }

      //Finish the injection by collecting the frames and reset funcObjFactory after the injection is complete
      if (funcObjFactory.toStringIndicator != null && funcObjFactory.indicatorIdx >= freeVariableValues.length) {
        while (funcObjFactory.count > 0) {
          //Deal with the stack frames and get the returned value;
          int returnedStringRef = ti.getTopFrame().pop();
          ElementInfo eiString = ti.getHeap().get(returnedStringRef);
          String output = eiString.toString();

          funcObjFactory.returnedStrings.push(output);
          funcObjFactory.count--;
        }

        //Reset
        funcObjFactory.toStringIndicator = null;
        funcObjFactory.indicatorIdx = 0;
      }

      //This call should reset the array of returned strings before returning
      funcObjRef = funcObjFactory.getFunctionObject(bootstrapMethodIndex, ti, fiClassInfo, samMethodName, bmi,
              freeVariableTypeNames, freeVariableValues);
      lastFuncObj = ti.getHeap().get(funcObjRef);
    }
    
    frame.pop(freeVariableSize);
    frame.pushRef(funcObjRef);
    
    return getNext(ti);
  }
}

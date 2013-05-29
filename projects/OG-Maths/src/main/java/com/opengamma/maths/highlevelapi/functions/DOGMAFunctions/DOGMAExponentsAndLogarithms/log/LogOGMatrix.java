/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.maths.highlevelapi.functions.DOGMAFunctions.DOGMAExponentsAndLogarithms.log;

import com.opengamma.maths.dogma.engine.DOGMAMethodHook;
import com.opengamma.maths.dogma.engine.methodhookinstances.unary.Log;
import com.opengamma.maths.highlevelapi.datatypes.primitive.OGArray;
import com.opengamma.maths.highlevelapi.datatypes.primitive.OGMatrix;
import com.opengamma.maths.lowlevelapi.exposedapi.EasyIZY;
import com.opengamma.maths.lowlevelapi.functions.checkers.Catchers;
import com.opengamma.maths.lowlevelapi.functions.memory.DenseMemoryManipulation;
import com.opengamma.maths.lowlevelapi.functions.memory.OGTypesMalloc;

/**
 * does log()
 */
@DOGMAMethodHook(provides = Log.class)
public final class LogOGMatrix implements Log<OGArray<? extends Number>, OGMatrix> {

  @Override
  public OGArray<? extends Number> eval(OGMatrix array1) {
    Catchers.catchNullFromArgList(array1, 1);

    double[] data = array1.getData();
    int n = data.length;
    double[] tmp;
    // check bounds
    boolean complex = false;
    for (int i = 0; i < n; i++) {
      if (data[i] < 0.e0) {
        complex = true;
        break;
      }
    }
    OGArray<? extends Number> retarr;
    if (complex) {
      tmp = DenseMemoryManipulation.convertSinglePointerToZeroInterleavedSinglePointer(data);
      EasyIZY.vz_ln(tmp, tmp);
      retarr = OGTypesMalloc.OGComplexMatrixBasedOnStructureOf(array1, tmp);
    } else {
      tmp = new double[n];
      EasyIZY.vd_ln(data, tmp);
      retarr = OGTypesMalloc.OGMatrixBasedOnStructureOf(array1, tmp);
    }
    return retarr;
  }
}
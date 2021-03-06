/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;

/**
 * 
 */
public class NonnegativityPreservingCubicSplineInterpolator1DTest {

  private static final NonnegativityPreservingCubicSplineInterpolator INTERP_NAT = new NonnegativityPreservingCubicSplineInterpolator(new NaturalSplineInterpolator());
  private static final NonnegativityPreservingCubicSplineInterpolator1D INTERP1D_NAT = new NonnegativityPreservingCubicSplineInterpolator1D(new NaturalSplineInterpolator());
  private static final NonnegativityPreservingCubicSplineInterpolator INTERP_NAK = new NonnegativityPreservingCubicSplineInterpolator(new CubicSplineInterpolator());
  private static final NonnegativityPreservingCubicSplineInterpolator1D INTERP1D_NAK = new NonnegativityPreservingCubicSplineInterpolator1D(new CubicSplineInterpolator());
  private static final NonnegativityPreservingCubicSplineInterpolator INTERP_AKIMA = new NonnegativityPreservingCubicSplineInterpolator(new SemiLocalCubicSplineInterpolator());
  private static final NonnegativityPreservingCubicSplineInterpolator1D INTERP1D_AKIMA = new NonnegativityPreservingCubicSplineInterpolator1D(new SemiLocalCubicSplineInterpolator());

  private static final double EPS = 1.e-7;

  /**
   * Recovery test on polynomial, rational, exponential functions, and node sensitivity test by finite difference method
   */
  @Test
  public void sampleFunctionTest() {
    final int nData = 10;
    final double[] xValues = new double[nData];
    final double[] yValues1 = new double[nData];
    final double[] yValues2 = new double[nData];
    final double[] yValues3 = new double[nData];
    final double[] yValues1Up = new double[nData];
    final double[] yValues2Up = new double[nData];
    final double[] yValues3Up = new double[nData];
    final double[] yValues1Dw = new double[nData];
    final double[] yValues2Dw = new double[nData];
    final double[] yValues3Dw = new double[nData];
    final double[] xKeys = new double[10 * nData];

    for (int i = 0; i < nData; ++i) {
      //      xValues[i] = i * i + i - 1.;
      xValues[i] = i + 1;
      yValues1[i] = 0.5 * xValues[i] * xValues[i] * xValues[i] - 1.5 * xValues[i] * xValues[i] + xValues[i] - 2.;
      yValues2[i] = Math.exp(0.1 * xValues[i] - 6.);
      yValues3[i] = (2. * xValues[i] * xValues[i] + xValues[i]) / (xValues[i] * xValues[i] + xValues[i] * xValues[i] * xValues[i] + 5. * xValues[i] + 2.);
      //      yValues1[i] = xValues[i];
      //      System.out.println(yValues1[i] + "\t" + yValues2[i] + "\t" + yValues3[i]);
      yValues1Up[i] = yValues1[i];
      yValues2Up[i] = yValues2[i];
      yValues3Up[i] = yValues3[i];
      yValues1Dw[i] = yValues1[i];
      yValues2Dw[i] = yValues2[i];
      yValues3Dw[i] = yValues3[i];
    }

    final double xMin = xValues[0];
    final double xMax = xValues[nData - 1];
    for (int i = 0; i < 10 * nData; ++i) {
      xKeys[i] = xMin + (xMax - xMin) / (10 * nData - 1) * i;
    }

    final NonnegativityPreservingCubicSplineInterpolator[] bareInterp = new NonnegativityPreservingCubicSplineInterpolator[] {INTERP_NAT, INTERP_NAK, INTERP_AKIMA };
    final NonnegativityPreservingCubicSplineInterpolator1D[] wrappedInterp = new NonnegativityPreservingCubicSplineInterpolator1D[] {INTERP1D_NAT, INTERP1D_NAK, INTERP1D_AKIMA };
    final int nMethods = bareInterp.length;

    for (int k = 0; k < nMethods; ++k) {
      final double[] resPrim1 = bareInterp[k].interpolate(xValues, yValues1, xKeys).getData();
      final double[] resPrim2 = bareInterp[k].interpolate(xValues, yValues2, xKeys).getData();
      final double[] resPrim3 = bareInterp[k].interpolate(xValues, yValues3, xKeys).getData();

      Interpolator1DDataBundle dataBund1 = wrappedInterp[k].getDataBundleFromSortedArrays(xValues, yValues1);
      Interpolator1DDataBundle dataBund2 = wrappedInterp[k].getDataBundleFromSortedArrays(xValues, yValues2);
      Interpolator1DDataBundle dataBund3 = wrappedInterp[k].getDataBundleFromSortedArrays(xValues, yValues3);
      for (int i = 0; i < 10 * nData; ++i) {
        final double ref1 = resPrim1[i];
        final double ref2 = resPrim2[i];
        final double ref3 = resPrim3[i];
        assertEquals(ref1, wrappedInterp[k].interpolate(dataBund1, xKeys[i]), 1.e-15 * Math.max(Math.abs(ref1), 1.));
        assertEquals(ref2, wrappedInterp[k].interpolate(dataBund2, xKeys[i]), 1.e-15 * Math.max(Math.abs(ref2), 1.));
        assertEquals(ref3, wrappedInterp[k].interpolate(dataBund3, xKeys[i]), 1.e-15 * Math.max(Math.abs(ref3), 1.));
      }

      for (int j = 0; j < nData; ++j) {
        yValues1Up[j] = yValues1[j] * (1. + EPS);
        yValues2Up[j] = yValues2[j] * (1. + EPS);
        yValues3Up[j] = yValues3[j] * (1. + EPS);
        yValues1Dw[j] = yValues1[j] * (1. - EPS);
        yValues2Dw[j] = yValues2[j] * (1. - EPS);
        yValues3Dw[j] = yValues3[j] * (1. - EPS);
        Interpolator1DDataBundle dataBund1Up = wrappedInterp[k].getDataBundleFromSortedArrays(xValues, yValues1Up);
        Interpolator1DDataBundle dataBund2Up = wrappedInterp[k].getDataBundleFromSortedArrays(xValues, yValues2Up);
        Interpolator1DDataBundle dataBund3Up = wrappedInterp[k].getDataBundleFromSortedArrays(xValues, yValues3Up);
        Interpolator1DDataBundle dataBund1Dw = wrappedInterp[k].getDataBundleFromSortedArrays(xValues, yValues1Dw);
        Interpolator1DDataBundle dataBund2Dw = wrappedInterp[k].getDataBundleFromSortedArrays(xValues, yValues2Dw);
        Interpolator1DDataBundle dataBund3Dw = wrappedInterp[k].getDataBundleFromSortedArrays(xValues, yValues3Dw);
        for (int i = 0; i < 10 * nData; ++i) {
          double res1 = 0.5 * (wrappedInterp[k].interpolate(dataBund1Up, xKeys[i]) - wrappedInterp[k].interpolate(dataBund1Dw, xKeys[i])) / EPS / yValues1[j];
          double res2 = 0.5 * (wrappedInterp[k].interpolate(dataBund2Up, xKeys[i]) - wrappedInterp[k].interpolate(dataBund2Dw, xKeys[i])) / EPS / yValues2[j];
          double res3 = 0.5 * (wrappedInterp[k].interpolate(dataBund3Up, xKeys[i]) - wrappedInterp[k].interpolate(dataBund3Dw, xKeys[i])) / EPS / yValues3[j];
          //        System.out.println(res1 + "\t" + INTERP1D.getNodeSensitivitiesForValue(dataBund1, xKeys[i])[j]);
          //        System.out.println(res2 + "\t" + INTERP1D.getNodeSensitivitiesForValue(dataBund2, xKeys[i])[j]);
          //        System.out.println(res3 + "\t" + INTERP1D.getNodeSensitivitiesForValue(dataBund3, xKeys[i])[j]);
          //        System.out.println(i + "\t" + j);
          //        INTERP1D.getNodeSensitivitiesForValue(dataBund1, xKeys[i]);
          assertEquals(res1, wrappedInterp[k].getNodeSensitivitiesForValue(dataBund1, xKeys[i])[j], Math.max(Math.abs(yValues1[j]) * EPS, EPS) * 10.);
          assertEquals(res2, wrappedInterp[k].getNodeSensitivitiesForValue(dataBund2, xKeys[i])[j], Math.max(Math.abs(yValues2[j]) * EPS, EPS) * 10.);
          assertEquals(res3, wrappedInterp[k].getNodeSensitivitiesForValue(dataBund3, xKeys[i])[j], Math.max(Math.abs(yValues3[j]) * EPS, EPS) * 10.);
        }
        yValues1Up[j] = yValues1[j];
        yValues2Up[j] = yValues2[j];
        yValues3Up[j] = yValues3[j];
        yValues1Dw[j] = yValues1[j];
        yValues2Dw[j] = yValues2[j];
        yValues3Dw[j] = yValues3[j];
      }
    }
  }

  /**
   * 
   */
  @Test
  public void modifiedFunctionTest() {
    final int nData = 10;
    final double[] xValues = new double[nData];
    final double[] yValues1 = new double[] {4., 3., 2., 1., 0.1, 0.1, 1., 2., 3., 4. };
    final double[] yValues2 = new double[] {-4., -3., -2., -1., -0.1, -0.1, -1., -2., -3., -4. };
    final double[] yValues3 = new double[] {1., 0.01, 0.5, 5., 0.1, 0.2, 0.5, 3., 2., 2. };
    final double[] yValues1Up = new double[nData];
    final double[] yValues2Up = new double[nData];
    final double[] yValues3Up = new double[nData];
    final double[] yValues1Dw = new double[nData];
    final double[] yValues2Dw = new double[nData];
    final double[] yValues3Dw = new double[nData];
    final double[] xKeys = new double[10 * nData];

    for (int i = 0; i < nData; ++i) {
      //      xValues[i] = i * i + i - 1.;
      xValues[i] = i + 1;
      yValues1Up[i] = yValues1[i];
      yValues2Up[i] = yValues2[i];
      yValues3Up[i] = yValues3[i];
      yValues1Dw[i] = yValues1[i];
      yValues2Dw[i] = yValues2[i];
      yValues3Dw[i] = yValues3[i];
    }

    final double xMin = xValues[0];
    final double xMax = xValues[nData - 1];
    for (int i = 0; i < 10 * nData; ++i) {
      xKeys[i] = xMin + (xMax - xMin) / (10 * nData - 1) * i;
    }

    final NonnegativityPreservingCubicSplineInterpolator[] bareInterp = new NonnegativityPreservingCubicSplineInterpolator[] {INTERP_NAT, INTERP_NAK };
    final NonnegativityPreservingCubicSplineInterpolator1D[] wrappedInterp = new NonnegativityPreservingCubicSplineInterpolator1D[] {INTERP1D_NAT, INTERP1D_NAK };
    final int nMethods = bareInterp.length;

    for (int k = 0; k < nMethods; ++k) {
      final double[] resPrim1 = bareInterp[k].interpolate(xValues, yValues1, xKeys).getData();
      final double[] resPrim2 = bareInterp[k].interpolate(xValues, yValues2, xKeys).getData();
      final double[] resPrim3 = bareInterp[k].interpolate(xValues, yValues3, xKeys).getData();

      Interpolator1DDataBundle dataBund1 = wrappedInterp[k].getDataBundleFromSortedArrays(xValues, yValues1);
      Interpolator1DDataBundle dataBund2 = wrappedInterp[k].getDataBundleFromSortedArrays(xValues, yValues2);
      Interpolator1DDataBundle dataBund3 = wrappedInterp[k].getDataBundleFromSortedArrays(xValues, yValues3);
      for (int i = 0; i < 10 * nData; ++i) {
        final double ref1 = resPrim1[i];
        final double ref2 = resPrim2[i];
        final double ref3 = resPrim3[i];
        assertEquals(ref1, wrappedInterp[k].interpolate(dataBund1, xKeys[i]), 1.e-15 * Math.max(Math.abs(ref1), 1.));
        assertEquals(ref2, wrappedInterp[k].interpolate(dataBund2, xKeys[i]), 1.e-15 * Math.max(Math.abs(ref2), 1.));
        assertEquals(ref3, wrappedInterp[k].interpolate(dataBund3, xKeys[i]), 1.e-15 * Math.max(Math.abs(ref3), 1.));
      }

      for (int j = 0; j < nData; ++j) {
        yValues1Up[j] = yValues1[j] * (1. + EPS);
        yValues2Up[j] = yValues2[j] * (1. + EPS);
        yValues3Up[j] = yValues3[j] * (1. + EPS);
        yValues1Dw[j] = yValues1[j] * (1. - EPS);
        yValues2Dw[j] = yValues2[j] * (1. - EPS);
        yValues3Dw[j] = yValues3[j] * (1. - EPS);
        Interpolator1DDataBundle dataBund1Up = wrappedInterp[k].getDataBundleFromSortedArrays(xValues, yValues1Up);
        Interpolator1DDataBundle dataBund2Up = wrappedInterp[k].getDataBundleFromSortedArrays(xValues, yValues2Up);
        Interpolator1DDataBundle dataBund3Up = wrappedInterp[k].getDataBundleFromSortedArrays(xValues, yValues3Up);
        Interpolator1DDataBundle dataBund1Dw = wrappedInterp[k].getDataBundleFromSortedArrays(xValues, yValues1Dw);
        Interpolator1DDataBundle dataBund2Dw = wrappedInterp[k].getDataBundleFromSortedArrays(xValues, yValues2Dw);
        Interpolator1DDataBundle dataBund3Dw = wrappedInterp[k].getDataBundleFromSortedArrays(xValues, yValues3Dw);
        //        System.out.println("\n");
        //        System.out.println(k + "\t" + j);
        for (int i = 0; i < 10 * nData; ++i) {
          double res1 = 0.5 * (wrappedInterp[k].interpolate(dataBund1Up, xKeys[i]) - wrappedInterp[k].interpolate(dataBund1Dw, xKeys[i])) / EPS / yValues1[j];
          double res2 = 0.5 * (wrappedInterp[k].interpolate(dataBund2Up, xKeys[i]) - wrappedInterp[k].interpolate(dataBund2Dw, xKeys[i])) / EPS / yValues2[j];
          double res3 = 0.5 * (wrappedInterp[k].interpolate(dataBund3Up, xKeys[i]) - wrappedInterp[k].interpolate(dataBund3Dw, xKeys[i])) / EPS / yValues3[j];
          //          System.out.println(res1 + "\t" + INTERP1D.getNodeSensitivitiesForValue(dataBund1, xKeys[i])[j]);
          //          System.out.println(res2 + "\t" + INTERP1D.getNodeSensitivitiesForValue(dataBund2, xKeys[i])[j]);
          //          System.out.println(res3 + "\t" + INTERP1D.getNodeSensitivitiesForValue(dataBund3, xKeys[i])[j]);
          //          System.out.println(i + "\t" + j);
          //          INTERP1D.getNodeSensitivitiesForValue(dataBund1, xKeys[i]);
          assertEquals(res1, wrappedInterp[k].getNodeSensitivitiesForValue(dataBund1, xKeys[i])[j], Math.max(Math.abs(yValues1[j]) * EPS, EPS) * 10.);
          assertEquals(res2, wrappedInterp[k].getNodeSensitivitiesForValue(dataBund2, xKeys[i])[j], Math.max(Math.abs(yValues2[j]) * EPS, EPS) * 10.);
          assertEquals(res3, wrappedInterp[k].getNodeSensitivitiesForValue(dataBund3, xKeys[i])[j], Math.max(Math.abs(yValues3[j]) * EPS, EPS) * 10.);
          //          if (j == 1) {
          //            System.out.println(xKeys[i] + "\t" + wrappedInterp[k].interpolate(dataBund2, xKeys[i]) + "\t" + wrappedInterp[k].interpolate(dataBund2Up, xKeys[i]) + "\t" +
          //                wrappedInterp[k].interpolate(dataBund2Dw, xKeys[i]));
          //          }
        }
        yValues1Up[j] = yValues1[j];
        yValues2Up[j] = yValues2[j];
        yValues3Up[j] = yValues3[j];
        yValues1Dw[j] = yValues1[j];
        yValues2Dw[j] = yValues2[j];
        yValues3Dw[j] = yValues3[j];
      }
    }
  }

  @Test
  public void linearDataTest() {
    final double[] xValues = new double[] {1., 2., 3., 4., 5., 6., 7., 8. };
    //    final double[] yValues = new double[] {1., 3., 5., 7., 9., 11., 13., 15. };
    final double[] yValues = new double[] {1., 1., 1., 1., 1., 1., 1., 1. };
    final int nData = xValues.length;
    double[] yValuesUp = Arrays.copyOf(yValues, nData);
    double[] yValuesDw = Arrays.copyOf(yValues, nData);
    final double[] xKeys = new double[10 * nData];
    final double xMin = xValues[0];
    final double xMax = xValues[nData - 1];
    for (int i = 0; i < 10 * nData; ++i) {
      xKeys[i] = xMin + (xMax - xMin) / (10 * nData - 1) * i;
    }

    Interpolator1DDataBundle dataBund = INTERP1D_NAT.getDataBundleFromSortedArrays(xValues, yValues);

    for (int j = 1; j < nData; ++j) {
      yValuesUp[j] = yValues[j] * (1. + EPS);
      yValuesDw[j] = yValues[j] * (1. - EPS);
      Interpolator1DDataBundle dataBundUp = INTERP1D_NAT.getDataBundle(xValues, yValuesUp);
      Interpolator1DDataBundle dataBundDw = INTERP1D_NAT.getDataBundle(xValues, yValuesDw);
      for (int i = 0; i < 10 * nData; ++i) {
        double res0 = 0.5 * (INTERP1D_NAT.interpolate(dataBundUp, xKeys[i]) - INTERP1D_NAT.interpolate(dataBundDw, xKeys[i])) / EPS / yValues[j];
        double res1 = (INTERP1D_NAT.interpolate(dataBundUp, xKeys[i]) - INTERP1D_NAT.interpolate(dataBund, xKeys[i])) / EPS / yValues[j];
        double res2 = (INTERP1D_NAT.interpolate(dataBund, xKeys[i]) - INTERP1D_NAT.interpolate(dataBundDw, xKeys[i])) / EPS / yValues[j];
        //        System.out.println(res0 + "\t" + res1 + "\t" + res2 + "\t" + INTERP1D_NAT.getNodeSensitivitiesForValue(dataBund, xKeys[i])[j]);
        assertEquals(res0, INTERP1D_NAT.getNodeSensitivitiesForValue(dataBund, xKeys[i])[j], Math.max(Math.abs(yValues[j]) * EPS, EPS));
      }
      yValuesUp[j] = yValues[j];
      yValuesDw[j] = yValues[j];
      //      System.out.println("\n");
    }
  }

}

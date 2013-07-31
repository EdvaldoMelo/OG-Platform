/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.inflation.method;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.instrument.inflation.CapFloorInflationZeroCouponMonthlyDefinition;
import com.opengamma.analytics.financial.interestrate.TestsDataSetsBlack;
import com.opengamma.analytics.financial.interestrate.TestsDataSetsSABR;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CapFloorInflationZeroCouponMonthly;
import com.opengamma.analytics.financial.model.interestrate.definition.InflationZeroCouponCapFloorParameters;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.analytics.financial.model.option.parameters.BlackSmileCapInflationZeroCouponParameters;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.inflation.BlackSmileCapInflationZeroCouponProvider;
import com.opengamma.analytics.financial.provider.description.inflation.BlackSmileCapInflationZeroCouponProviderDiscount;
import com.opengamma.analytics.financial.provider.description.inflation.InflationIssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.method.SuccessiveRootFinderInflationZeroCouponCapFloorCalibrationEngine;
import com.opengamma.analytics.financial.provider.method.SuccessiveRootFinderInflationZeroCouponCapFloorCalibrationObjective;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.interpolation.Interpolator2D;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the calibration engine for inflation zero coupon cap/floor calibration.
 */
public class CapFloorZeroCouponCalibrationObjectiveTest {

  //Cap/floor description
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final Currency CUR = Currency.EUR;
  private static final ZonedDateTime START_DATE = DateUtils.getUTCDate(2011, 9, 7);
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtils.getUTCDate(2011, 9, 9);
  private static final double NOTIONAL = 10000; //100m
  private static final double[] STRIKES = {-.01, .00, .01, .02, .03, .04 };
  private static final boolean IS_CAP = true;
  private static final InflationIssuerProviderDiscount MARKET = MulticurveProviderDiscountDataSets.createMarket1();
  private static final IndexPrice[] PRICE_INDEXES = MARKET.getPriceIndexes().toArray(new IndexPrice[MARKET.getPriceIndexes().size()]);
  private static final IndexPrice PRICE_INDEX_EUR = PRICE_INDEXES[0];
  private static final double INDEX_START_VALUE = 100.0;
  private static final Calendar CALENDAR_EUR = MulticurveProviderDiscountDataSets.getEURCalendar();
  private static final int MONTH_LAG = 3;
  private static final ZonedDateTime LAST_KNOWN_FIXING_DATE = DateUtils.getUTCDate(2008, 7, 01);
  private static final InterpolatedDoublesSurface BLACK_SURF = TestsDataSetsBlack.createBlackSurfaceExpiryStrike();
  private static final BlackSmileCapInflationZeroCouponParameters BLACK_PARAM = new BlackSmileCapInflationZeroCouponParameters(BLACK_SURF, PRICE_INDEX_EUR);
  private static final BlackSmileCapInflationZeroCouponProviderDiscount BLACK_INFLATION = new BlackSmileCapInflationZeroCouponProviderDiscount(MARKET.getInflationProvider(), BLACK_PARAM);

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 9, 7);
  private static final YieldCurveBundle CURVES = TestsDataSetsSABR.createCurves1();
  private static final SABRInterestRateParameters SABR_PARAMETER = TestsDataSetsSABR.createSABR1();
  private static final SABRInterestRateDataBundle SABR_BUNDLE = new SABRInterestRateDataBundle(SABR_PARAMETER, CURVES);
  private static final String[] CURVES_NAME = CURVES.getAllNames().toArray(new String[CURVES.size()]);

  private static final CapFloorInflationZeroCouponMonthlyBlackSmileMethod METHOD = CapFloorInflationZeroCouponMonthlyBlackSmileMethod.getInstance();
  double[][] marketPrices = new double[6][30];

  private static double[] expiryTimes1 = new double[30];
  private static final double[] strikes = {-.01, .00, .01, .02, .03, .04 };
  private static final double[][] volatilities = { {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 },
    {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 },
    {.01, .01, .01, .01, .01, .01 },
    {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 },
    {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 },
    {.01, .01, .01, .01, .01, .01 },
    {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 },
    {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 }, {.01, .01, .01, .01, .01, .01 },
    {.01, .01, .01, .01, .01, .01 } };
  private static final int[] availabelTenor = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 15, 20, 25, 30 };
  CapFloorInflationZeroCouponMonthlyDefinition[][] CAP_DEFINITIONS = new CapFloorInflationZeroCouponMonthlyDefinition[6][availabelTenor.length];
  CapFloorInflationZeroCouponMonthly[][] CAPS = new CapFloorInflationZeroCouponMonthly[6][availabelTenor.length];

  @Test
  /**
   * Tests the correctness of INFLATION YEAR ON YEAR CAP/FLOOR calibration to market prices.
   */
  public void calibration() {

    for (int loop1 = 0; loop1 < STRIKES.length; loop1++) {
      for (int loop2 = 0; loop2 < availabelTenor.length; loop2++) {
        final Period tenor = Period.ofYears(availabelTenor[loop2]);
        final ZonedDateTime payementDate = ScheduleCalculator.getAdjustedDate(START_DATE, tenor, BUSINESS_DAY, CALENDAR_EUR);
        CAP_DEFINITIONS[loop1][loop2] = CapFloorInflationZeroCouponMonthlyDefinition.from(SETTLEMENT_DATE, payementDate, NOTIONAL, PRICE_INDEX_EUR, MONTH_LAG, MONTH_LAG,
            availabelTenor[loop2], LAST_KNOWN_FIXING_DATE, INDEX_START_VALUE, STRIKES[loop1], IS_CAP);
        CAPS[loop1][loop2] = CAP_DEFINITIONS[loop1][loop2].toDerivative(REFERENCE_DATE, CURVES_NAME);
      }
    }
    for (int loopexp = 0; loopexp < CAPS[0].length; loopexp++) {
      expiryTimes1[loopexp] = CAPS[0][loopexp].getReferenceEndTime();
    }
    final InflationZeroCouponCapFloorParameters parameters = new InflationZeroCouponCapFloorParameters(expiryTimes1, strikes, volatilities, PRICE_INDEX_EUR);
    final SuccessiveRootFinderInflationZeroCouponCapFloorCalibrationObjective objective = new SuccessiveRootFinderInflationZeroCouponCapFloorCalibrationObjective(parameters, CUR);
    final SuccessiveRootFinderInflationZeroCouponCapFloorCalibrationEngine calibrationEngine = new SuccessiveRootFinderInflationZeroCouponCapFloorCalibrationEngine(objective);
    for (int loop1 = 0; loop1 < STRIKES.length; loop1++) {
      for (int loop2 = 0; loop2 < availabelTenor.length; loop2++) {
        marketPrices[loop1][loop2] = METHOD.presentValue(CAPS[loop1][loop2], BLACK_INFLATION).getAmount(CUR);
      }
    }

    for (int loop1 = 0; loop1 < STRIKES.length; loop1++) {
      for (int loop2 = 0; loop2 < availabelTenor.length; loop2++) {
        calibrationEngine.addInstrument(CAPS[loop1][loop2], marketPrices[loop1][loop2]);
      }
    }
    calibrationEngine.calibrate(MARKET.getInflationProvider());
    final MultipleCurrencyAmount[][] pvCapYearOnYear = new MultipleCurrencyAmount[STRIKES.length][CAPS[0].length];
    for (int loop1 = 0; loop1 < STRIKES.length; loop1++) {
      for (int loop2 = 0; loop2 < availabelTenor.length; loop2++) {
        final Interpolator2D interpolator = objective.getInflationCapZeroCouponProvider().getBlackParameters().getVolatilitySurface().getInterpolator();
        final BlackSmileCapInflationZeroCouponParameters CalibratedBlackSmileCapInflationZeroCouponParameters = new BlackSmileCapInflationZeroCouponParameters(
            objective.getInflationCapZeroCouponParameters(), interpolator);
        final BlackSmileCapInflationZeroCouponProvider CalibratedBlackSmileCapInflationYearOnYearProvider = new BlackSmileCapInflationZeroCouponProvider(objective.getInflationCapZeroCouponProvider()
            .getInflationProvider(),
            CalibratedBlackSmileCapInflationZeroCouponParameters);
        pvCapYearOnYear[loop1][loop2] = METHOD.presentValue(CAPS[loop1][loop2], CalibratedBlackSmileCapInflationYearOnYearProvider);
        assertEquals("Inflaiton year on year calibration: cap/floor " + loop1, pvCapYearOnYear[loop1][loop2].getAmount(CUR), marketPrices[loop1][loop2], 1E-2);
      }
    }
  }

}
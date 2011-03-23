/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.swap;

import static org.junit.Assert.assertEquals;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.financial.instrument.annuity.AnnuityCouponIborSpreadDefinition;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Tenor;

/**
 * 
 */
public class ZZZSwapIborIborDefinitionTest {

  // Swap 2Y
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final Tenor ANNUITY_TENOR = new Tenor(Period.ofYears(2));
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtil.getUTCDate(2011, 3, 17);
  private static final double NOTIONAL = 1000000;
  //  private static final ZonedDateTime MATURITY_DATE = ScheduleCalculator.getAdjustedDate(SETTLEMENT_DATE, BUSINESS_DAY, CALENDAR, IS_EOM, ANNUITY_TENOR);
  private static final Currency CUR = Currency.USD;
  //Ibor leg: semi money
  private static final Tenor INDEX_TENOR_1 = new Tenor(Period.ofMonths(6));
  private static final int SETTLEMENT_DAYS_1 = 2;
  private static final DayCount DAY_COUNT_1 = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final boolean IS_PAYER_1 = true;
  private static final IborIndex INDEX_1 = new IborIndex(CUR, INDEX_TENOR_1, SETTLEMENT_DAYS_1, CALENDAR, DAY_COUNT_1, BUSINESS_DAY, IS_EOM);
  private static final AnnuityCouponIborDefinition IBOR_LEG_1 = AnnuityCouponIborDefinition.from(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL, INDEX_1, IS_PAYER_1);
  //Ibor leg: quarterly money
  private static final Tenor INDEX_TENOR_2 = new Tenor(Period.ofMonths(3));
  private static final int SETTLEMENT_DAYS_2 = 2;
  private static final DayCount DAY_COUNT_2 = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final IborIndex INDEX_2 = new IborIndex(CUR, INDEX_TENOR_2, SETTLEMENT_DAYS_2, CALENDAR, DAY_COUNT_2, BUSINESS_DAY, IS_EOM);
  private static final double SPREAD = -0.001;
  private static final AnnuityCouponIborSpreadDefinition IBOR_LEG_2 = AnnuityCouponIborSpreadDefinition.from(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL, INDEX_2, SPREAD, !IS_PAYER_1);
  // Swap
  private static final ZZZSwapIborIborDefinition TENOR_SWAP = new ZZZSwapIborIborDefinition(IBOR_LEG_1, IBOR_LEG_2);

  @Test(expected = IllegalArgumentException.class)
  public void testNullLeg1() {
    new ZZZSwapIborIborDefinition(null, IBOR_LEG_2);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullLeg2() {
    new ZZZSwapIborIborDefinition(IBOR_LEG_1, null);
  }

  @Test
  public void testGetter() {
    assertEquals(TENOR_SWAP.getLegWithoutSpread(), IBOR_LEG_1);
    assertEquals(TENOR_SWAP.getLegWithSpread(), IBOR_LEG_2);
  }

}

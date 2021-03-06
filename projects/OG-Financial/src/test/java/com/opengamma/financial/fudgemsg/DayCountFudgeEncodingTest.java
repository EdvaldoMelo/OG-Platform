/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;

import org.fudgemsg.UnmodifiableFudgeField;
import org.fudgemsg.wire.types.FudgeWireType;
import org.testng.annotations.Test;

import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class DayCountFudgeEncodingTest extends FinancialTestBase {

  private static final DayCount s_ref = DayCountFactory.INSTANCE.getDayCount("Act/360");

  @Test
  public void testCycle() {
    assertEquals(s_ref, cycleObject(DayCount.class, s_ref));
  }

  @Test
  public void testFromString() {
    assertEquals(s_ref, getFudgeContext().getFieldValue(DayCount.class,
        UnmodifiableFudgeField.of(FudgeWireType.STRING, s_ref.getConventionName())));
  }

}

/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import java.math.BigDecimal;
import java.util.Arrays;

import org.testng.annotations.Test;

import com.opengamma.core.position.impl.SimplePosition;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link ComputationTargetFudgeBuilder} class.
 */
@Test(groups = TestGroup.UNIT)
public class ComputationTargetFudgeBuilderTest extends AbstractFudgeBuilderTestCase {

  public void testNull() {
    assertEncodeDecodeCycle(ComputationTarget.class, ComputationTarget.NULL);
  }

  public void testBasic() {
    assertEncodeDecodeCycle(ComputationTarget.class, new ComputationTarget(ComputationTargetType.POSITION, new SimplePosition(UniqueId.of("Pos", "Foo"), BigDecimal.ONE, ExternalId.of("Sec", "Bar"))));
  }

  public void testNested_1() {
    assertEncodeDecodeCycle(ComputationTarget.class, new ComputationTarget(ComputationTargetType.PORTFOLIO_NODE.containing(ComputationTargetType.POSITION), Arrays.asList(UniqueId.of("Prt", "Foo")),
        new SimplePosition(UniqueId.of("Pos", "Foo"), BigDecimal.ONE, ExternalId.of("Sec", "Bar"))));
  }

  public void testNested_2() {
    assertEncodeDecodeCycle(
        ComputationTarget.class,
        new ComputationTarget(ComputationTargetType.PORTFOLIO_NODE.containing(ComputationTargetType.PORTFOLIO_NODE).containing(ComputationTargetType.POSITION), Arrays.asList(
            UniqueId.of("Prt", "Foo"), UniqueId.of("Prt", "Bar")), new SimplePosition(UniqueId.of("Pos", "Foo"), BigDecimal.ONE, ExternalId.of("Sec", "Bar"))));
  }

}

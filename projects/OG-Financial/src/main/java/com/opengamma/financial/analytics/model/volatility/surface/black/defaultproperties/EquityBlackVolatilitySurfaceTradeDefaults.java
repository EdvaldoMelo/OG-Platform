/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties;

import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.financial.analytics.model.equity.EquitySecurityUtils;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;

/**
 *
 */
public class EquityBlackVolatilitySurfaceTradeDefaults extends EquityBlackVolatilitySurfaceDefaults {

  public EquityBlackVolatilitySurfaceTradeDefaults(final String... defaultsPerTicker) {
    super(ComputationTargetType.TRADE, defaultsPerTicker);
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.TRADE) {
      return false;
    }
    final Security security = target.getTrade().getSecurity();
    final String ticker = EquitySecurityUtils.getIndexOrEquityName(security);
    if (getAllTickers().contains(ticker)) {
      return true;
    }
    return false;
  }

  @Override
  protected String getTicker(final ComputationTarget target) {
    final EquityIndexOptionSecurity security = (EquityIndexOptionSecurity) target.getTrade().getSecurity();
    return EquitySecurityUtils.getIndexOrEquityName(security);
  }
}
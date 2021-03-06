/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import java.util.Set;

import org.apache.commons.lang.NotImplementedException;
import org.testng.annotations.Test;

import com.opengamma.analytics.financial.commodity.derivative.AgricultureForward;
import com.opengamma.analytics.financial.commodity.derivative.AgricultureFuture;
import com.opengamma.analytics.financial.commodity.derivative.AgricultureFutureOption;
import com.opengamma.analytics.financial.commodity.derivative.EnergyForward;
import com.opengamma.analytics.financial.commodity.derivative.EnergyFuture;
import com.opengamma.analytics.financial.commodity.derivative.EnergyFutureOption;
import com.opengamma.analytics.financial.commodity.derivative.MetalForward;
import com.opengamma.analytics.financial.commodity.derivative.MetalFuture;
import com.opengamma.analytics.financial.commodity.derivative.MetalFutureOption;
import com.opengamma.analytics.financial.credit.cds.ISDACDSDerivative;
import com.opengamma.analytics.financial.equity.future.derivative.CashSettledFuture;
import com.opengamma.analytics.financial.equity.future.derivative.EquityFuture;
import com.opengamma.analytics.financial.equity.future.derivative.EquityIndexDividendFuture;
import com.opengamma.analytics.financial.equity.future.derivative.EquityIndexFuture;
import com.opengamma.analytics.financial.equity.future.derivative.IndexFuture;
import com.opengamma.analytics.financial.equity.future.derivative.VolatilityIndexFuture;
import com.opengamma.analytics.financial.equity.option.EquityIndexFutureOption;
import com.opengamma.analytics.financial.equity.option.EquityIndexOption;
import com.opengamma.analytics.financial.equity.option.EquityOption;
import com.opengamma.analytics.financial.equity.variance.EquityVarianceSwap;
import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.forex.derivative.ForexNonDeliverableForward;
import com.opengamma.analytics.financial.forex.derivative.ForexNonDeliverableOption;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionDigital;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionSingleBarrier;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.forex.derivative.ForexSwap;
import com.opengamma.analytics.financial.instrument.TestInstrumentDefinitionsAndDerivatives;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponIborRatchet;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondCapitalIndexedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondCapitalIndexedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondIborSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondIborTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondInterestIndexedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondInterestIndexedTransaction;
import com.opengamma.analytics.financial.interestrate.cash.derivative.Cash;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositCounterpart;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositIbor;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositZero;
import com.opengamma.analytics.financial.interestrate.fra.derivative.ForwardRateAgreement;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuture;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFutureOptionPremiumSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFutureOptionPremiumTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.FederalFundsFutureSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.FederalFundsFutureTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionPremiumSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionPremiumTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.SwapFuturesPriceDeliverableSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.SwapFuturesPriceDeliverableTransaction;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CapFloorInflationYearOnYearInterpolation;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CapFloorInflationYearOnYearMonthly;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CapFloorInflationZeroCouponInterpolation;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CapFloorInflationZeroCouponMonthly;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationYearOnYearInterpolation;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationYearOnYearInterpolationWithMargin;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationYearOnYearMonthly;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationYearOnYearMonthlyWithMargin;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationZeroCouponInterpolation;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationZeroCouponInterpolationGearing;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationZeroCouponMonthly;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationZeroCouponMonthlyGearing;
import com.opengamma.analytics.financial.interestrate.payments.ForexForward;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorCMS;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorCMSSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponArithmeticAverageON;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponArithmeticAverageONSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponArithmeticAverageONSpreadSimplified;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponCMS;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixedCompounding;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborAverage;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompounding;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompoundingSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborGearing;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponOIS;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCompoundingCoupon;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionBermudaFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.varianceswap.VarianceSwap;

/**
 * 
 */
public class InstrumentDerivativeVisitorTest {
  private static final Set<InstrumentDerivative> ALL_DERIVATIVES = TestInstrumentDefinitionsAndDerivatives.getAllDerivatives();
  private static final MyVisitor<Object> VISITOR = new MyVisitor<>();

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDerivative() {
    new InstrumentDerivativeVisitorDelegate(null);
  }

  @Test
  public void testNullVisitor() {
    for (final InstrumentDerivative derivative : ALL_DERIVATIVES) {
      if (derivative != null) {
        try {
          derivative.accept(null);
          fail();
        } catch (final IllegalArgumentException e) {
        } catch (final NullPointerException e) {
          throw new NullPointerException("accept(InstrumentDerivativeVisitor visitor) in " + derivative.getClass().getSimpleName() + " does not check that the visitor is not null");
        }
      } else {
        throw new NullPointerException("Derivative was null");
      }
    }
    for (final InstrumentDerivative derivative : ALL_DERIVATIVES) {
      try {
        derivative.accept(null, "");
        fail();
      } catch (final IllegalArgumentException e) {
      } catch (final NullPointerException e) {
        throw new NullPointerException("accept(InstrumentDerivativeVisitor visitor, S data) in " + derivative.getClass().getSimpleName() + " does not check that the visitor is not null");
      }
    }
  }

  @Test
  public void testVisitMethodsImplemented() {

  }

  @Test
  public void testDelegate() {
    final String s = "aaaa";
    final String result = s + " + data1";
    final BondFixedVisitor<Object> visitor = new BondFixedVisitor<>(VISITOR, s);
    for (final InstrumentDerivative definition : ALL_DERIVATIVES) {
      if (definition instanceof BondFixedSecurity) {
        assertEquals(definition.accept(visitor), s);
        assertEquals(definition.accept(visitor, ""), result);
      } else {
        assertEquals(definition.accept(visitor), definition.accept(VISITOR));
        assertEquals(definition.accept(visitor, ""), definition.accept(VISITOR, ""));
      }
    }
  }

  @Test
  public void testAdapter() {
    final DummyVisitor visitor = new DummyVisitor();
    for (final InstrumentDerivative derivative : ALL_DERIVATIVES) {
      try {
        derivative.accept(visitor);
        fail();
      } catch (final UnsupportedOperationException e) {
      }
    }

    for (final InstrumentDerivative derivative : ALL_DERIVATIVES) {
      try {
        derivative.accept(visitor, "");
        fail();
      } catch (final UnsupportedOperationException e) {
      }
    }

    for (final InstrumentDerivative derivative : ALL_DERIVATIVES) {
      try {
        derivative.accept(visitor, null);
        fail();
      } catch (final UnsupportedOperationException e) {
      }
    }
  }

  @Test
  public void testSameValueAdapter() {
    final Double value = Math.PI;
    final InstrumentDerivativeVisitor<Double, Double> visitor = new InstrumentDerivativeVisitorSameValueAdapter<>(value);
    for (final InstrumentDerivative derivative : ALL_DERIVATIVES) {
      assertEquals(value, derivative.accept(visitor));
      assertEquals(value, derivative.accept(visitor, Math.E));
    }
  }

  @Test
  public void testSameMethodAdapter() {
    final String data = "qwerty";
    final InstrumentDerivativeVisitor<String, String> visitor = new SameMethodAdapter();
    for (final InstrumentDerivative derivative : ALL_DERIVATIVES) {
      final String simpleName = derivative.getClass().getSimpleName();
      assertEquals(simpleName, derivative.accept(visitor));
      assertEquals(derivative.getClass().getSimpleName() + data, derivative.accept(visitor, data));
    }
  }

  private static class DummyVisitor extends InstrumentDerivativeVisitorAdapter<Object, Object> {

    public DummyVisitor() {
    }

  }

  private static class SameMethodAdapter extends InstrumentDerivativeVisitorSameMethodAdapter<String, String> {

    public SameMethodAdapter() {
    }

    @Override
    public String visit(final InstrumentDerivative instrument) {
      return instrument.getClass().getSimpleName();
    }

    @Override
    public String visit(final InstrumentDerivative instrument, final String data) {
      return instrument.getClass().getSimpleName() + data;
    }

  }

  private static class BondFixedVisitor<T> extends InstrumentDerivativeVisitorDelegate<T, String> {
    private final String _s;

    public BondFixedVisitor(final InstrumentDerivativeVisitor<T, String> delegate, final String s) {
      super(delegate);
      _s = s;
    }

    @Override
    public String visitBondFixedSecurity(final BondFixedSecurity bond, final T data) {
      return _s + " + data1";
    }

    @Override
    public String visitBondFixedSecurity(final BondFixedSecurity bond) {
      return _s;
    }

  }

  private static class MyVisitor<T> implements InstrumentDerivativeVisitor<T, String> {

    public MyVisitor() {
    }

    private String getValue(final InstrumentDerivative derivative, final boolean withData) {
      String result = derivative.getClass().getSimpleName();
      if (withData) {
        result += " + data";
      }
      return result;
    }

    @Override
    public String visitBondFixedSecurity(final BondFixedSecurity bond, final T data) {
      return getValue(bond, true);
    }

    @Override
    public String visitBondFixedTransaction(final BondFixedTransaction bond, final T data) {
      return getValue(bond, false);
    }

    @Override
    public String visitBondIborSecurity(final BondIborSecurity bond, final T data) {
      return getValue(bond, true);
    }

    @Override
    public String visitBondIborTransaction(final BondIborTransaction bond, final T data) {
      return getValue(bond, true);
    }

    @Override
    public String visitBillSecurity(final BillSecurity bill, final T data) {
      return getValue(bill, true);
    }

    @Override
    public String visitBillTransaction(final BillTransaction bill, final T data) {
      return getValue(bill, true);
    }

    @Override
    public String visitGenericAnnuity(final Annuity<? extends Payment> genericAnnuity, final T data) {
      return getValue(genericAnnuity, true);
    }

    @Override
    public String visitFixedCouponAnnuity(final AnnuityCouponFixed fixedCouponAnnuity, final T data) {
      return getValue(fixedCouponAnnuity, true);
    }

    @Override
    public String visitAnnuityCouponIborRatchet(final AnnuityCouponIborRatchet annuity, final T data) {
      return getValue(annuity, true);
    }

    @Override
    public String visitFixedCouponSwap(final SwapFixedCoupon<?> swap, final T data) {
      return getValue(swap, true);
    }

    @Override
    public String visitSwaptionCashFixedIbor(final SwaptionCashFixedIbor swaption, final T data) {
      return getValue(swaption, true);
    }

    @Override
    public String visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption, final T data) {
      return getValue(swaption, true);
    }

    @Override
    public String visitSwaptionBermudaFixedIbor(final SwaptionBermudaFixedIbor swaption, final T data) {
      return getValue(swaption, true);
    }

    @Override
    public String visitForexForward(final ForexForward fx, final T data) {
      throw new NotImplementedException("Not implemented because derivative is deprecated");
    }

    @Override
    public String visitCash(final Cash cash, final T data) {
      return getValue(cash, true);
    }

    @Override
    public String visitFixedPayment(final PaymentFixed payment, final T data) {
      return getValue(payment, true);
    }

    @Override
    public String visitCouponCMS(final CouponCMS payment, final T data) {
      return getValue(payment, true);
    }

    @Override
    public String visitCapFloorIbor(final CapFloorIbor payment, final T data) {
      return getValue(payment, true);
    }

    @Override
    public String visitCapFloorCMS(final CapFloorCMS payment, final T data) {
      return getValue(payment, true);
    }

    @Override
    public String visitCapFloorCMSSpread(final CapFloorCMSSpread payment, final T data) {
      return getValue(payment, true);
    }

    @Override
    public String visitForwardRateAgreement(final ForwardRateAgreement fra, final T data) {
      return getValue(fra, true);
    }

    @Override
    public String visitBondCapitalIndexedSecurity(final BondCapitalIndexedSecurity<?> bond, final T data) {
      return getValue(bond, true);
    }

    @Override
    public String visitBondCapitalIndexedTransaction(final BondCapitalIndexedTransaction<?> bond, final T data) {
      return getValue(bond, true);
    }

    @Override
    public String visitCDSDerivative(final ISDACDSDerivative cds, final T data) {
      return getValue(cds, true);
    }

    @Override
    public String visitBondFixedSecurity(final BondFixedSecurity bond) {
      return getValue(bond, false);
    }

    @Override
    public String visitBondFixedTransaction(final BondFixedTransaction bond) {
      return getValue(bond, false);
    }

    @Override
    public String visitBondIborSecurity(final BondIborSecurity bond) {
      return getValue(bond, false);
    }

    @Override
    public String visitBondIborTransaction(final BondIborTransaction bond) {
      return getValue(bond, false);
    }

    @Override
    public String visitBillSecurity(final BillSecurity bill) {
      return getValue(bill, false);
    }

    @Override
    public String visitBillTransaction(final BillTransaction bill) {
      return getValue(bill, false);
    }

    @Override
    public String visitGenericAnnuity(final Annuity<? extends Payment> genericAnnuity) {
      return getValue(genericAnnuity, false);
    }

    @Override
    public String visitFixedCouponAnnuity(final AnnuityCouponFixed fixedCouponAnnuity) {
      return getValue(fixedCouponAnnuity, false);
    }

    @Override
    public String visitAnnuityCouponIborRatchet(final AnnuityCouponIborRatchet annuity) {
      return getValue(annuity, false);
    }

    @Override
    public String visitFixedCouponSwap(final SwapFixedCoupon<?> swap) {
      return getValue(swap, false);
    }

    @Override
    public String visitSwaptionCashFixedIbor(final SwaptionCashFixedIbor swaption) {
      return getValue(swaption, false);
    }

    @Override
    public String visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption) {
      return getValue(swaption, false);
    }

    @Override
    public String visitSwaptionBermudaFixedIbor(final SwaptionBermudaFixedIbor swaption) {
      return getValue(swaption, false);
    }

    @Override
    public String visitForexForward(final ForexForward fx) {
      throw new NotImplementedException("Not implemented because derivative is deprecated");
    }

    @Override
    public String visitCash(final Cash cash) {
      return getValue(cash, false);
    }

    @Override
    public String visitFixedPayment(final PaymentFixed payment) {
      return getValue(payment, false);
    }

    @Override
    public String visitCouponCMS(final CouponCMS payment) {
      return getValue(payment, false);
    }

    @Override
    public String visitCapFloorIbor(final CapFloorIbor payment) {
      return getValue(payment, false);
    }

    @Override
    public String visitCapFloorCMS(final CapFloorCMS payment) {
      return getValue(payment, false);
    }

    @Override
    public String visitCapFloorCMSSpread(final CapFloorCMSSpread payment) {
      return getValue(payment, false);
    }

    @Override
    public String visitForwardRateAgreement(final ForwardRateAgreement fra) {
      return getValue(fra, false);
    }

    @Override
    public String visitBondCapitalIndexedSecurity(final BondCapitalIndexedSecurity<?> bond) {
      return getValue(bond, false);
    }

    @Override
    public String visitBondCapitalIndexedTransaction(final BondCapitalIndexedTransaction<?> bond) {
      return getValue(bond, false);
    }

    @Override
    public String visitCDSDerivative(final ISDACDSDerivative cds) {
      return getValue(cds, false);
    }

    @Override
    public String visitCouponFixed(final CouponFixed payment, final T data) {
      return getValue(payment, true);
    }

    @Override
    public String visitCouponFixed(final CouponFixed payment) {
      return getValue(payment, false);
    }

    @Override
    public String visitCouponIbor(final CouponIbor payment, final T data) {
      return getValue(payment, true);
    }

    @Override
    public String visitCouponIbor(final CouponIbor payment) {
      return getValue(payment, false);
    }

    @Override
    public String visitCouponIborSpread(final CouponIborSpread payment, final T data) {
      return getValue(payment, true);
    }

    @Override
    public String visitCouponIborSpread(final CouponIborSpread payment) {
      return getValue(payment, false);
    }

    @Override
    public String visitCouponIborGearing(final CouponIborGearing payment) {
      return getValue(payment, false);
    }

    @Override
    public String visitCouponIborGearing(final CouponIborGearing payment, final T data) {
      return getValue(payment, true);
    }

    @Override
    public String visitCouponIborCompounding(final CouponIborCompounding payment, final T data) {
      return getValue(payment, true);
    }

    @Override
    public String visitCouponOIS(final CouponOIS payment, final T data) {
      return getValue(payment, true);
    }

    @Override
    public String visitCouponOIS(final CouponOIS payment) {
      return getValue(payment, false);
    }

    @Override
    public String visitSwap(final Swap<?, ?> swap, final T data) {
      return getValue(swap, true);
    }

    @Override
    public String visitSwap(final Swap<?, ?> swap) {
      return getValue(swap, false);
    }

    @Override
    public String visitCouponInflationZeroCouponMonthly(final CouponInflationZeroCouponMonthly coupon, final T data) {
      return getValue(coupon, true);
    }

    @Override
    public String visitCouponInflationZeroCouponMonthly(final CouponInflationZeroCouponMonthly coupon) {
      return getValue(coupon, false);
    }

    @Override
    public String visitCouponInflationZeroCouponMonthlyGearing(final CouponInflationZeroCouponMonthlyGearing coupon, final T data) {
      return getValue(coupon, true);
    }

    @Override
    public String visitCouponInflationZeroCouponMonthlyGearing(final CouponInflationZeroCouponMonthlyGearing coupon) {
      return getValue(coupon, false);
    }

    @Override
    public String visitCouponInflationZeroCouponInterpolation(final CouponInflationZeroCouponInterpolation coupon, final T data) {
      return getValue(coupon, true);
    }

    @Override
    public String visitCouponInflationZeroCouponInterpolation(final CouponInflationZeroCouponInterpolation coupon) {
      return getValue(coupon, false);
    }

    @Override
    public String visitCouponInflationZeroCouponInterpolationGearing(final CouponInflationZeroCouponInterpolationGearing coupon, final T data) {
      return getValue(coupon, true);
    }

    @Override
    public String visitCouponInflationZeroCouponInterpolationGearing(final CouponInflationZeroCouponInterpolationGearing coupon) {
      return getValue(coupon, false);
    }

    @Override
    public String visitBondFuture(final BondFuture bondFuture, final T data) {
      return getValue(bondFuture, true);
    }

    @Override
    public String visitBondFuture(final BondFuture future) {
      return getValue(future, false);
    }

    @Override
    public String visitInterestRateFutureTransaction(final InterestRateFutureTransaction future, final T data) {
      return getValue(future, true);
    }

    @Override
    public String visitInterestRateFutureTransaction(final InterestRateFutureTransaction future) {
      return getValue(future, false);
    }

    @Override
    public String visitFederalFundsFutureSecurity(final FederalFundsFutureSecurity future, final T data) {
      return getValue(future, true);
    }

    @Override
    public String visitFederalFundsFutureSecurity(final FederalFundsFutureSecurity future) {
      return getValue(future, false);
    }

    @Override
    public String visitFederalFundsFutureTransaction(final FederalFundsFutureTransaction future, final T data) {
      return getValue(future, true);
    }

    @Override
    public String visitFederalFundsFutureTransaction(final FederalFundsFutureTransaction future) {
      return getValue(future, false);
    }

    @Override
    public String visitSwapFuturesDeliverableSecurity(final SwapFuturesPriceDeliverableSecurity futures, final T data) {
      return getValue(futures, true);
    }

    @Override
    public String visitSwapFuturesDeliverableSecurity(final SwapFuturesPriceDeliverableSecurity futures) {
      return getValue(futures, false);
    }

    @Override
    public String visitBondFutureOptionPremiumSecurity(final BondFutureOptionPremiumSecurity option, final T data) {
      return getValue(option, true);
    }

    @Override
    public String visitBondFutureOptionPremiumSecurity(final BondFutureOptionPremiumSecurity option) {
      return getValue(option, false);
    }

    @Override
    public String visitBondFutureOptionPremiumTransaction(final BondFutureOptionPremiumTransaction option, final T data) {
      return getValue(option, true);
    }

    @Override
    public String visitBondFutureOptionPremiumTransaction(final BondFutureOptionPremiumTransaction option) {
      return getValue(option, false);
    }

    @Override
    public String visitInterestRateFutureOptionMarginSecurity(final InterestRateFutureOptionMarginSecurity option, final T data) {
      return getValue(option, true);
    }

    @Override
    public String visitInterestRateFutureOptionMarginSecurity(final InterestRateFutureOptionMarginSecurity option) {
      return getValue(option, false);
    }

    @Override
    public String visitInterestRateFutureOptionMarginTransaction(final InterestRateFutureOptionMarginTransaction option, final T data) {
      return getValue(option, true);
    }

    @Override
    public String visitInterestRateFutureOptionMarginTransaction(final InterestRateFutureOptionMarginTransaction option) {
      return getValue(option, false);
    }

    @Override
    public String visitInterestRateFutureOptionPremiumSecurity(final InterestRateFutureOptionPremiumSecurity option, final T data) {
      return getValue(option, true);
    }

    @Override
    public String visitInterestRateFutureOptionPremiumSecurity(final InterestRateFutureOptionPremiumSecurity option) {
      return getValue(option, false);
    }

    @Override
    public String visitInterestRateFutureOptionPremiumTransaction(final InterestRateFutureOptionPremiumTransaction option, final T data) {
      return getValue(option, true);
    }

    @Override
    public String visitInterestRateFutureOptionPremiumTransaction(final InterestRateFutureOptionPremiumTransaction option) {
      return getValue(option, false);
    }

    @Override
    public String visitDepositIbor(final DepositIbor deposit, final T data) {
      return getValue(deposit, true);
    }

    @Override
    public String visitDepositIbor(final DepositIbor deposit) {
      return getValue(deposit, false);
    }

    @Override
    public String visitDepositCounterpart(final DepositCounterpart deposit, final T data) {
      return getValue(deposit, true);
    }

    @Override
    public String visitDepositCounterpart(final DepositCounterpart deposit) {
      return getValue(deposit, false);
    }

    @Override
    public String visitDepositZero(final DepositZero deposit, final T data) {
      return getValue(deposit, true);
    }

    @Override
    public String visitDepositZero(final DepositZero deposit) {
      return getValue(deposit, false);
    }

    @Override
    public String visitForex(final Forex derivative, final T data) {
      return getValue(derivative, true);
    }

    @Override
    public String visitForex(final Forex derivative) {
      return getValue(derivative, false);
    }

    @Override
    public String visitForexSwap(final ForexSwap derivative, final T data) {
      return getValue(derivative, true);
    }

    @Override
    public String visitForexSwap(final ForexSwap derivative) {
      return getValue(derivative, false);
    }

    @Override
    public String visitForexOptionVanilla(final ForexOptionVanilla derivative, final T data) {
      return getValue(derivative, true);
    }

    @Override
    public String visitForexOptionVanilla(final ForexOptionVanilla derivative) {
      return getValue(derivative, false);
    }

    @Override
    public String visitForexOptionSingleBarrier(final ForexOptionSingleBarrier derivative, final T data) {
      return getValue(derivative, true);
    }

    @Override
    public String visitForexOptionSingleBarrier(final ForexOptionSingleBarrier derivative) {
      return getValue(derivative, false);
    }

    @Override
    public String visitForexNonDeliverableForward(final ForexNonDeliverableForward derivative, final T data) {
      return getValue(derivative, true);
    }

    @Override
    public String visitForexNonDeliverableForward(final ForexNonDeliverableForward derivative) {
      return getValue(derivative, false);
    }

    @Override
    public String visitForexNonDeliverableOption(final ForexNonDeliverableOption derivative, final T data) {
      return getValue(derivative, true);
    }

    @Override
    public String visitForexNonDeliverableOption(final ForexNonDeliverableOption derivative) {
      return getValue(derivative, false);
    }

    @Override
    public String visitForexOptionDigital(final ForexOptionDigital derivative, final T data) {
      return getValue(derivative, true);
    }

    @Override
    public String visitForexOptionDigital(final ForexOptionDigital derivative) {
      return getValue(derivative, false);
    }

    @Override
    public String visitMetalForward(final MetalForward future, final T data) {
      return getValue(future, true);
    }

    @Override
    public String visitMetalForward(final MetalForward future) {
      return getValue(future, false);
    }

    @Override
    public String visitMetalFuture(final MetalFuture future, final T data) {
      return getValue(future, true);
    }

    @Override
    public String visitMetalFuture(final MetalFuture future) {
      return getValue(future, false);
    }

    @Override
    public String visitMetalFutureOption(final MetalFutureOption future, final T data) {
      return getValue(future, true);
    }

    @Override
    public String visitMetalFutureOption(final MetalFutureOption future) {
      return getValue(future, false);
    }

    @Override
    public String visitAgricultureForward(final AgricultureForward future, final T data) {
      return getValue(future, true);
    }

    @Override
    public String visitAgricultureForward(final AgricultureForward future) {
      return getValue(future, false);
    }

    @Override
    public String visitAgricultureFuture(final AgricultureFuture future, final T data) {
      return getValue(future, true);
    }

    @Override
    public String visitAgricultureFuture(final AgricultureFuture future) {
      return getValue(future, false);
    }

    @Override
    public String visitAgricultureFutureOption(final AgricultureFutureOption future, final T data) {
      return getValue(future, true);
    }

    @Override
    public String visitAgricultureFutureOption(final AgricultureFutureOption future) {
      return getValue(future, false);
    }

    @Override
    public String visitEnergyForward(final EnergyForward future, final T data) {
      return getValue(future, true);
    }

    @Override
    public String visitEnergyForward(final EnergyForward future) {
      return getValue(future, false);
    }

    @Override
    public String visitEnergyFuture(final EnergyFuture future, final T data) {
      return getValue(future, true);
    }

    @Override
    public String visitEnergyFuture(final EnergyFuture future) {
      return getValue(future, false);
    }

    @Override
    public String visitEnergyFutureOption(final EnergyFutureOption future, final T data) {
      return getValue(future, true);
    }

    @Override
    public String visitEnergyFutureOption(final EnergyFutureOption future) {
      return getValue(future, false);
    }

    @Override
    public String visitCouponIborCompounding(final CouponIborCompounding payment) {
      return getValue(payment, false);
    }

    @Override
    public String visitEquityFuture(final EquityFuture future) {
      return getValue(future, false);
    }

    @Override
    public String visitEquityFuture(final EquityFuture future, final T data) {
      return getValue(future, true);
    }

    @Override
    public String visitEquityIndexDividendFuture(final EquityIndexDividendFuture future) {
      return getValue(future, false);
    }

    @Override
    public String visitEquityIndexDividendFuture(final EquityIndexDividendFuture future, final T data) {
      return getValue(future, true);
    }

    @Override
    public String visitEquityIndexOption(final EquityIndexOption option, final T data) {
      return getValue(option, true);
    }

    @Override
    public String visitEquityIndexOption(final EquityIndexOption option) {
      return getValue(option, false);
    }

    @Override
    public String visitEquityIndexFutureOption(final EquityIndexFutureOption option, final T data) {
      return getValue(option, true);
    }

    @Override
    public String visitEquityIndexFutureOption(final EquityIndexFutureOption option) {
      return getValue(option, false);
    }

    @Override
    public String visitEquityOption(final EquityOption option, final T data) {
      return getValue(option, true);
    }

    @Override
    public String visitEquityOption(final EquityOption option) {
      return getValue(option, false);
    }

    @Override
    public String visitVarianceSwap(final VarianceSwap varianceSwap) {
      return getValue(varianceSwap, false);
    }

    @Override
    public String visitVarianceSwap(final VarianceSwap varianceSwap, final T data) {
      return getValue(varianceSwap, true);
    }

    @Override
    public String visitEquityVarianceSwap(final EquityVarianceSwap varianceSwap) {
      return getValue(varianceSwap, false);
    }

    @Override
    public String visitEquityVarianceSwap(final EquityVarianceSwap varianceSwap, final T data) {
      return getValue(varianceSwap, true);
    }

    @Override
    public String visitCouponIborCompoundingSpread(CouponIborCompoundingSpread payment) {
      return null;
    }

    @Override
    public String visitCouponIborCompoundingSpread(CouponIborCompoundingSpread payment, T data) {
      return null;
    }

    @Override
    public String visitCouponIborAverage(CouponIborAverage payment, T data) {
      return null;
    }

    @Override
    public String visitCouponIborAverage(CouponIborAverage payment) {
      return null;
    }

    @Override
    public String visitCouponInflationYearOnYearMonthly(CouponInflationYearOnYearMonthly coupon, T data) {
      return null;
    }

    @Override
    public String visitCouponInflationYearOnYearMonthly(CouponInflationYearOnYearMonthly coupon) {
      return null;
    }

    @Override
    public String visitCouponInflationYearOnYearInterpolation(CouponInflationYearOnYearInterpolation coupon, T data) {
      return null;
    }

    @Override
    public String visitCouponInflationYearOnYearInterpolation(CouponInflationYearOnYearInterpolation coupon) {
      return null;
    }

    @Override
    public String visitInterestRateFutureSecurity(InterestRateFutureSecurity future, T data) {
      return null;
    }

    @Override
    public String visitCouponFixedCompounding(CouponFixedCompounding payment, T data) {
      return null;
    }

    @Override
    public String visitInterestRateFutureSecurity(InterestRateFutureSecurity future) {
      return null;
    }

    @Override
    public String visitCouponFixedCompounding(CouponFixedCompounding payment) {
      return null;
    }

    @Override
    public String visitFixedCompoundingCouponSwap(SwapFixedCompoundingCoupon<?> swap, T data) {
      return null;
    }

    @Override
    public String visitFixedCompoundingCouponSwap(SwapFixedCompoundingCoupon<?> swap) {
      return null;
    }

    @Override
    public String visitCashSettledFuture(CashSettledFuture future, T data) {
      return getValue(future, true);
    }

    @Override
    public String visitCashSettledFuture(CashSettledFuture future) {
      return getValue(future, false);
    }

    @Override
    public String visitIndexFuture(IndexFuture future, T data) {
      return getValue(future, true);
    }

    @Override
    public String visitIndexFuture(IndexFuture future) {
      return getValue(future, false);
    }

    @Override
    public String visitEquityIndexFuture(EquityIndexFuture future, T data) {
      return getValue(future, true);
    }

    @Override
    public String visitEquityIndexFuture(EquityIndexFuture future) {
      return getValue(future, false);// TODO Auto-generated method stub
    }

    @Override
    public String visitVolatilityIndexFuture(VolatilityIndexFuture future, T data) {
      return getValue(future, true);
    }

    @Override
    public String visitVolatilityIndexFuture(VolatilityIndexFuture future) {
      return getValue(future, false);
    }

    @Override
    public String visitCouponArithmeticAverageON(CouponArithmeticAverageON payment, T data) {
      return null;
    }

    @Override
    public String visitCouponArithmeticAverageON(CouponArithmeticAverageON payment) {
      return null;
    }

    @Override
    public String visitSwapFuturesDeliverableTransaction(SwapFuturesPriceDeliverableTransaction futures, T data) {
      return null;
    }

    @Override
    public String visitSwapFuturesDeliverableTransaction(SwapFuturesPriceDeliverableTransaction futures) {
      return null;
    }

    @Override
    public String visitCapFloorInflationZeroCouponInterpolation(CapFloorInflationZeroCouponInterpolation coupon, T data) {
      return null;
    }

    @Override
    public String visitCapFloorInflationZeroCouponInterpolation(CapFloorInflationZeroCouponInterpolation coupon) {
      return null;
    }

    @Override
    public String visitCapFloorInflationZeroCouponMonthly(CapFloorInflationZeroCouponMonthly coupon, T data) {
      return null;
    }

    @Override
    public String visitCapFloorInflationZeroCouponMonthly(CapFloorInflationZeroCouponMonthly coupon) {
      return null;
    }

    @Override
    public String visitCapFloorInflationYearOnYearInterpolation(CapFloorInflationYearOnYearInterpolation coupon, T data) {
      return null;
    }

    @Override
    public String visitCapFloorInflationYearOnYearInterpolation(CapFloorInflationYearOnYearInterpolation coupon) {
      return null;
    }

    @Override
    public String visitCapFloorInflationYearOnYearMonthly(CapFloorInflationYearOnYearMonthly coupon, T data) {
      return null;
    }

    @Override
    public String visitCapFloorInflationYearOnYearMonthly(CapFloorInflationYearOnYearMonthly coupon) {
      return null;
    }

    @Override
    public String visitCouponArithmeticAverageONSpread(CouponArithmeticAverageONSpread payment, T data) {
      return null;
    }

    @Override
    public String visitCouponArithmeticAverageONSpread(CouponArithmeticAverageONSpread payment) {
      return null;
    }

    @Override
    public String visitCouponArithmeticAverageONSpreadSimplified(CouponArithmeticAverageONSpreadSimplified payment, T data) {
      return null;
    }

    @Override
    public String visitCouponArithmeticAverageONSpreadSimplified(CouponArithmeticAverageONSpreadSimplified payment) {
      return null;
    }

    @Override
    public String visitBondFuturesSecurity(BondFuturesSecurity bondFutures, T data) {
      return null;
    }

    @Override
    public String visitBondFuturesSecurity(BondFuturesSecurity bondFutures) {
      return null;
    }

    @Override
    public String visitBondFuturesTransaction(BondFuturesTransaction bondFutures, T data) {
      return null;
    }

    @Override
    public String visitBondFuturesTransaction(BondFuturesTransaction bondFutures) {
      return null;
    }

    @Override
    public String visitCouponInflationYearOnYearMonthlyWithMargin(CouponInflationYearOnYearMonthlyWithMargin coupon, T data) {
      return null;
    }

    @Override
    public String visitCouponInflationYearOnYearMonthlyWithMargin(CouponInflationYearOnYearMonthlyWithMargin coupon) {
      return null;
    }

    @Override
    public String visitCouponInflationYearOnYearInterpolationWithMargin(CouponInflationYearOnYearInterpolationWithMargin coupon, T data) {
      return null;
    }

    @Override
    public String visitCouponInflationYearOnYearInterpolationWithMargin(CouponInflationYearOnYearInterpolationWithMargin coupon) {
      return null;
    }

    @Override
    public String visitBondInterestIndexedSecurity(BondInterestIndexedSecurity<?, ?> bond) {
      return null;
    }

    @Override
    public String visitBondInterestIndexedSecurity(BondInterestIndexedSecurity<?, ?> bond, T data) {
      return null;
    }

    @Override
    public String visitBondInterestIndexedTransaction(BondInterestIndexedTransaction<?, ?> bond, T data) {
      return null;
    }

    @Override
    public String visitBondInterestIndexedTransaction(BondInterestIndexedTransaction<?, ?> bond) {
      return null;
    }
  }

}

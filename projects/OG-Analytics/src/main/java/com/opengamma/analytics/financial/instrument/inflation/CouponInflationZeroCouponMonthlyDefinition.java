/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.inflation;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationZeroCouponMonthly;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Class describing an zero-coupon inflation coupon where the inflation figures are the one of the reference month and are not interpolated.
 * The start index value is known when the coupon is traded/issued.
 * The index for a given month is given in the yield curve and in the time series on the first of the month.
 * The pay-off is (Index_End / Index_Start - X) with X=0 for notional payment and X=1 for no notional payment.
 */
public class CouponInflationZeroCouponMonthlyDefinition extends CouponInflationDefinition {

  /**
   * The reference date for the index at the coupon start. May not be relevant as the index value is known.
   */
  private final ZonedDateTime _referenceStartDate;
  /**
   * The index value at the start of the coupon.
   */
  private final double _indexStartValue;
  /**
   * The reference date for the index at the coupon end. The first of the month. There is usually a difference of two or three month between the reference date and the payment date.
   */
  private final ZonedDateTime _referenceEndDate;
  /**
   * Flag indicating if the notional is paid (true) or not (false).
   */
  private final boolean _payNotional;
  /**
   * The lag in month between the index validity and the coupon dates for the standard product (the one in exchange market and used for the calibration, this lag is in most cases 3 month).
   */
  private final int _conventionalMonthLag;

  /**
   * The lag in month between the index validity and the coupon dates for the actual product. (In most of the cases,lags are standard so _conventionalMonthLag=_monthLag)
   */
  private final int _monthLag;

  /**
   * Constructor for zero-coupon inflation coupon.
   * @param currency The coupon currency.
   * @param paymentDate The payment date.
   * @param accrualStartDate Start date of the accrual period.
   * @param accrualEndDate End date of the accrual period.
   * @param paymentYearFraction Accrual factor of the accrual period.
   * @param notional Coupon notional.
   * @param priceIndex The price index associated to the coupon.
   * @param conventionalMonthLag The lag in month between the index validity and the coupon dates for the standard product.
   * @param monthLag The lag in month between the index validity and the coupon dates for the actual product.
   * @param referenceStartDate The reference date for the index at the coupon start.
   * @param indexStartValue The index value at the start of the coupon.
   * @param referenceEndDate The reference date for the index at the coupon end.
   * @param payNotional Flag indicating if the notional is paid (true) or not (false).
   */
  public CouponInflationZeroCouponMonthlyDefinition(final Currency currency, final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate,
      final ZonedDateTime accrualEndDate, final double paymentYearFraction, final double notional, final IndexPrice priceIndex, final int conventionalMonthLag,
      final int monthLag, final ZonedDateTime referenceStartDate, final double indexStartValue, final ZonedDateTime referenceEndDate, final boolean payNotional) {
    super(currency, paymentDate, accrualStartDate, accrualEndDate, paymentYearFraction, notional, priceIndex);
    ArgumentChecker.notNull(referenceStartDate, "Reference start date");
    ArgumentChecker.notNull(referenceEndDate, "Reference end date");
    _referenceStartDate = referenceStartDate;
    _indexStartValue = indexStartValue;
    _referenceEndDate = referenceEndDate;
    _payNotional = payNotional;
    _conventionalMonthLag = conventionalMonthLag;
    _monthLag = monthLag;
  }

  /**
   * Builder for inflation zero-coupon.
   * The accrualStartDate is used for the referenceStartDate. The paymentDate is used for accrualEndDate. The paymentYearFraction is 1.0. The notional is not paid in the coupon.
   * @param accrualStartDate Start date of the accrual period.
   * @param paymentDate The payment date.
   * @param notional Coupon notional.
   * @param priceIndex The price index associated to the coupon.
   * @param conventionalMonthLag The lag in month between the index validity and the coupon dates for the standard product.
   * @param monthLag The lag in month between the index validity and the coupon dates for the actual product.
   * @param indexStartValue The index value at the start of the coupon.
   * @param referenceEndDate The reference date for the index at the coupon end.
   * @return The coupon.
   */
  public static CouponInflationZeroCouponMonthlyDefinition from(final ZonedDateTime accrualStartDate, final ZonedDateTime paymentDate, final double notional,
      final IndexPrice priceIndex, final int conventionalMonthLag, final int monthLag, final double indexStartValue, final ZonedDateTime referenceEndDate) {
    ArgumentChecker.notNull(priceIndex, "Price index");
    return new CouponInflationZeroCouponMonthlyDefinition(priceIndex.getCurrency(), paymentDate, accrualStartDate, paymentDate, 1.0, notional, priceIndex, conventionalMonthLag,
        monthLag, accrualStartDate, indexStartValue, referenceEndDate, false);
  }

  /**
   * Builder for inflation zero-coupon based on an inflation lag and index publication. The fixing date is the publication lag after the last reference month.
   * @param accrualStartDate Start date of the accrual period.
   * @param paymentDate The payment date.
   * @param notional Coupon notional.
   * @param priceIndex The price index associated to the coupon.
   * @param indexStartValue The index value at the start of the coupon.
   * @param conventionalMonthLag The lag in month between the index validity and the coupon dates for the standard product.
   * @param monthLag The lag in month between the index validity and the coupon dates for the standard product.
   * @param payNotional Flag indicating if the notional is paid (true) or not (false).
   * @return The inflation zero-coupon.
   */
  public static CouponInflationZeroCouponMonthlyDefinition from(final ZonedDateTime accrualStartDate, final ZonedDateTime paymentDate, final double notional,
      final IndexPrice priceIndex, final double indexStartValue, final int conventionalMonthLag, final int monthLag, final boolean payNotional) {
    ZonedDateTime referenceStartDate = accrualStartDate.minusMonths(monthLag);
    ZonedDateTime referenceEndDate = paymentDate.minusMonths(monthLag);
    referenceStartDate = referenceStartDate.withDayOfMonth(1);
    referenceEndDate = referenceEndDate.withDayOfMonth(1);

    return new CouponInflationZeroCouponMonthlyDefinition(priceIndex.getCurrency(), paymentDate, accrualStartDate, paymentDate, 1.0, notional, priceIndex, conventionalMonthLag,
        monthLag, referenceStartDate, indexStartValue, referenceEndDate, payNotional);
  }

  /**
   * Builder for inflation zero-coupon based on an inflation lag and the index publication lag. The fixing date is the publication lag after the last reference month.
   * The index start value is calculated from a time series. The index value required for the coupon should be in the time series.
   * @param accrualStartDate Start date of the accrual period.
   * @param paymentDate The payment date.
   * @param notional Coupon notional.
   * @param priceIndex The price index associated to the coupon.
   * @param priceIndexTimeSeries The time series with the relevant price index values.
   * @param conventionalMonthLag The lag in month between the index validity and the coupon dates for the standard product.
   * @param monthlag The lag in month between the index validity and the coupon dates for the actual product.
   * @param payNotional Flag indicating if the notional is paid (true) or not (false).
   * @return The inflation zero-coupon.
   */
  public static CouponInflationZeroCouponMonthlyDefinition from(final ZonedDateTime accrualStartDate, final ZonedDateTime paymentDate, final double notional,
      final IndexPrice priceIndex, final DoubleTimeSeries<ZonedDateTime> priceIndexTimeSeries, final int conventionalMonthLag, final int monthlag, final boolean payNotional) {
    final ZonedDateTime refInterpolatedDate = accrualStartDate.minusMonths(monthlag);
    final ZonedDateTime referenceStartDate;
    referenceStartDate = refInterpolatedDate.withDayOfMonth(1);
    final Double indexStartValue = priceIndexTimeSeries.getValue(referenceStartDate);
    ArgumentChecker.notNull(indexStartValue, "price index fixing"); // Fixing not known
    return from(accrualStartDate, paymentDate, notional, priceIndex, indexStartValue, conventionalMonthLag, monthlag, payNotional);
  }

  /**
   * Gets the reference date for the index at the coupon start.
   * @return The reference date for the index at the coupon start.
   */
  public ZonedDateTime getReferenceStartDate() {
    return _referenceStartDate;
  }

  /**
   * Gets the index value at the start of the coupon.
   * @return The index value at the start of the coupon.
   */
  public double getIndexStartValue() {
    return _indexStartValue;
  }

  /**
   * Gets the reference date for the index at the coupon end.
   * @return The reference date for the index at the coupon end.
   */
  public ZonedDateTime getReferenceEndDate() {
    return _referenceEndDate;
  }

  /**
   * Gets the pay notional flag.
   * @return The flag.
   */
  public boolean payNotional() {
    return _payNotional;
  }

  /**
   * Gets the lag in month between the index validity and the coupon dates for the standard product.
   * @return The lag.
   */
  public int getConventionalMonthLag() {
    return _conventionalMonthLag;
  }

  /**
   * Gets the lag in month between the index validity and the coupon dates for the actual product.
   * @return The lag.
   */
  public int getMonthLag() {
    return _monthLag;
  }

  @Override
  public CouponInflationDefinition with(final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate, final double notional) {
    final ZonedDateTime refInterpolatedDate = accrualEndDate.minusMonths(_conventionalMonthLag);
    final ZonedDateTime referenceEndDate = refInterpolatedDate.withDayOfMonth(1);
    return new CouponInflationZeroCouponMonthlyDefinition(getCurrency(), paymentDate, accrualStartDate, accrualEndDate, getPaymentYearFraction(), getNotional(),
        getPriceIndex(), _conventionalMonthLag, 3, getReferenceStartDate(), getIndexStartValue(), referenceEndDate, payNotional());
  }

  @Override
  public CouponInflationZeroCouponMonthly toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.isTrue(!date.isAfter(getPaymentDate()), "Do not have any fixing data but are asking for a derivative after the payment date");
    ArgumentChecker.notNull(yieldCurveNames, "yield curve names");
    ArgumentChecker.isTrue(yieldCurveNames.length > 0, "at least one curve required");
    ArgumentChecker.isTrue(!date.isAfter(getPaymentDate()), "date is after payment date");
    final double paymentTime = TimeCalculator.getTimeBetween(date, getPaymentDate());
    final double referenceEndTime = TimeCalculator.getTimeBetween(date, getReferenceEndDate());
    final ZonedDateTime naturalPaymentDate = getPaymentDate().minusMonths(_monthLag - _conventionalMonthLag);
    final double naturalPaymentTime = TimeCalculator.getTimeBetween(date, naturalPaymentDate);
    return new CouponInflationZeroCouponMonthly(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), getPriceIndex(), _indexStartValue, referenceEndTime, naturalPaymentTime,
        _payNotional);
  }

  @Override
  public Coupon toDerivative(final ZonedDateTime date, final DoubleTimeSeries<ZonedDateTime> priceIndexTimeSeries, final String... yieldCurveNames) {
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNull(yieldCurveNames, "yield curve names");
    ArgumentChecker.isTrue(yieldCurveNames.length > 0, "at least one curve required");
    ArgumentChecker.isTrue(!date.isAfter(getPaymentDate()), "date is after payment date");
    final LocalDate dayConversion = date.toLocalDate();
    final String discountingCurveName = yieldCurveNames[0];
    final double paymentTime = TimeCalculator.getTimeBetween(date, getPaymentDate());
    final LocalDate dayFixing = getReferenceEndDate().toLocalDate();
    if (dayConversion.isAfter(dayFixing)) {
      final Double fixedEndIndex = priceIndexTimeSeries.getValue(getReferenceEndDate());
      if (fixedEndIndex != null) {
        final Double fixedRate = (fixedEndIndex / getIndexStartValue() - (payNotional() ? 0.0 : 1.0));
        return new CouponFixed(getCurrency(), paymentTime, discountingCurveName, getPaymentYearFraction(), getNotional(), fixedRate);
      }
    }
    double referenceEndTime = 0.0;
    referenceEndTime = TimeCalculator.getTimeBetween(date, _referenceEndDate);
    final ZonedDateTime naturalPaymentDate = getPaymentDate().minusMonths(_monthLag - _conventionalMonthLag);
    final double naturalPaymentTime = TimeCalculator.getTimeBetween(date, naturalPaymentDate);
    return new CouponInflationZeroCouponMonthly(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), getPriceIndex(), _indexStartValue, referenceEndTime, naturalPaymentTime,
        _payNotional);
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponInflationZeroCouponFirstOfMonth(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponInflationZeroCouponFirstOfMonth(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _conventionalMonthLag;
    long temp;
    temp = Double.doubleToLongBits(_indexStartValue);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _monthLag;
    result = prime * result + (_payNotional ? 1231 : 1237);
    result = prime * result + ((_referenceEndDate == null) ? 0 : _referenceEndDate.hashCode());
    result = prime * result + ((_referenceStartDate == null) ? 0 : _referenceStartDate.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final CouponInflationZeroCouponMonthlyDefinition other = (CouponInflationZeroCouponMonthlyDefinition) obj;
    if (_conventionalMonthLag != other._conventionalMonthLag) {
      return false;
    }
    if (Double.doubleToLongBits(_indexStartValue) != Double.doubleToLongBits(other._indexStartValue)) {
      return false;
    }
    if (_monthLag != other._monthLag) {
      return false;
    }
    if (_payNotional != other._payNotional) {
      return false;
    }
    if (_referenceEndDate == null) {
      if (other._referenceEndDate != null) {
        return false;
      }
    } else if (!_referenceEndDate.equals(other._referenceEndDate)) {
      return false;
    }
    if (_referenceStartDate == null) {
      if (other._referenceStartDate != null) {
        return false;
      }
    } else if (!_referenceStartDate.equals(other._referenceStartDate)) {
      return false;
    }
    return true;
  }

}

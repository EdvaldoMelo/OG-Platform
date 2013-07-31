/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.curve;

import static com.opengamma.engine.value.ValuePropertyNames.CURRENCY;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CONSTRUCTION_CONFIG;
import static com.opengamma.engine.value.ValueRequirementNames.YIELD_CURVE;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.HULL_WHITE_DISCOUNTING;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.PROPERTY_HULL_WHITE_PARAMETERS;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveYieldInterpolated;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorYDCurve;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantParameters;
import com.opengamma.analytics.financial.provider.calculator.generic.LastTimeCalculator;
import com.opengamma.analytics.financial.provider.calculator.hullwhite.ParSpreadMarketQuoteCurveSensitivityHullWhiteCalculator;
import com.opengamma.analytics.financial.provider.calculator.hullwhite.ParSpreadMarketQuoteHullWhiteCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.hullwhite.HullWhiteProviderDiscountBuildingRepository;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteOneFactorProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteOneFactorProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.region.RegionSource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.conversion.CurveNodeConverter;
import com.opengamma.financial.analytics.curve.CashNodeConverter;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.analytics.curve.CurveDefinition;
import com.opengamma.financial.analytics.curve.CurveGroupConfiguration;
import com.opengamma.financial.analytics.curve.CurveNodeVisitorAdapter;
import com.opengamma.financial.analytics.curve.CurveSpecification;
import com.opengamma.financial.analytics.curve.CurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.DiscountingCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.FRANodeConverter;
import com.opengamma.financial.analytics.curve.FXForwardNodeConverter;
import com.opengamma.financial.analytics.curve.IborCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.InterpolatedCurveDefinition;
import com.opengamma.financial.analytics.curve.OvernightCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.RateFutureNodeConverter;
import com.opengamma.financial.analytics.curve.SwapNodeConverter;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeVisitor;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeWithIdentifier;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.convention.Convention;
import com.opengamma.financial.convention.ConventionSource;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * Produces yield curves using the Hull-White one-factor discounting method.
 */
public class HullWhiteOneFactorDiscountingCurveFunction extends
  MultiCurveFunction<HullWhiteOneFactorProviderInterface, HullWhiteProviderDiscountBuildingRepository, GeneratorYDCurve, MulticurveSensitivity> {
  /** The calculator */
  private static final ParSpreadMarketQuoteHullWhiteCalculator PSMQHWC = ParSpreadMarketQuoteHullWhiteCalculator.getInstance();
  /** The sensitivity calculator */
  private static final ParSpreadMarketQuoteCurveSensitivityHullWhiteCalculator PSMQCSHWC = ParSpreadMarketQuoteCurveSensitivityHullWhiteCalculator.getInstance();
  /** The maturity calculator */
  private static final LastTimeCalculator MATURITY_CALCULATOR = LastTimeCalculator.getInstance();

  /**
   * @param configurationName The configuration name, not null
   */
  public HullWhiteOneFactorDiscountingCurveFunction(final String configurationName) {
    super(configurationName);
  }

  @Override
  public CompiledFunctionDefinition getCompiledFunction(final ZonedDateTime earliestInvokation, final ZonedDateTime latestInvokation, final String[] curveNames,
      final Set<ValueRequirement> exogenousRequirements, final CurveConstructionConfiguration curveConstructionConfiguration) {
    return new MyCompiledFunctionDefinition(earliestInvokation, latestInvokation, curveNames, exogenousRequirements, curveConstructionConfiguration);
  }

  /**
   * Compiled function implementation.
   */
  protected class MyCompiledFunctionDefinition extends CurveCompiledFunctionDefinition {
    private final Set<ValueRequirement> _exogenousRequirements;
    private final CurveConstructionConfiguration _curveConstructionConfiguration;

    protected MyCompiledFunctionDefinition(final ZonedDateTime earliestInvokation, final ZonedDateTime latestInvokation, final String[] curveNames,
        final Set<ValueRequirement> exogenousRequirements, final CurveConstructionConfiguration curveConstructionConfiguration) {
      super(earliestInvokation, latestInvokation, curveNames, ValueRequirementNames.YIELD_CURVE, exogenousRequirements);
      _curveConstructionConfiguration = curveConstructionConfiguration;
      _exogenousRequirements = exogenousRequirements;
    }

    @Override
    @SuppressWarnings("synthetic-access")
    protected Pair<HullWhiteOneFactorProviderInterface, CurveBuildingBlockBundle> getCurves(final FunctionInputs inputs, final ZonedDateTime now,
        final HullWhiteProviderDiscountBuildingRepository builder, final HullWhiteOneFactorProviderInterface knownData, final ConventionSource conventionSource,
        final HolidaySource holidaySource, final RegionSource regionSource) {
      final ValueProperties curveConstructionProperties = ValueProperties.builder()
          .with(CURVE_CONSTRUCTION_CONFIG, _curveConstructionConfiguration.getName())
          .get();
      final HistoricalTimeSeriesBundle timeSeries =
          (HistoricalTimeSeriesBundle) inputs.getValue(new ValueRequirement(ValueRequirementNames.CURVE_INSTRUMENT_CONVERSION_HISTORICAL_TIME_SERIES,
              ComputationTargetSpecification.NULL, curveConstructionProperties));
      final int nGroups = _curveConstructionConfiguration.getCurveGroups().size();
      final InstrumentDerivative[][][] definitions = new InstrumentDerivative[nGroups][][];
      final GeneratorYDCurve[][] curveGenerators = new GeneratorYDCurve[nGroups][];
      final String[][] curves = new String[nGroups][];
      final double[][] parameterGuess = new double[nGroups][];
      final LinkedHashMap<String, Currency> discountingMap = new LinkedHashMap<>();
      final LinkedHashMap<String, IborIndex[]> forwardIborMap = new LinkedHashMap<>();
      final LinkedHashMap<String, IndexON[]> forwardONMap = new LinkedHashMap<>();
      //TODO comparator to sort groups by order
      int i = 0; // Implementation Note: loop on the groups
      for (final CurveGroupConfiguration group : _curveConstructionConfiguration.getCurveGroups()) { // Group - start
        int j = 0;
        final int nCurves = group.getTypesForCurves().size();
        definitions[i] = new InstrumentDerivative[nCurves][];
        curveGenerators[i] = new GeneratorYDCurve[nCurves];
        curves[i] = new String[nCurves];
        parameterGuess[i] = new double[nCurves];
        final DoubleArrayList parameterGuessForCurves = new DoubleArrayList();
        for (final Map.Entry<String, List<CurveTypeConfiguration>> entry : group.getTypesForCurves().entrySet()) {
          final List<IborIndex> iborIndex = new ArrayList<>();
          final List<IndexON> overnightIndex = new ArrayList<>();
          final String curveName = entry.getKey();
          final ValueProperties properties = ValueProperties.builder().with(CURVE, curveName).get();
          final CurveSpecification specification =
              (CurveSpecification) inputs.getValue(new ValueRequirement(ValueRequirementNames.CURVE_SPECIFICATION, ComputationTargetSpecification.NULL, properties));
          final CurveDefinition definition =
              (CurveDefinition) inputs.getValue(new ValueRequirement(ValueRequirementNames.CURVE_DEFINITION, ComputationTargetSpecification.NULL, properties));
          final SnapshotDataBundle snapshot =
              (SnapshotDataBundle) inputs.getValue(new ValueRequirement(ValueRequirementNames.CURVE_MARKET_DATA, ComputationTargetSpecification.NULL, properties));
          final int nNodes = specification.getNodes().size();
          final InstrumentDerivative[] derivativesForCurve = new InstrumentDerivative[nNodes];
          final double[] marketDataForCurve = new double[nNodes];
          int k = 0;
          for (final CurveNodeWithIdentifier node : specification.getNodes()) { // Node points - start
            final Double marketData = snapshot.getDataPoint(node.getIdentifier());
            if (marketData == null) {
              throw new OpenGammaRuntimeException("Could not get market data for " + node.getIdentifier());
            }
            marketDataForCurve[k] = marketData;
            parameterGuessForCurves.add(marketData);
            final InstrumentDefinition<?> definitionForNode = node.getCurveNode().accept(getCurveNodeConverter(conventionSource, holidaySource, regionSource, snapshot,
                node.getIdentifier(), timeSeries, now));
            derivativesForCurve[k++] = CurveNodeConverter.getDerivative(node, definitionForNode, now, timeSeries);
          } // Node points - end
          for (final CurveTypeConfiguration type : entry.getValue()) { // Type - start
            if (type instanceof DiscountingCurveTypeConfiguration) {
              final String reference = ((DiscountingCurveTypeConfiguration) type).getReference();
              try {
                final Currency currency = Currency.of(reference);
                //should this map check that the curve name has not already been entered?
                discountingMap.put(curveName, currency);
              } catch (final IllegalArgumentException e) {
                throw new OpenGammaRuntimeException("Cannot handle reference type " + reference + " for discounting curves");
              }
            } else if (type instanceof IborCurveTypeConfiguration) {
              final IborCurveTypeConfiguration ibor = (IborCurveTypeConfiguration) type;
              final Convention convention = conventionSource.getConvention(ibor.getConvention());
              if (!(convention instanceof IborIndexConvention)) {
                throw new OpenGammaRuntimeException("Expecting convention of type IborIndexConvention; have " + convention.getClass());
              }
              final IborIndexConvention iborIndexConvention = (IborIndexConvention) convention;
              final int spotLag = 0; //TODO
              iborIndex.add(new IborIndex(iborIndexConvention.getCurrency(), ibor.getTenor().getPeriod(), spotLag, iborIndexConvention.getDayCount(),
                  iborIndexConvention.getBusinessDayConvention(), iborIndexConvention.isIsEOM(), iborIndexConvention.getName()));
            } else if (type instanceof OvernightCurveTypeConfiguration) {
              final OvernightCurveTypeConfiguration overnight = (OvernightCurveTypeConfiguration) type;
              final Convention convention = conventionSource.getConvention(overnight.getConvention());
              if (!(convention instanceof OvernightIndexConvention)) {
                throw new OpenGammaRuntimeException("Expecting convention of type OvernightIndexConvention; have " + convention.getClass());
              }
              final OvernightIndexConvention overnightConvention = (OvernightIndexConvention) convention;
              overnightIndex.add(new IndexON(overnightConvention.getName(), overnightConvention.getCurrency(), overnightConvention.getDayCount(), overnightConvention.getPublicationLag()));
            } else {
              throw new OpenGammaRuntimeException("Cannot handle " + type.getClass());
            }
          } // type - end
          if (!iborIndex.isEmpty()) {
            forwardIborMap.put(curveName, iborIndex.toArray(new IborIndex[iborIndex.size()]));
          }
          if (!overnightIndex.isEmpty()) {
            forwardONMap.put(curveName, overnightIndex.toArray(new IndexON[overnightIndex.size()]));
          }
          definitions[i][j] = derivativesForCurve;
          curveGenerators[i][j] = getGenerator(definition);
          curves[i][j] = curveName;
          parameterGuess[i] = parameterGuessForCurves.toDoubleArray();
          j++;
        }
        i++;
      } // Group - end
      //TODO this is only in here because the code in analytics doesn't use generics properly
      final Pair<HullWhiteOneFactorProviderDiscount, CurveBuildingBlockBundle> temp = builder.makeCurvesFromDerivatives(definitions, curveGenerators, curves, parameterGuess,
          (HullWhiteOneFactorProviderDiscount) knownData, discountingMap, forwardIborMap, forwardONMap, getCalculator(), getSensitivityCalculator());
      final Pair<HullWhiteOneFactorProviderInterface, CurveBuildingBlockBundle> result = Pair.of((HullWhiteOneFactorProviderInterface) temp.getFirst(), temp.getSecond());
      return result;
    }

    @Override
    public Set<ValueRequirement> getRequirements(final FunctionCompilationContext compilationContext, final ComputationTarget target, final ValueRequirement desiredValue) {
      final Set<ValueRequirement> requirements = super.getRequirements(compilationContext, target, desiredValue);
      if (requirements == null) {
        return null;
      }
      final ValueProperties constraints = desiredValue.getConstraints();
      final Set<String> hwPropertyNames = constraints.getValues(PROPERTY_HULL_WHITE_PARAMETERS);
      if (hwPropertyNames == null || hwPropertyNames.size() != 1) {
        return null;
      }
      final Set<String> hwCurrencies = constraints.getValues(CURRENCY);
      if (hwCurrencies == null || hwCurrencies.size() != 1) {
        return null;
      }
      final ValueProperties hwProperties = ValueProperties.builder()
          .with(PROPERTY_HULL_WHITE_PARAMETERS, hwPropertyNames)
          .with(CURRENCY, hwCurrencies)
          .get();
      requirements.add(new ValueRequirement(ValueRequirementNames.HULL_WHITE_ONE_FACTOR_PARAMETERS, ComputationTargetSpecification.NULL, hwProperties));
      return requirements;
    }

    @Override
    protected InstrumentDerivativeVisitor<HullWhiteOneFactorProviderInterface, Double> getCalculator() {
      return PSMQHWC;
    }

    @Override
    protected InstrumentDerivativeVisitor<HullWhiteOneFactorProviderInterface, MulticurveSensitivity> getSensitivityCalculator() {
      return PSMQCSHWC;
    }

    @Override
    protected String getCurveTypeProperty() {
      return HULL_WHITE_DISCOUNTING;
    }

    @Override
    protected ValueProperties getCurveProperties(final String curveName) {
      return super.getCurveProperties(curveName).copy()
          .withAny(PROPERTY_HULL_WHITE_PARAMETERS)
          .withAny(CURRENCY)
          .get();
    }

    @Override
    protected ValueProperties getBundleProperties() {
      return super.getBundleProperties().copy()
          .withAny(PROPERTY_HULL_WHITE_PARAMETERS)
          .withAny(CURRENCY)
          .get();
    }

    @Override
    protected HullWhiteOneFactorProviderInterface getKnownData(final FunctionInputs inputs) {
      final HullWhiteOneFactorPiecewiseConstantParameters modelParameters = (HullWhiteOneFactorPiecewiseConstantParameters) inputs.getValue(ValueRequirementNames.HULL_WHITE_ONE_FACTOR_PARAMETERS);
      Currency currency = null;
      for (final ComputedValue input : inputs.getAllValues()) {
        if (input.getSpecification().getValueName().equals(ValueRequirementNames.HULL_WHITE_ONE_FACTOR_PARAMETERS)) {
          currency = Currency.of(input.getSpecification().getProperty(ValuePropertyNames.CURRENCY));
          break;
        }
      }
      if (currency == null) {
        throw new OpenGammaRuntimeException("Could not get the currency for this set of Hull-White one factor parameters");
      }
      final FXMatrix fxMatrix = (FXMatrix) inputs.getValue(ValueRequirementNames.FX_MATRIX);
      HullWhiteOneFactorProviderDiscount knownData;
      if (_exogenousRequirements.isEmpty()) {
        knownData = new HullWhiteOneFactorProviderDiscount(new MulticurveProviderDiscount(fxMatrix), modelParameters, currency);
      } else {
        final Object curveBundle = inputs.getValue(ValueRequirementNames.CURVE_BUNDLE);
        if (curveBundle instanceof MulticurveProviderDiscount) {
          knownData = new HullWhiteOneFactorProviderDiscount((MulticurveProviderDiscount) curveBundle, modelParameters, currency);
        }
        knownData = (HullWhiteOneFactorProviderDiscount) inputs.getValue(ValueRequirementNames.CURVE_BUNDLE);
      }
      return knownData;
    }

    @Override
    protected HullWhiteProviderDiscountBuildingRepository getBuilder(final double absoluteTolerance, final double relativeTolerance, final int maxIterations) {
      return new HullWhiteProviderDiscountBuildingRepository(absoluteTolerance, relativeTolerance, maxIterations);
    }

    @Override
    protected GeneratorYDCurve getGenerator(final CurveDefinition definition) {
      if (definition instanceof InterpolatedCurveDefinition) {
        final InterpolatedCurveDefinition interpolatedDefinition = (InterpolatedCurveDefinition) definition;
        final String interpolatorName = interpolatedDefinition.getInterpolatorName();
        final String leftExtrapolatorName = interpolatedDefinition.getLeftExtrapolatorName();
        final String rightExtrapolatorName = interpolatedDefinition.getRightExtrapolatorName();
        final Interpolator1D interpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(interpolatorName, leftExtrapolatorName, rightExtrapolatorName);
        return new GeneratorCurveYieldInterpolated(MATURITY_CALCULATOR, interpolator);
      }
      throw new OpenGammaRuntimeException("Cannot handle curves of type " + definition.getClass());
    }

    @Override
    protected CurveNodeVisitor<InstrumentDefinition<?>> getCurveNodeConverter(final ConventionSource conventionSource, final HolidaySource holidaySource, final RegionSource regionSource,
        final SnapshotDataBundle marketData, final ExternalId dataId, final HistoricalTimeSeriesBundle historicalData, final ZonedDateTime valuationTime) {
      return CurveNodeVisitorAdapter.<InstrumentDefinition<?>>builder()
          .cashNodeVisitor(new CashNodeConverter(conventionSource, holidaySource, regionSource, marketData, dataId, valuationTime))
          .fraNode(new FRANodeConverter(conventionSource, holidaySource, regionSource, marketData, dataId, valuationTime))
          .fxForwardNode(new FXForwardNodeConverter(conventionSource, holidaySource, regionSource, marketData, dataId, valuationTime))
          .rateFutureNode(new RateFutureNodeConverter(conventionSource, holidaySource, regionSource, marketData, dataId, valuationTime))
          .swapNode(new SwapNodeConverter(conventionSource, holidaySource, regionSource, marketData, dataId, valuationTime))
          .create();
    }

    @Override
    protected Set<ComputedValue> getResults(final ValueSpecification bundleSpec, final ValueSpecification jacobianSpec, final ValueProperties bundleProperties,
        final Pair<HullWhiteOneFactorProviderInterface, CurveBuildingBlockBundle> pair) {
      final Set<ComputedValue> result = new HashSet<>();
      final HullWhiteOneFactorProviderDiscount provider = (HullWhiteOneFactorProviderDiscount) pair.getFirst();
      result.add(new ComputedValue(bundleSpec, pair.getFirst()));
      result.add(new ComputedValue(jacobianSpec, pair.getSecond()));
      for (final String curveName : getCurveNames()) {
        final ValueProperties curveProperties = bundleProperties.copy()
            .with(CURVE, curveName)
            .get();
        final ValueSpecification curveSpec = new ValueSpecification(YIELD_CURVE, ComputationTargetSpecification.NULL, curveProperties);
        result.add(new ComputedValue(curveSpec, provider.getMulticurveProvider().getCurve(curveName)));
      }
      return result;
    }

  }

}
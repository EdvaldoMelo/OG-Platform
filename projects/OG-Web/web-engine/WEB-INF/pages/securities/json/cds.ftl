"buy":"${security.buy?string}",
"protectionBuyer":{"scheme": "${security.protectionBuyer.scheme}", "value": "${security.protectionBuyer.value}"},
"protectionSeller":{"scheme": "${security.protectionSeller.scheme}", "value": "${security.protectionSeller.value}"},
"referenceEntity":{"scheme": "${security.referenceEntity.scheme}", "value": "${security.referenceEntity.value}"},
"debtSeniority":"${security.debtSeniority}",
"restructuringClause":"${security.restructuringClause}",
"startDate": {"date": "${security.startDate.toLocalDate()}", "zone": "${security.startDate.zone}"},
"effectiveDate": {"date": "${security.effectiveDate.toLocalDate()}", "zone": "${security.effectiveDate.zone}"},
"maturityDate": {"date": "${security.maturityDate.toLocalDate()}", "zone": "${security.maturityDate.zone}"},
"stubType":"${security.stubType}",
"couponFrequency":"${security.couponFrequency.conventionName}",
"dayCount":"${security.dayCount.conventionName}",
"businessDayConvention":"${security.businessDayConvention}",
"immAdjustMaturityDate":"${security.immAdjustMaturityDate?string}",
"adjustEffectiveDate":"${security.adjustEffectiveDate?string}",
"adjustMaturityDate":"${security.adjustMaturityDate?string}",
"notional":{"amount":"${security.notional.amount}","currency":"${security.notional.currency}"},
"recoveryRate":"${security.recoveryRate}",
"includeAccruedPremium":"${security.includeAccruedPremium?string}",
"protectionStart":"${security.protectionStart?string}",
/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.marketdatasnapshot.impl;

import java.util.List;

import com.google.common.collect.Lists;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.master.CombinedMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotHistoryRequest;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotHistoryResult;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchRequest;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchResult;

/**
 * A {@link MarketDataSnapshotMaster} that combines the behavior of the masters
 * in the session, user and global contexts. 
 */
public class CombinedMarketDataSnapshotMaster extends CombinedMaster<MarketDataSnapshotDocument, MarketDataSnapshotMaster> implements MarketDataSnapshotMaster {

  public CombinedMarketDataSnapshotMaster(final List<MarketDataSnapshotMaster> masterList) {
    super(masterList);
  }

  @Override
  public ChangeManager changeManager() {
    // TODO: if needed
    throw new UnsupportedOperationException();
  }

  @Override
  public MarketDataSnapshotSearchResult search(final MarketDataSnapshotSearchRequest request) {
    final MarketDataSnapshotSearchResult result = new MarketDataSnapshotSearchResult();
    
    for (MarketDataSnapshotMaster master : getMasterList()) {
      MarketDataSnapshotSearchResult search = master.search(request);
      result.getDocuments().addAll(search.getDocuments());
      result.setVersionCorrection(search.getVersionCorrection());
    }
    
    applyPaging(result, request.getPagingRequest());

    return result;
  }

  /**
   * Callback interface for the search operation to sort, filter and process results.
   */
  public interface SearchCallback extends CombinedMaster.SearchCallback<MarketDataSnapshotDocument, MarketDataSnapshotMaster> {
  }

  public void search(final MarketDataSnapshotSearchRequest request, final SearchCallback callback) {
    // TODO: parallel operation of any search requests
    List<MarketDataSnapshotSearchResult> results = Lists.newArrayList();
    for (MarketDataSnapshotMaster master : getMasterList()) {
      results.add(master.search(request));
    }
    search(results, callback);
  }

  @Override
  public MarketDataSnapshotHistoryResult history(final MarketDataSnapshotHistoryRequest request) {
    final MarketDataSnapshotMaster master = getMasterByScheme(request.getObjectId().getScheme());
    if (master != null) {
      return master.history(request);
    }
    return (new Try<MarketDataSnapshotHistoryResult>() {
      @Override
      public MarketDataSnapshotHistoryResult tryMaster(final MarketDataSnapshotMaster master) {
        return master.history(request);
      }
    }).each(request.getObjectId().getScheme());
  }  

}
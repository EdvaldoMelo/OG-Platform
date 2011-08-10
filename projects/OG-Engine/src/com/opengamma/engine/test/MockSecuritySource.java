/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.DummyChangeManager;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.IdUtils;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdSupplier;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * A simple mutable implementation of a source of securities.
 * <p>
 * This class is intended for testing scenarios.
 * It is not thread-safe and must not be used in production.
 */
public class MockSecuritySource implements SecuritySource {

  /**
   * The securities keyed by identifier.
   */
  private final Map<UniqueId, Security> _securities = new HashMap<UniqueId, Security>();
  /**
   * The suppler of unique identifiers.
   */
  private final UniqueIdSupplier _uidSupplier;

  /**
   * Creates the security master.
   */
  public MockSecuritySource() {
    _uidSupplier = new UniqueIdSupplier("Mock");
  }

  //-------------------------------------------------------------------------
  @Override
  public Security getSecurity(UniqueId identifier) {
    ArgumentChecker.notNull(identifier, "identifier");
    return _securities.get(identifier);
  }
  
  @Override
  public Security getSecurity(ObjectId objectId, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    // Mock source doesn't support versioning
    return getSecurity(objectId.atLatestVersion());
  }

  @Override
  public Collection<Security> getSecurities(ExternalIdBundle bundle) {
    ArgumentChecker.notNull(bundle, "bundle");
    List<Security> result = new ArrayList<Security>();
    for (Security sec : _securities.values()) {
      if (sec.getIdentifiers().containsAny(bundle)) {
        result.add(sec);
      }
    }
    return result;
  }
  
  @Override
  public Collection<Security> getSecurities(ExternalIdBundle bundle, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(bundle, "bundle");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    // Mock source doesn't support versioning
    return getSecurities(bundle);
  }

  @Override
  public Security getSecurity(ExternalIdBundle bundle) {
    ArgumentChecker.notNull(bundle, "bundle");
    for (ExternalId secId : bundle.getExternalIds()) {
      for (Security sec : _securities.values()) {
        if (sec.getIdentifiers().contains(secId)) {
          return sec;
        }
      }
    }
    return null;
  }
  
  @Override
  public Security getSecurity(ExternalIdBundle bundle, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(bundle, "bundle");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    // Mock source doesn't support versioning
    return getSecurity(bundle);
  }
  
  //-------------------------------------------------------------------------
  @Override
  public ChangeManager changeManager() {
    return DummyChangeManager.INSTANCE;
  }

  //-------------------------------------------------------------------------
  /**
   * Adds a security to the master.
   * 
   * @param security  the security to add, not null
   */
  public void addSecurity(Security security) {
    ArgumentChecker.notNull(security, "security");
    IdUtils.setInto(security, _uidSupplier.get());
    _securities.put(security.getUniqueId(), security);
  }

}

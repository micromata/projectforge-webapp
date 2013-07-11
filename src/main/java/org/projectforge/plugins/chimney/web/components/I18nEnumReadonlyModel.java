/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web.components;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.projectforge.core.I18nEnum;

/**
 * A read-only model that retrieves the localized string for an {@link I18nEnum} from another model.
 * @author Sweeps <pf@byte-storm.com>
 */
public class I18nEnumReadonlyModel implements IModel<String>
{
  private static final long serialVersionUID = 5129248716586970965L;

  private final IModel<? extends I18nEnum> target;
  private final Component comp;

  public I18nEnumReadonlyModel(final IModel<? extends I18nEnum> model, final Component comp)
  {
    this.target = model;
    this.comp = comp;
  }

  @Override
  public void detach()
  {
    target.detach();
  }

  @Override
  public String getObject()
  {
    final I18nEnum o = target.getObject();
    if (o == null)
      return null;
    return comp.getString(o.getI18nKey());
  }

  @Override
  public void setObject(final String object)
  {
    throw new UnsupportedOperationException();
  }

}

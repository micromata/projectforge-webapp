/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web;

import org.apache.wicket.model.IModel;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;

/**
 * Model that converts date format between Joda's DateTime and DateMidnight
 * to be usable with wicket components
 * @author Sweeps <pf@byte-storm.com>
 */
public abstract class JodaDateModel<M> implements IModel<DateMidnight> {
  protected static final long serialVersionUID = 1L;
  protected final IModel<M> model;

  public JodaDateModel(final IModel<M> activityModel) {
    this.model = activityModel;
  }

  @Override
  public void detach()
  {
    model.detach();
  }

  @Override
  public DateMidnight getObject()
  {
    if (model == null || model.getObject() == null)
      return null;

    // Convert DateTime to DateMidnight
    final DateTime beginDate = getDateTime();
    if (beginDate == null)
      return null;
    return new DateMidnight(beginDate);
  }

  /**
   * Convenience method for model.getObject(). Unlike model.getObject() this method
   * returns null when the model null is null, instead of throwing a null pointer exception.
   * @return The model's object or null
   */
  public M getModelObject() {
    if (model == null)
      return null;
    return model.getObject();
  }

  protected abstract DateTime getDateTime();

  @Override
  public void setObject(final DateMidnight object)
  {
    if (model == null || model.getObject() == null)
      return;

    if (object == null)
      setDateTime(null);
    else
      setDateTime(new DateTime(object));
  }

  protected abstract void setDateTime(DateTime dateTime);

}


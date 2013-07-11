/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web.components;

import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.projectforge.plugins.chimney.wbs.AbstractWbsNodeDO;

/**
 * Custom choice renderer for drop down lists of wbs nodes.
 * This renderer simply returns the wbs node's title as display value.
 * @author Sweeps <pf@byte-storm.com>
 */
public class WbsNodeChoiceRenderer<T extends AbstractWbsNodeDO> implements IChoiceRenderer<T>
{

  private static final long serialVersionUID = 6014860033208906566L;

  @Override
  public Object getDisplayValue(final T object)
  {
    return object.getTitle();
  }

  @Override
  public String getIdValue(final T object, final int index)
  {
    return object.getId().toString();
  }

}

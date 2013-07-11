/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web.components;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.feedback.IFeedbackMessageFilter;
import org.apache.wicket.markup.html.panel.FeedbackPanel;

public class ChimneyFeedbackPanel extends FeedbackPanel
{
  private static final long serialVersionUID = -1242744315485645494L;

  public ChimneyFeedbackPanel(final String id, final IFeedbackMessageFilter filter)
  {
    super(id, filter);
  }

  public ChimneyFeedbackPanel(final String id)
  {
    super(id);
  }

  @Override
  protected void onBeforeRender()
  {
    if (anyErrorMessage())
      add(AttributeModifier.replace("class", "alert alert_red feedback"));
    else
      add(AttributeModifier.replace("class", "alert alert_green feedback"));
    super.onBeforeRender();
  }

  @Override
  public boolean isVisible()
  {
    return this.anyMessage();
  }
}

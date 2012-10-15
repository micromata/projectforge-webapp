/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.web.wicket.flowlayout;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.Model;

/**
 * @author Johannes Unterstein (j.unterstein@micromata.de)
 * @author M. Lauterbach (m.lauterbach@micromata.de)
 * 
 */
public class AjaxIconButtonPanel extends IconButtonPanel
{
  private static final long serialVersionUID = 1366456680979183965L;

  /**
   * @param id
   * @param button
   * @param type
   * @param tooltip
   */
  public AjaxIconButtonPanel(final String id, final Button button, final IconType type, final String tooltip)
  {
    super(id, button, type, tooltip);
  }

  /**
   * @param id
   * @param type
   * @param tooltip
   */
  public AjaxIconButtonPanel(final String id, final IconType type, final Model<String> tooltip)
  {
    super(id, type, tooltip);
  }

  /**
   * @param id
   * @param type
   * @param tooltip
   */
  public AjaxIconButtonPanel(final String id, final IconType type, final String tooltip)
  {
    super(id, type, tooltip);
  }

  /**
   * @param id
   * @param type
   */
  public AjaxIconButtonPanel(final String id, final IconType type)
  {
    super(id, type);
  }

  /**
   * @see org.projectforge.web.wicket.flowlayout.IconButtonPanel#onSubmit()
   */
  @Override
  protected final void onSubmit()
  {
    // does nothing
  }

  protected void onSubmit(final AjaxRequestTarget target)
  {

  }

  protected void onError(final AjaxRequestTarget target)
  {

  }

  /**
   * @see org.projectforge.web.wicket.flowlayout.IconButtonPanel#createButton(java.lang.String)
   */
  @Override
  protected Button createButton(final String string)
  {
    return new AjaxButton(string) {
      private static final long serialVersionUID = -6046879772559434161L;

      @Override
      protected void onSubmit(final AjaxRequestTarget target, final Form< ? > form)
      {
        AjaxIconButtonPanel.this.onSubmit(target);
      }

      @Override
      protected void onError(final AjaxRequestTarget target, final Form< ? > form)
      {
        AjaxIconButtonPanel.this.onError(target);
      }
    };
  }

}

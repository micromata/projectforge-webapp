/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.com)
//
// ProjectForge is dual-licensed.
//
// This community edition is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License as published
// by the Free Software Foundation; version 3 of the License.
//
// This community edition is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
// Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, see http://www.gnu.org/licenses/.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.web.wicket.mobileflowlayout;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.flowlayout.AbstractFieldsetPanel;
import org.projectforge.web.wicket.flowlayout.DivPanel;

/**
 * Represents a entry of a group panel. This can be a label, text field or other form components.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class MobileFieldsetPanel extends AbstractFieldsetPanel<MobileFieldsetPanel>
{
  /**
   * Please use this only and only if you haven't multiple children. Please use {@link #newChildId()} instead.
   */
  private static final String FIELDS_ID = "fields";

  private static final long serialVersionUID = 2845731250470151819L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MobileFieldsetPanel.class);

  private WebMarkupContainer fields;

  /**
   */
  @SuppressWarnings("serial")
  public MobileFieldsetPanel(final String id, final String labeltext)
  {
    super(id);
    fieldset = new WebMarkupContainer("fieldset");
    superAdd(fieldset);
    this.labelText = labeltext;
    fieldset.add((label = new WebMarkupContainer("label")));
    label.add(new Label("labeltext", new Model<String>() {
      @Override
      public String getObject()
      {
        return labelText;
      };
    }).setRenderBodyOnly(true));
  }

  /**
   */
  public MobileFieldsetPanel(final DivPanel parent, final String labeltext)
  {
    this(parent.newChildId(), labeltext);
    parent.add(this);
  }

  /**
   * @see org.apache.wicket.Component#onBeforeRender()
   */
  @Override
  protected void onBeforeRender()
  {
    if (rendered == false) {
    }
    super.onBeforeRender();
  }

  /**
   * @see org.projectforge.web.wicket.flowlayout.AbstractFieldsetPanel#addChild(org.apache.wicket.Component[])
   */
  @Override
  protected MarkupContainer addChild(final Component... childs)
  {
    return fieldset.add(childs);
  }

  /**
   * @see org.projectforge.web.wicket.flowlayout.AbstractFieldsetPanel#addInvisibleChild()
   */
  @Override
  protected void addInvisibleChild()
  {
    add(WicketUtils.getInvisibleComponent(FIELDS_ID));
  }

  /**
   * @see org.projectforge.web.wicket.flowlayout.AbstractFieldsetPanel#getThis()
   */
  @Override
  protected MobileFieldsetPanel getThis()
  {
    return this;
  }

  /**
   * Creates and add a new RepeatingView as div-child if not already exist.
   * @see RepeatingView#newChildId()
   */
  @Override
  public String newChildId()
  {
    if (multipleChildren == true) {
      if (repeater == null) {
        repeater = new RepeatingView(FIELDS_ID);
        add(repeater);
      }
      return repeater.newChildId();
    } else {
      return FIELDS_ID;
    }
  }

}

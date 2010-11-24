/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2010 Kai Reinhard (k.reinhard@me.com)
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

package org.projectforge.web.core;

import org.apache.wicket.PageParameters;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.projectforge.common.NumberHelper;
import org.projectforge.web.MenuEntry;
import org.projectforge.web.wicket.WicketUtils;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class MenuEntryPanel extends Panel
{
  private static final long serialVersionUID = -5842187160235305180L;

  public MenuEntryPanel(String id)
  {
    super(id);
  }

  @SuppressWarnings("serial")
  public void init(final MenuEntry menuEntry)
  {
    final WebMarkupContainer li = new WebMarkupContainer("entry");
    add(li);
    final AbstractLink link;
    if (menuEntry.isWicketPage() == true) {
      link = new Link<String>("link") {
        @Override
        public void onClick()
        {
          if (menuEntry.getParams() == null) {
            setResponsePage(menuEntry.getPageClass());
          } else {
            final PageParameters params = WicketUtils.getPageParameters(menuEntry.getParams());
            setResponsePage(menuEntry.getPageClass(), params);
          }
        }
      };
    } else {
      link = new ExternalLink("link", WicketUtils.getUrl(getResponse(), menuEntry.getUrl(), true));
    }
    if (menuEntry.isNewWindow() == true) {
      link.add(new SimpleAttributeModifier("target", "_blank"));
    }
    final boolean isFirst = menuEntry.isFirst();
    final boolean isSelected = menuEntry.isSelected();
    if (isSelected == true) {
      li.add(new SimpleAttributeModifier("class", isFirst == true ? "first selected" : "selected"));
    } else if (isFirst == true) {
      li.add(new SimpleAttributeModifier("class", "first off"));
    } else if (menuEntry.getParent() == null) {
      // class="off" for unselected menu items on the first level:
      li.add(new SimpleAttributeModifier("class", "off"));
    }
    li.add(link);
    final Label suffixLabel;
    if (menuEntry.getNewCounterModel() != null) {
      suffixLabel = new Label("suffix", new Model<String>() {
        @Override
        public String getObject()
        {
          final Integer counter = menuEntry.getNewCounterModel().getObject();
          if (NumberHelper.greaterZero(counter) == true) {
            return String.valueOf(counter);
          } else {
            return "";
          }
        }
      }) {
        @Override
        public boolean isVisible()
        {
          final Integer counter = menuEntry.getNewCounterModel().getObject();
          return NumberHelper.greaterZero(counter) == true;
        }
      };
    } else {
      suffixLabel = new Label("suffix");
      suffixLabel.setVisible(false);
    }
    if (menuEntry.getNewCounterTooltip() != null) {
      WicketUtils.addTooltip(suffixLabel, getString(menuEntry.getNewCounterTooltip()));
    }
    final Label label = new Label("label", getString(menuEntry.getI18nKey()));
   
    if (menuEntry.getUrl() != null) {
      link.add(label); // Show label with link.
      link.add(suffixLabel);
      li.add(new Label("label").setVisible(false));
      li.add(new Label("suffix").setVisible(false));
    } else {
      link.setVisible(false); // Show only label (without link)
      li.add(label);
      li.add(suffixLabel);
    }
    if (menuEntry.hasSubMenuEntries() == true) {
      final WebMarkupContainer ul = new WebMarkupContainer("subMenu");
      li.add(ul);
      final RepeatingView menuRepeater = new RepeatingView("menuEntries");
      ul.add(menuRepeater);
      for (final MenuEntry subEntry : menuEntry.getSubMenuEntries()) {
        final MenuEntryPanel entryPanel = new MenuEntryPanel(menuRepeater.newChildId());
        entryPanel.setRenderBodyOnly(true);
        menuRepeater.add(entryPanel);
        entryPanel.init(subEntry);
      }
    } else {
      li.add(new Label("subMenu").setVisible(false));
    }
  }
}

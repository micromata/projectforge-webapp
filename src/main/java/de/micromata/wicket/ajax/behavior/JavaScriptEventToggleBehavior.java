/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.de)
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

package de.micromata.wicket.ajax.behavior;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.calldecorator.AjaxCallDecorator;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.string.StringValue;

/**
 * Behavior which represents the possibility to submit a javascript toggle status change to the server via an {@link AjaxEventBehavior}.<br/>
 * By default this behavior listens to the onClick event, but it is also possible to bind other events using the
 * {@link #JavaScriptEventToggleBehavior(String)} constructor.
 * 
 * @author <a href="j.unterstein@micromata.de">Johannes Unterstein</a>
 * 
 */
public abstract class JavaScriptEventToggleBehavior extends AjaxEventBehavior
{
  private static final long serialVersionUID = -5357558488585073864L;

  private static final String CONDITION = "condition";

  public JavaScriptEventToggleBehavior()
  {
    this("onClick");
  }

  public JavaScriptEventToggleBehavior(final String event)
  {
    super(event);
  }

  /**
   * @see org.apache.wicket.ajax.AjaxEventBehavior#onEvent(org.apache.wicket.ajax.AjaxRequestTarget)
   */
  @Override
  protected void onEvent(final AjaxRequestTarget target)
  {
    // Gather query params ?...&condition=true
    final StringValue conditionValue = RequestCycle.get().getRequest().getQueryParameters().getParameterValue(CONDITION);
    if (conditionValue != null) {
      final String conditionString = conditionValue.toString();
      if ("true".equals(conditionString)) {
        onToggleCall(target, true);
      } else if ("false".equals(conditionString)) {
        onToggleCall(target, false);
      } else {
        onUnregonizedCall(target);
      }
    } else {
      onUnregonizedCall(target);
    }
  }

  /**
   * @see org.apache.wicket.behavior.AbstractAjaxBehavior#getCallbackUrl()
   */
  @Override
  public CharSequence getCallbackUrl()
  {
    return super.getCallbackUrl() + "&" + CONDITION + "=" + getJavaScriptConditionForNewState();
  }

  /**
   * Default implementation uses html data attributes for remembering the toggle status. You can overwrite this method to use other
   * conditions, like the existing of dedicated css classes.
   * 
   * @return
   */
  protected String getJavaScriptConditionForNewState()
  {
    return "\'+ ! $(this).data(\"" + CONDITION + "\")+\'"; // invert current data value to display the new state!
  }

  /**
   * @see org.apache.wicket.ajax.AbstractDefaultAjaxBehavior#getAjaxCallDecorator()
   */
  @Override
  protected IAjaxCallDecorator getAjaxCallDecorator()
  {
    return new AjaxCallDecorator() {
      private static final long serialVersionUID = -4937878321650138366L;

      @Override
      public CharSequence decorateScript(final Component c, final CharSequence script)
      {
        String javaScript = "var data = $(this).data(\"" + CONDITION + "\"); var result = null;";
        javaScript += "result = ! data;"; // switch data object via javaScript
        javaScript += "if(result != null) { $(this).data(\"" + CONDITION + "\", result); };";
        return script + javaScript;
      }
    };
  }

  /**
   * Hook method which is called, when an ajax call is recognized, but the page parameters could not be interpreted properly.
   */
  protected void onUnregonizedCall(final AjaxRequestTarget target)
  {

  }

  /**
   * This method is called when the toggle status of the added component is changed
   * 
   * @param target
   * @param toggleStatus displays if the toggler is now active (toggleStatus = true), or inactive (toggleStatus = false)
   */
  protected abstract void onToggleCall(AjaxRequestTarget target, boolean toggleStatus);
}

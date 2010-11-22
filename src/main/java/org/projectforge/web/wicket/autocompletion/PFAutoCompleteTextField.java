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

package org.projectforge.web.wicket.autocompletion;

import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteBehavior;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.IAutoCompleteRenderer;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.projectforge.web.wicket.WicketUtils;

public abstract class PFAutoCompleteTextField<T> extends TextField<T>
{
  private static final long serialVersionUID = 3207038195316387588L;

  /** auto complete behavior attached to this textfield */
  private PFAutoCompleteBehavior<T> behavior;

  private PFAutoCompleteSettings settings;

  protected boolean providesTooltip;

  /**
   * Construct.
   * 
   * @param id
   * @param model
   * @param type
   * @param settings
   */
  public PFAutoCompleteTextField(final String id, final IModel<T> model)
  {
    this(id, model, PFAutoCompleteRenderer.INSTANCE, new PFAutoCompleteSettings());// , type, StringAutoCompleteRenderer.INSTANCE,
    // settings);
  }

  public PFAutoCompleteTextField(final String id, final IModel<T> model, final IAutoCompleteRenderer<String> renderer,
      final PFAutoCompleteSettings settings)
  {
    super(id, model);
    this.settings = settings;
    behavior = new PFAutoCompleteBehavior<T>(renderer, settings) {
      private static final long serialVersionUID = 1L;

      @Override
      protected List<T> getChoices(String input)
      {
        return PFAutoCompleteTextField.this.getChoices(input);
      }

      @Override
      protected List<T> getFavorites()
      {
        return PFAutoCompleteTextField.this.getFavorites();
      }

      @Override
      protected List<String> getRecentUserInputs()
      {
        return PFAutoCompleteTextField.this.getRecentUserInputs();
      }

      @Override
      protected String formatValue(T value)
      {
        return PFAutoCompleteTextField.this.formatValue(value);
      }

      protected String formatLabel(T value)
      {
        return PFAutoCompleteTextField.this.formatLabel(value);
      }
    };
    add(behavior);
  }

  @SuppressWarnings("serial")
  public PFAutoCompleteTextField<T> enableTooltips()
  {
    WicketUtils.addTooltip(this, new Model<String>() {
      @Override
      public String getObject()
      {
        return PFAutoCompleteTextField.this.getTooltip();
      }
    });
    return this;
  }

  /** {@inheritDoc} */
  @Override
  protected void onComponentTag(ComponentTag tag)
  {
    super.onComponentTag(tag);
    // disable browser's autocomplete
    tag.put("autocomplete", "off");
  }

  /**
   * Override this callback method that for returning favorite entries to show, if the user double clicks the empty input field. These
   * objects will be passed to the renderer to generate output.
   * 
   * @see AutoCompleteBehavior#getChoices(String)
   * 
   * @return null, if no favorites to show.
   */
  protected List<T> getFavorites()
  {
    return null;
  }

  /**
   * Override this callback method that for returning recent user inputs to show, if the user double clicks the empty input field.
   * 
   * @return null means: don't show recent user inputs.
   */
  protected List<String> getRecentUserInputs()
  {
    return null;
  }

  /**
   * Uses ObjectUtils.toString(Object) as default.
   * @param value
   * @return
   */
  protected String formatValue(T value)
  {
    return ObjectUtils.toString(value);
  }

  /**
   * Only used if labelValue is set to true.
   * @param value
   * @return The label to show in the drop down choice. If not overloaded null is returned.
   */
  protected String formatLabel(T value)
  {
    return null;
  }

  /**
   * Overwrite this method if a title attribute for the input text field should be set. Don't forget to call {@link #enableTooltips()}.
   * @return Tool-tip of the object currently represented by the input field or null.
   */
  protected String getTooltip()
  {
    return null;
  }

  /**
   * Callback method that should return a list of all possible assist choice objects. These objects will be passed to the renderer to
   * generate output.
   * 
   * @see AutoCompleteBehavior#getChoices(String)
   * 
   * @param input current input
   * @return list of all possible choice objects
   */
  protected abstract List<T> getChoices(String input);

  /**
   * Fluent.
   * @see PFAutoCompleteSettings#withAutoFill(boolean)
   */
  public PFAutoCompleteTextField<T> withAutoFill(boolean autoFill)
  {
    settings.withAutoFill(autoFill);
    return this;
  }

  /**
   * Fluent.
   * @see PFAutoCompleteSettings#withAutoSubmit(boolean)
   */
  public PFAutoCompleteTextField<T> withAutoSubmit(boolean autoSubmit)
  {
    settings.withAutoSubmit(autoSubmit);
    return this;
  }

  /**
   * Fluent.
   * @see PFAutoCompleteSettings#withFocus(boolean)
   */
  public PFAutoCompleteTextField<T> withFocus(boolean hasFocus)
  {
    settings.withFocus(hasFocus);
    return this;
  }

  /**
   * Fluent.
   * @see PFAutoCompleteSettings#withCacheLength(int)
   */
  public PFAutoCompleteTextField<T> withCacheLength(int cacheLength)
  {
    settings.withCacheLength(cacheLength);
    return this;
  }

  /**
   * Fluent.
   * @see PFAutoCompleteSettings#withDelay(int)
   */
  public PFAutoCompleteTextField<T> withDelay(int delay)
  {
    settings.withDelay(delay);
    return this;
  }

  /**
   * Fluent.
   * @see PFAutoCompleteSettings#withMatchCase(boolean)
   */
  public PFAutoCompleteTextField<T> withMatchCase(boolean matchCase)
  {
    settings.withMatchCase(matchCase);
    return this;
  }

  /**
   * Fluent.
   * @see PFAutoCompleteSettings#withMatchContains(boolean)
   */
  public PFAutoCompleteTextField<T> withMatchContains(boolean matchContains)
  {
    settings.withMatchContains(matchContains);
    return this;
  }

  /**
   * Fluent.
   * @see PFAutoCompleteSettings#withMatchSubset(boolean)
   */
  public PFAutoCompleteTextField<T> withMatchSubset(boolean matchSubset)
  {
    settings.withMatchSubset(matchSubset);
    return this;
  }

  /**
   * Fluent.
   * @see PFAutoCompleteSettings#withMaxItemsToShow(int)
   */
  public PFAutoCompleteTextField<T> withMaxItemsToShow(int maxItemsToShow)
  {
    settings.withMaxItemsToShow(maxItemsToShow);
    return this;
  }

  /**
   * Fluent.
   * @see PFAutoCompleteSettings#withMinChars(int)
   */
  public PFAutoCompleteTextField<T> withMinChars(int minChars)
  {
    settings.withMinChars(minChars);
    return this;
  }

  /**
   * Fluent.
   * @see PFAutoCompleteSettings#withMustMatch(boolean)
   */
  public PFAutoCompleteTextField<T> withMustMatch(boolean mustMatch)
  {
    settings.withMustMatch(mustMatch);
    return this;
  }

  /**
   * Fluent.
   * @see PFAutoCompleteSettings#withScroll(boolean)
   */
  public PFAutoCompleteTextField<T> withScroll(boolean scroll)
  {
    settings.withScroll(scroll);
    return this;
  }

  /**
   * Fluent.
   * @see PFAutoCompleteSettings#withScrollHeight(boolean)
   */
  public PFAutoCompleteTextField<T> withScrollHeight(int scrollHeight)
  {
    settings.withScrollHeight(scrollHeight);
    return this;
  }

  /**
   * Fluent.
   * @see PFAutoCompleteSettings#withSelectFirst(boolean)
   */
  public PFAutoCompleteTextField<T> withSelectFirst(boolean selectFirst)
  {
    settings.withSelectFirst(selectFirst);
    return this;
  }

  /**
   * Fluent.
   * @see PFAutoCompleteSettings#withSelectOnly(boolean)
   */
  public PFAutoCompleteTextField<T> withSelectOnly(boolean selectOnly)
  {
    settings.withSelectOnly(selectOnly);
    return this;
  }

  /**
   * Fluent.
   * @see PFAutoCompleteSettings#withWidth(int)
   */
  public PFAutoCompleteTextField<T> withWidth(int width)
  {
    settings.withWidth(width);
    return this;
  }

  /**
   * Fluent.
   * @see PFAutoCompleteSettings#withLabelValue(boolean)
   */
  public PFAutoCompleteTextField<T> withLabelValue(boolean labelValue)
  {
    settings.withLabelValue(labelValue);
    return this;
  }
}

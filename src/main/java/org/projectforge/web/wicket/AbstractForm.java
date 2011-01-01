/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2011 Kai Reinhard (k.reinhard@me.com)
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

package org.projectforge.web.wicket;

import java.text.MessageFormat;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.validation.AbstractFormValidator;
import org.apache.wicket.validation.IValidationError;
import org.apache.wicket.validation.ValidationError;
import org.projectforge.user.PFUserDO;


public abstract class AbstractForm<F, P extends AbstractBasePage> extends Form<F>
{
  private static final long serialVersionUID = -5703197102062729288L;

  protected final P parentPage;

  private ShinyFormVisitor shinyVisitor = new ShinyFormVisitor();

  /**
   * Convenience method for creating a component which is in the mark-up file but should not be visible.
   * @param wicketId
   * @return
   * @see AbstractBasePage#createInvisibleDummyComponent(String)
   */
  public static Label createInvisibleDummyComponent(final String wicketId)
  {
    return AbstractBasePage.createInvisibleDummyComponent(wicketId);
  }

  @SuppressWarnings("serial")
  public AbstractForm(P parentPage)
  {
    super("form");
    this.parentPage = parentPage;
    add(new AbstractFormValidator() {
      public FormComponent< ? >[] getDependentFormComponents()
      {
        return getDependentFormValidationComponents();
      }

      public void validate(Form< ? > form)
      {
        validation();
      }
    });
  }

  /**
   * Dependent form components which should be processed first before form validation.
   * @return
   */
  public FormComponent< ? >[] getDependentFormValidationComponents()
  {
    return null;
  }

  public P getParentPage()
  {
    return parentPage;
  }

  /**
   * Is called by parent page directly after creating this form (constructor call).
   */
  protected void init()
  {

  }

  public void onBeforeRender()
  {
    super.onBeforeRender();
    visitChildren(shinyVisitor);
  }

  /**
   * @see AbstractBasePage#getUrl(String)
   */
  public String getUrl(String path)
  {
    return parentPage.getUrl(path, true);
  }

  /**
   * @see AbstractBasePage#getUrl(String, boolean)
   */
  public String getUrl(String path, boolean encodeUrl)
  {
    return parentPage.getUrl(path, encodeUrl);
  }

  /**
   * Here you can add validation and errors manually.
   */
  protected void validation()
  {
    // Do nothing at default;
  }

  public void addError(final String msgKey)
  {
    error(getString(msgKey));
  }

  public void addError(final String msgKey, final Object... params)
  {
    error(MessageFormat.format(getString(msgKey), params));
  }

  public void addFieldRequiredError(final String fieldKey)
  {
    error(MessageFormat.format(getString("validation.error.fieldRequired"), getString(fieldKey)));
  }

  public void addFormComponentError(final FormComponent< ? > component, final String msgKey)
  {
    component.error((IValidationError) new ValidationError().addMessageKey(msgKey));
  }

  public void addComponentError(final Component component, final String msgKey)
  {
    component.error(getString(msgKey));
  }

  public String getLocalizedMessage(String key, Object... params)
  {
    if (params == null) {
      return getString(key);
    }
    return MessageFormat.format(getString(key), params);
  }

  /**
   * @see AbstractBasePage#escapeHtml(String)
   */
  protected String escapeHtml(String str)
  {
    return parentPage.escapeHtml(str);
  }

  protected PFUserDO getUser()
  {
    return this.parentPage.getUser();
  }
}

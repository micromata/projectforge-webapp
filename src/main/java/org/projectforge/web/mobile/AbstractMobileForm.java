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

package org.projectforge.web.mobile;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.validation.AbstractFormValidator;

public abstract class AbstractMobileForm<F, P extends AbstractMobilePage> extends Form<F>
{
  private static final long serialVersionUID = 3798448024275972658L;

  protected final P parentPage;

  @SuppressWarnings("serial")
  public AbstractMobileForm(P parentPage)
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

  /**
   * Here you can add validation and errors manually.
   */
  protected void validation()
  {
    // Do nothing at default;
  }
}

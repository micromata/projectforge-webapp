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

package org.projectforge.web.book;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.apache.wicket.Component;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.convert.IConverter;
import org.projectforge.book.BookDO;
import org.projectforge.book.BookDao;
import org.projectforge.book.BookFilter;
import org.projectforge.common.StringHelper;
import org.projectforge.web.wicket.AbstractListForm;
import org.projectforge.web.wicket.autocompletion.PFAutoCompleteTextField;
import org.projectforge.web.wicket.components.CoolCheckBoxPanel;

public class BookListForm extends AbstractListForm<BookListFilter, BookListPage>
{
  private static final long serialVersionUID = 7055486163615227688L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(BookListForm.class);

  @SpringBean(name = "bookDao")
  private BookDao bookDao;

  @Override
  protected void init()
  {
    super.init();
    filterContainer.add(new CoolCheckBoxPanel("presentCheckbox", new PropertyModel<Boolean>(getSearchFilter(), "present"),
        getString("book.status.present"), true));
    filterContainer.add(new CoolCheckBoxPanel("missedCheckBox", new PropertyModel<Boolean>(getSearchFilter(), "missed"),
        getString("book.status.missed"), true));
    filterContainer.add(new CoolCheckBoxPanel("disposedCheckBox", new PropertyModel<Boolean>(getSearchFilter(), "disposed"),
        getString("book.status.disposed"), true));
    filterContainer.add(new CoolCheckBoxPanel("deletedCheckBox", new PropertyModel<Boolean>(getSearchFilter(), "deleted"),
        getString("deleted"), true));
  }

  public BookListForm(BookListPage parentPage)
  {
    super(parentPage);
  }

  @SuppressWarnings("serial")
  @Override
  protected Component createSearchTextField()
  {
    @SuppressWarnings("unchecked")
    final PFAutoCompleteTextField<BookDO> searchField = new PFAutoCompleteTextField<BookDO>("searchString", new Model() {
      @Override
      public Serializable getObject()
      {
        // Pseudo object for storing search string (title field is used for this foreign purpose).
        return new BookDO().setTitle(searchFilter.getSearchString());
      }

      @Override
      public void setObject(final Serializable object)
      {
        if (object != null) {
          if (object instanceof String) {
            searchFilter.setSearchString((String) object);
          }
        } else {
          searchFilter.setSearchString("");
        }
      }
    }) {
      @Override
      protected List<BookDO> getChoices(final String input)
      {
        final BookFilter filter = new BookFilter();
        filter.setSearchString(input);
        filter.setSearchFields("title", "authors");
        final List<BookDO> list = bookDao.getList(filter);
        return list;
      }

      @Override
      protected List<String> getRecentUserInputs()
      {
        return parentPage.getRecentSearchTermsQueue().getRecents();
      }

      @Override
      protected String formatLabel(final BookDO book)
      {
        return StringHelper.listToString("; ", book.getAuthors(), book.getTitle());
      }

      @Override
      protected String formatValue(BookDO book)
      {
        return "id:" + book.getId();
      }

      @Override
      public IConverter getConverter(Class< ? > type)
      {
        return new IConverter() {
          @Override
          public Object convertToObject(String value, Locale locale)
          {
            searchFilter.setSearchString(value);
            return null;
          }

          @Override
          public String convertToString(Object value, Locale locale)
          {
            return searchFilter.getSearchString();
          }
        };
      }
    };
    searchField.withLabelValue(true).withMatchContains(true).withMinChars(2).withFocus(true).withAutoSubmit(true);
    return searchField;
  }

  @Override
  protected BookListFilter newSearchFilterInstance()
  {
    return new BookListFilter();
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}

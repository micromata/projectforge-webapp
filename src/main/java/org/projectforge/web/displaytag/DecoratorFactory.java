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

package org.projectforge.web.displaytag;

import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;

import org.apache.commons.lang.StringUtils;
import org.displaytag.decorator.DefaultDecoratorFactory;
import org.displaytag.decorator.DisplaytagColumnDecorator;
import org.displaytag.decorator.TableDecorator;
import org.displaytag.exception.DecoratorInstantiationException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class DecoratorFactory extends DefaultDecoratorFactory
{

  @Override
  public DisplaytagColumnDecorator loadColumnDecorator(PageContext pageContext, String decoratorName)
      throws DecoratorInstantiationException
  {
    if (StringUtils.isEmpty(decoratorName) == true) {
      return super.loadColumnDecorator(pageContext, decoratorName);
    }
    DisplaytagColumnDecorator decorator = (DisplaytagColumnDecorator)getDecorator(pageContext, decoratorName);
    return decorator;
  }

  @Override
  public TableDecorator loadTableDecorator(PageContext pageContext, String decoratorName) throws DecoratorInstantiationException
  {
    if (StringUtils.isEmpty(decoratorName) == true) {
      return super.loadTableDecorator(pageContext, decoratorName);
    }
    TableDecorator decorator = (TableDecorator)getDecorator(pageContext, decoratorName);
    return decorator;
  }

  private Object getDecorator(PageContext pageContext, String decoratorName)
  {
    HttpSession session = pageContext.getSession();
    WebApplicationContext webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(session.getServletContext());
    ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(new String[] {}, webApplicationContext);
    Object decorator = ctx.getBeanFactory().getBean(decoratorName);
    ctx.getBeanFactory().autowireBeanProperties(decorator, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, false);
    return decorator;
  }
}

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

package org.projectforge.web.doc;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.projectforge.web.wicket.WicketUtils;

/**
 * Enables the tutorial's action link.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class TutorialFilter implements Filter
{
  public void destroy()
  {
    // do nothing
  }

  public void init(final FilterConfig cfg) throws ServletException
  {
    // do nothing
  }

  public void doFilter(final ServletRequest req, final ServletResponse resp, final FilterChain chain) throws IOException, ServletException
  {
    final String uri = ((HttpServletRequest) req).getRequestURI();
    if (uri.matches(".*/ProjectForge.*.html") == false) {
      chain.doFilter(req, resp);
      return;
    }
    final PrintWriter out = resp.getWriter();
    final CharResponseWrapper wrapper = new CharResponseWrapper((HttpServletResponse) resp);
    chain.doFilter(req, wrapper);
    final CharArrayWriter caw = new CharArrayWriter();
    final String tutorialUrl = ((HttpServletResponse)resp).encodeURL(WicketUtils.getAbsoluteUrl("/wa/tutorial"));
    String html = wrapper.toString();
    html = html.replace("{actionLink}", tutorialUrl);
    caw.write(html);
    // caw.write(wrapper.toString().substring(0, wrapper.toString().indexOf("</body>") - 1));
    // caw.write("<p>\n<center>" + messages.getString("Visitor") + "<font color='red'>" + counter.getCounter() + "</font></center>");
    // caw.write("\n</body></html>");
    resp.setContentLength(caw.toString().length());
    out.write(caw.toString());
    out.close();
  }

  public class CharResponseWrapper extends HttpServletResponseWrapper
  {
    private CharArrayWriter output;

    public String toString()
    {
      return output.toString();
    }

    public CharResponseWrapper(HttpServletResponse response)
    {
      super(response);
      output = new CharArrayWriter();
    }

    public PrintWriter getWriter()
    {
      return new PrintWriter(output);
    }
  }
}

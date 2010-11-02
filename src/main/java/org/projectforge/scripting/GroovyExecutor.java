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

package org.projectforge.scripting;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.Script;
import groovy.lang.Writable;
import groovy.text.SimpleTemplateEngine;
import groovy.text.Template;
import groovy.text.TemplateEngine;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.codehaus.groovy.control.CompilationFailedException;
import org.projectforge.access.AccessException;

public class GroovyExecutor
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(GroovyExecutor.class);

  public GroovyResult execute(final String script, final Map<String, Object> variables)
  {
    if (script == null) {
      return new GroovyResult();
    }
    final Script groovyObject = compileGroovy(script, true);
    if (groovyObject == null) {
      return new GroovyResult();
    }
    return execute(groovyObject, variables);
  }

  public String executeTemplate(final String template, final Map<String, Object> variables)
  {
    securityChecks(template);
    final TemplateEngine templateEngine = new SimpleTemplateEngine();
    try {
      final Template templateObject = templateEngine.createTemplate(template);
      final Writable writable = templateObject.make(variables);
      final StringWriter writer = new StringWriter();
      writable.writeTo(writer);
      return writer.toString();
    } catch (CompilationFailedException ex) {
      log.error(ex.getMessage() + " while executing template: " + template, ex);
    } catch (FileNotFoundException ex) {
      log.error(ex.getMessage() + " while executing template: " + template, ex);
    } catch (ClassNotFoundException ex) {
      log.error(ex.getMessage() + " while executing template: " + template, ex);
    } catch (IOException ex) {
      log.error(ex.getMessage() + " while executing template: " + template, ex);
    }
    return null;
  }

  /**
   * @param script
   * @param bindScriptResult If true then "scriptResult" from type GroovyResult is binded.
   * @return
   */
  public Script compileGroovy(final String script, boolean bindScriptResult)
  {
    securityChecks(script);
    final GroovyClassLoader gcl = new GroovyClassLoader();
    Class< ? > groovyClass = null;
    try {
      groovyClass = gcl.parseClass(script);
    } catch (CompilationFailedException ex) {
      log.info("Groovy-CompilationFailedException: " + ex.getMessage());
      return null;
    }
    Script groovyObject = null;
    try {
      groovyObject = (Script) groovyClass.newInstance();
    } catch (InstantiationException ex) {
      log.error(ex.getMessage(), ex);
      return null;
    } catch (IllegalAccessException ex) {
      log.error(ex.getMessage(), ex);
      return null;
    }
    if (bindScriptResult == true) {
      final Binding binding = groovyObject.getBinding();
      final GroovyResult scriptResult = new GroovyResult();
      binding.setVariable("scriptResult", scriptResult);
    }
    return groovyObject;
  }

  public GroovyResult execute(final Script groovyScript)
  {
    return execute(groovyScript, null);
  }

  public GroovyResult execute(final Script groovyScript, final Map<String, Object> variables)
  {
    if (variables != null) {
      final Binding binding = groovyScript.getBinding();
      for (Map.Entry<String, Object> entry : variables.entrySet()) {
        binding.setVariable(entry.getKey(), entry.getValue());
      }
    }
    final GroovyResult scriptResult = new GroovyResult();
    Object result = null;
    try {
      result = groovyScript.run();
    } catch (Exception ex) {
      log.info("Groovy-Execution-Exception: " + ex.getMessage(), ex);
      return new GroovyResult(ex);
    }
    scriptResult.setResult(result);
    return scriptResult;
  }

  private void securityChecks(final String script)
  {
    final String[] forbiddenKeyWords = { "__baseDao", "__baseObject"};
    for (String forbiddenKeyWord : forbiddenKeyWords) {
      if (StringUtils.contains(script, forbiddenKeyWord) == true) {
        throw new AccessException("access.exception.violation", forbiddenKeyWord);
      }
    }
  }
}

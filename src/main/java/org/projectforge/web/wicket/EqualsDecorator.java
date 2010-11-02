/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.web.wicket;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.apache.wicket.model.IModel;
import org.apache.wicket.util.lang.Objects;

/**
 * From the book "Wicket in Action" by Martijn Dashorst and Eelco Hillenius.<br/>
 * ©2009 by Manning Publications Co. All rights reserved.
 * 
 */
public final class EqualsDecorator
{
  private EqualsDecorator()
  {
  }

  public static IModel< ? > decorate(final IModel< ? > model)
  {
    return (IModel< ? >) Proxy.newProxyInstance(model.getClass().getClassLoader(), model.getClass().getInterfaces(), new Decorator(model));
  }

  @SuppressWarnings("serial")
  private static class Decorator implements InvocationHandler, Serializable
  {
    private final IModel< ? > model;

    Decorator(IModel< ? > model)
    {
      this.model = model;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
      String methodName = method.getName();
      if (methodName.equals("equals")) {
        if (args[0] instanceof IModel< ? >) {
          return Objects.equal(model.getObject(), ((IModel< ? >) args[0]).getObject());
        }
      } else if (methodName.equals("hashCode")) {
        Object val = model.getObject();
        return Objects.hashCode(val);
      } else if (methodName.equals("writeReplace")) {
        return new SerializableReplacement(model);
      }
      return method.invoke(model, args);
    }
  }

  @SuppressWarnings("serial")
  private static class SerializableReplacement implements Serializable
  {
    private final IModel< ? > model;

    SerializableReplacement(IModel< ? > model)
    {
      this.model = model;
    }

    private Object readResolve() throws ObjectStreamException
    {
      return decorate(model);
    }
  }
}

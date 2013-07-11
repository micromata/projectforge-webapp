/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.utils.spring;

import java.util.concurrent.Callable;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * A service bean that allows to execute arbitrary code within an already
 * existing or new transaction via command pattern. The code to be executed
 * must be passed through a {@link Callable} or {@link Runnable}, depending
 * on whether a return value is needed or not. Either a read-only or a
 * read/write transaction can be used.
 * @author Sweeps <pf@byte-storm.com>
 */
@Service
public class RunInTransactionService
{
  /**
   * Calls the run() method of the passed {@link Runnable} inside a new or existing read/write transaction.
   * @param command The command to be executed.
   */
  @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
  public void inTransaction(final Runnable command)
  {
    command.run();
  }

  /**
   * Calls the run() method of the passed {@link Runnable} inside a new or existing read-only transaction.
   * @param command The command to be executed.
   */
  @Transactional(readOnly = true, propagation = Propagation.REQUIRED)
  public void inReadOnlyTransaction(final Runnable command)
  {
    // Note: The following call is safe. The @Transactional of inTransaction()
    // is ignored because this is a call within the same class. Same-class
    // method calls are not intercepted by Spring.
    inTransaction(command);
  }

  /**
   * Calls the call() method of the passed {@link Callable} inside a new or existing read/write transaction.
   * @param command The command to be executed.
   * @return The return value of command.call().
   */
  @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
  public <T> T inTransaction(final Callable<T> command)
  {
    try {
      return command.call();
    } catch (final Exception e) {
      throw new RuntimeException(command.toString(), e);
    }
  }

  /**
   * Calls the call() method of the passed {@link Callable} inside a new or existing read-only transaction.
   * @param command The command to be executed.
   * @return The return value of command.call().
   */
  @Transactional(readOnly = true, propagation = Propagation.REQUIRED)
  public <T> T inReadOnlyTransaction(final Callable<T> command)
  {
    // Note: The following call is safe. The @Transactional of inTransaction()
    // is ignored because this is a call within the same class. Same-class
    // method calls are not intercepted by Spring.
    return inTransaction(command);
  }
}

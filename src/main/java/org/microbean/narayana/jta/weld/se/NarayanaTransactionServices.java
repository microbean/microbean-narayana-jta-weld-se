/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2018 microBean.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.microbean.narayana.jta.weld.se;

import javax.enterprise.inject.Instance;

import javax.enterprise.inject.spi.CDI;

import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.jboss.weld.transaction.spi.TransactionServices;

/**
 * A {@link TransactionServices} implementation that uses the <a
 * href="https://narayana.io/">Narayana transaction engine</a> and
 * does not use JNDI.
 *
 * <p>{@link TransactionServices} implementations are used by <a
 * href="https://docs.jboss.org/weld/reference/latest/en-US/html/index.html">Weld</a>
 * for <a
 * href="http://docs.jboss.org/cdi/spec/2.0/cdi-spec.html#transactional_observer_methods">transactional
 * observer notification</a> as well as for providing the
 * implementation backing the <a
 * href="http://docs.jboss.org/cdi/spec/2.0/cdi-spec.html#additional_builtin_beans">built-in
 * {@code UserTransaction} CDI bean</a>.</p>
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 *
 * @see TransactionServices
 */
public class NarayanaTransactionServices implements TransactionServices {

  private UserTransaction userTransaction;

  /*
   * Constructors.
   */


  /**
   * Creates a new {@link NarayanaTransactionServices}.
   */
  public NarayanaTransactionServices() {
    super();
  }


  /*
   * Instance methods.
   */
  

  /**
   * Returns the {@link UserTransaction} present in this environment
   * by invoking the {@link
   * com.arjuna.ats.jta.UserTransaction#userTransaction()} method and
   * returning its result.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * <p>The return value of this method is used as the backing
   * implementation of the <a
   * href="http://docs.jboss.org/cdi/spec/2.0/cdi-spec.html#additional_builtin_beans">built-in
   * {@code UserTransaction} CDI bean</a>.</p>
   *
   * @return the non-{@code null} {@link UserTransaction} present in
   * this environment
   *
   * @see com.arjuna.ats.jta.UserTransaction#userTransaction()
   */
  @Override
  public final UserTransaction getUserTransaction() {
    return com.arjuna.ats.jta.UserTransaction.userTransaction();
  }

  /**
   * Returns {@code true} if the current {@link UserTransaction}
   * {@linkplain UserTransaction#getStatus() has a status} indicating
   * that it is active.
   *
   * <p>This method returns {@code true} if the current {@link
   * UserTransaction} {@linkplain UserTransaction#getStatus() has a
   * status} equal to one of the following values:</p>
   *
   * <ul>
   *
   * <li>{@link Status#STATUS_ACTIVE}</li>
   *
   * <li>{@link Status#STATUS_COMMITTING}</li>
   *
   * <li>{@link Status#STATUS_MARKED_ROLLBACK}</li>
   *
   * <li>{@link Status#STATUS_PREPARED}</li>
   *
   * <li>{@link Status#STATUS_PREPARING}</li>
   *
   * <li>{@link Status#STATUS_ROLLING_BACK}</li>
   *
   * </ul>
   *
   * @return {@code true} if the current {@link UserTransaction}
   * {@linkplain UserTransaction#getStatus() has a status} indicating
   * that it is active; {@code false} otherwise
   *
   * @exception RuntimeException if an invocation of the {@link
   * UserTransaction#getStatus()} method resulted in a {@link
   * SystemException}
   *
   * @see Status
   */
  @Override
  public final boolean isTransactionActive() {
    final UserTransaction userTransaction;
    synchronized (this) {
      if (this.userTransaction == null) {
        this.userTransaction = CDI.current().select(UserTransaction.class).get();
      }
      userTransaction = this.userTransaction;
    }
    final boolean returnValue;
    if (userTransaction == null) {
      returnValue = false;
    } else {
      boolean temp = false;
      try {
        final int status = userTransaction.getStatus();
        temp =
          status == Status.STATUS_ACTIVE ||
          status == Status.STATUS_COMMITTING ||
          status == Status.STATUS_MARKED_ROLLBACK ||
          status == Status.STATUS_PREPARED ||
          status == Status.STATUS_PREPARING ||
          status == Status.STATUS_ROLLING_BACK;
      } catch (final SystemException e) {
        throw new RuntimeException(e.getMessage(), e);
      } finally {
        returnValue = temp;
      }
    }
    return returnValue;
  }

  /**
   * Registers the supplied {@link Synchronization} with the current
   * {@link Transaction}.
   *
   * @exception RuntimeException if an invocation of the {@link
   * TransactionManager#getTransaction()} method resulted in a {@link
   * SystemException}, or if an invocation of the {@link
   * Transaction#registerSynchronization(Synchronization)} method
   * resulted in either a {@link SystemException} or a {@link
   * RollbackException}
   *
   * @see Transaction#registerSynchronization(Synchronization)
   */
  @Override
  public final void registerSynchronization(final Synchronization synchronization) {
    final CDI<Object> cdi = CDI.current();    
    final Instance<Transaction> transactionInstance = cdi.select(Transaction.class);
    Transaction transaction = null;
    if (transactionInstance.isUnsatisfied()) {
      Instance<TransactionManager> transactionManagerInstance = cdi.select(TransactionManager.class);
      assert transactionManagerInstance != null;
      final TransactionManager transactionManager;
      if (transactionManagerInstance.isUnsatisfied()) {
        transactionManager = com.arjuna.ats.jta.TransactionManager.transactionManager();
      } else {
        transactionManager = transactionManagerInstance.get();
      }
      if (transactionManager != null) {
        try {
          transaction = transactionManager.getTransaction();
        } catch (final SystemException e) {
          throw new RuntimeException(e.getMessage(), e);
        }
      }
    } else {
      transaction = transactionInstance.get();
    }
    if (transaction != null) {
      try {
        transaction.registerSynchronization(synchronization);
      } catch (final SystemException | RollbackException e) {
        throw new RuntimeException(e.getMessage(), e);
      }
    }
  }

  /**
   * Releases any internal resources acquired during the lifespan of
   * this object.
   */
  @Override
  public synchronized final void cleanup() {
    this.userTransaction = null;
  }

  
}
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

import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.jboss.weld.transaction.spi.TransactionServices;

public class NarayanaTransactionServices implements TransactionServices {

  public NarayanaTransactionServices() {
    super();
  }

  @Override
  public final UserTransaction getUserTransaction() {
    return com.arjuna.ats.jta.UserTransaction.userTransaction();
  }

  @Override
  public final boolean isTransactionActive() {
    final UserTransaction userTransaction = this.getUserTransaction();
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

  @Override
  public final void registerSynchronization(final Synchronization synchronization) {
    TransactionManager transactionManager = com.arjuna.ats.jta.TransactionManager.transactionManager();
    if (transactionManager != null) {
      Transaction transaction = null;
      try {
        transaction = transactionManager.getTransaction();
      } catch (final SystemException e) {
        throw new RuntimeException(e.getMessage(), e);
      }
      if (transaction != null) {
        try {
          transaction.registerSynchronization(synchronization);
        } catch (final SystemException | RollbackException e) {
          throw new RuntimeException(e.getMessage(), e);
        }
      }
    }
  }

  @Override
  public final void cleanup() {

  }

  
}

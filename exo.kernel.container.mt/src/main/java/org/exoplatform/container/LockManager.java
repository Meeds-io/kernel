/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.exoplatform.container;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class is used to be aware of all the {@link Lock} currently used to prevent
 * deadlocks
 * 
 */
public class LockManager
{

   /**
    * The logger
    */
   private static final Log LOG = ExoLogger.getLogger("exo.kernel.container.mt.LockManager");

   /**
    * The singleton
    */
   private static final LockManager INSTANCE = new LockManager();

   /**
    * Threads currently *waiting* to acquire a lockable resource (registered
    * between register() and unregister()).
    */
   private final ConcurrentMap<Thread, Lockable> locks = new ConcurrentHashMap<Thread, Lockable>();

   /**
    * Locks that are currently *held* (i.e. acquired but not yet released).
    * Tracked so that isEmpty() returns false until every lock has been unlocked.
    */
   private final ConcurrentMap<Lockable, Thread> heldLocks = new ConcurrentHashMap<Lockable, Thread>();

   /**
    * The total amount of uncompleted tasks
    */
   private final AtomicInteger totalUncompletedTasks = new AtomicInteger();

   private LockManager()
   {
   }

   /**
    * The unique instance of the {@link LockManager}
    */
   public static LockManager getInstance()
   {
      return INSTANCE;
   }

   /**
    * Gives a new {@link Lock} instance
    */
   public Lock createLock()
   {
      return new InternalReentrantLock();
   }

   /**
    * Creates a new {@link RunnableFuture} instance
    */
   public <T> RunnableFuture<T> createRunnableFuture(Runnable runnable, T value)
   {
      return new InternalFutureTask<T>(runnable, value);
   }

   /**
    * Creates a new {@link RunnableFuture} instance
    */
   public <T> RunnableFuture<T> createRunnableFuture(Callable<T> callable)
   {
      return new InternalFutureTask<T>(callable);
   }

   /**
    * Gives the total amount of uncompleted tasks
    */
   int getTotalUncompletedTasks()
   {
      return totalUncompletedTasks.get();
   }

   /**
    * Increments and get the total amount of uncompleted tasks
    */
   int incrementAndGetTotalUncompletedTasks()
   {
      return totalUncompletedTasks.incrementAndGet();
   }

   /**
    * Indicates whether or not there are some remaining lockable resources.
    * Returns {@code true} only when no thread is waiting on a lock AND no
    * lock is currently held.
    */
   boolean isEmpty()
   {
      return locks.isEmpty() && heldLocks.isEmpty();
   }

   /**
    * Registers a lockable resource for the current thread
    */
   private void register(Lockable l)
   {
      locks.put(Thread.currentThread(), l);
   }

   /**
    * Unregisters a lockable resource for the current thread
    */
   private void unregister(Lockable l)
   {
      locks.remove(Thread.currentThread(), l);
   }

   /**
    * Records that the current thread has successfully *acquired* a lockable
    * resource (i.e. moved from "waiting" to "holding").
    */
   private void registerHeld(Lockable l)
   {
      heldLocks.put(l, Thread.currentThread());
   }

   /**
    * Records that the current thread has released a lockable resource.
    */
   private void unregisterHeld(Lockable l)
   {
      heldLocks.remove(l);
   }

   /**
    * Checks if there is a deadlock, if so an {@link InterruptedException}
    * will be thrown.
    * <p>
    * The caller must have already called {@code register(l)} before invoking
    * this method, so the current thread's "waiting-for" entry is visible to
    * any concurrent deadlock check at the moment we walk the graph.
    * <p>
    * We spin-wait briefly for the lock's owner thread to appear in the waiting
    * map: it may have just acquired the lock and not yet entered its own
    * {@code register()} call, or it may genuinely hold the lock without waiting
    * for anything.  A short spin closes this window without a hard sleep.
    */
   private void checkDeadLock(Lockable l) throws InterruptedException
   {
      if (!l.isLocked())
      {
         LOG.trace("The lock is not locked so we cannot have a deadlock");
         return;
      }
      final Thread owner = l.getOwner();
      if (owner == null || owner == Thread.currentThread())
      {
         LOG.trace("The lock is not locked or the lock owner is the current "
            + "thread so we cannot have a deadlock");
         return;
      }
      // Walk the wait-for graph.  We retry only for the first hop (the direct
      // owner of l) because that thread may be in the tiny window between
      // acquiring l and entering its own register() call.  All subsequent hops
      // must already be registered if they are genuinely waiting.
      final int MAX_SPINS = 50;
      for (int spin = 0; spin <= MAX_SPINS; spin++)
      {
         if (locks.containsKey(owner) || spin == MAX_SPINS)
         {
            // Either the owner is now registered (common case) or we have
            // exhausted the spin budget – in both cases run a full graph walk.
            walkDeadLockGraph(l, owner);
            return;
         }
         Thread.yield();
      }
   }

   /**
    * Walks the wait-for graph starting from {@code owner} to detect whether
    * a cycle exists that involves the current thread.  Throws
    * {@link InterruptedException} and interrupts the victim if a deadlock is
    * confirmed; returns normally otherwise.
    */
   private void walkDeadLockGraph(Lockable l, Thread owner) throws InterruptedException
   {
      Thread currentOwner = owner;
      while (true)
      {
         Lockable waited = locks.get(currentOwner);
         if (waited == null)
         {
            // currentOwner is not waiting on anything – no cycle through it.
            LOG.trace("Owner {} has no registered lockable resource – no deadlock", currentOwner);
            return;
         }
         Thread nextOwner = waited.getOwner();
         if (nextOwner == null)
         {
            LOG.trace("The lockable resource has no owner anymore so we cannot have a deadlock");
            return;
         }
         if (nextOwner == Thread.currentThread())
         {
            // Cycle detected: current thread is waiting for owner, and
            // owner (transitively) is waiting for current thread.
            if (owner == l.getOwner() && l.isLocked())
            {
               LOG.debug("A deadlock has been detected, both threads will be interrupted");
               owner.interrupt();
               throw new InterruptedException();
            }
            else
            {
               LOG.trace("The owner has changed or the resource is no more locked so we cannot have a deadlock");
               return;
            }
         }
         currentOwner = nextOwner;
      }
   }

   /**
    * Internal sub-class of {@link ReentrantLock} needed to be able to register
    * and unregister all the locks automatically
    */
   private class InternalReentrantLock extends ReentrantLock implements Lockable
   {

      /**
       * The serial version UID
       */
      private static final long serialVersionUID = 1696442015918441687L;

      /**
       * {@inheritDoc}
       */
      public Thread getOwner()
      {
         return super.getOwner();
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public void lock()
      {
         register(this);
         super.lock();
         // Now we hold the lock: stop "waiting" and start "holding"
         unregister(this);
         registerHeld(this);
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public void unlock()
      {
         super.unlock();
         // Release hold tracking only when the lock is fully released
         // (hold count drops to zero for this thread).
         if (!isHeldByCurrentThread())
         {
            unregisterHeld(this);
         }
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public void lockInterruptibly() throws InterruptedException
      {
         register(this);
         try
         {
            checkDeadLock(this);
            super.lockInterruptibly();
            // Acquired successfully: start "holding"
            registerHeld(this);
         }
         finally
         {
            // Always stop "waiting"
            unregister(this);
         }
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public boolean tryLock()
      {
         register(this);
         boolean result = super.tryLock();
         unregister(this);
         if (result)
         {
            registerHeld(this);
         }
         return result;
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException
      {
         register(this);
         try
         {
            checkDeadLock(this);
            boolean result = super.tryLock(timeout, unit);
            if (result)
            {
               registerHeld(this);
            }
            return result;
         }
         finally
         {
            unregister(this);
         }
      }
   }

   /**
    * Internal sub-class of {@link FutureTask} needed to be able to register
    * and unregister all the tasks automatically
    */
   private class InternalFutureTask<V> extends FutureTask<V> implements Lockable
   {
      /**
       * The current owner of exclusive mode synchronization.
       */
      private final AtomicReference<Thread> exclusiveOwnerThread = new AtomicReference<Thread>();

      /**
       * {@inheritDoc}
       */
      public InternalFutureTask(Callable<V> callable)
      {
         super(callable);
      }

      /**
       * {@inheritDoc}
       */
      public InternalFutureTask(Runnable runnable, V result)
      {
         super(runnable, result);
      }

      /**
       * Checks if there is a deadlock, if so it will interrupt the thread waiting for the lock
       */
      private void checkDeadLock()
      {
         try
         {
            LockManager.this.checkDeadLock(this);
         }
         catch (InterruptedException e)
         {
            LOG.debug("An InterruptedException has been caught, but a task must not be interrupted");
         }
      }


      /**
       * {@inheritDoc}
       */
      @Override
      public V get() throws InterruptedException, ExecutionException
      {
         register(this);
         checkDeadLock();
         try
         {
            return super.get();
         }
         finally
         {
            unregister(this);
         }
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException
      {
         register(this);
         checkDeadLock();
         try
         {
            return super.get(timeout, unit);
         }
         finally
         {
            unregister(this);
         }
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public void run()
      {
         if (!exclusiveOwnerThread.compareAndSet(null, Thread.currentThread()))
         {
            // Already running on another thread – FutureTask.run() will be a no-op anyway
            return;
         }
         registerHeld(this);
         try
         {
            super.run();
         }
         finally
         {
            exclusiveOwnerThread.compareAndSet(Thread.currentThread(), null);
            unregisterHeld(this);
         }
      }

      /**
       * Gives the Owner of the task
       */
      public Thread getOwner()
      {
         return exclusiveOwnerThread.get();
      }

      /**
       * Indicates whether the task is locked or not, in practice it will be considered as locked if it is not done
       */
      public boolean isLocked()
      {
         return !isDone();
      }
   }

   /**
    * Defines a lockable resource
    */
   private static interface Lockable
   {
      /**
       * Gives the owner in case the resource is locked
       */
      Thread getOwner();

      /**
       * Indicates whether the resource is locked or not
       */
      boolean isLocked();
   }
}
/**
 * File: thread-pool.h
 * -------------------
 * This class defines the ThreadPool class, which accepts a collection
 * of thunks (which are zero-argument functions that don't return a value)
 * and schedules them in a FIFO manner to be executed by a constant number
 * of child threads that exist solely to invoke previously scheduled thunks.
 */

#ifndef _thread_pool_
#define _thread_pool_
#include <cstddef>     // for size_t
#include <functional>  // for the function template used in the schedule signature
#include <thread>      // for thread
#include <vector>      // for vector
#include <queue>
#include <mutex>
#include <condition_variable>
#include "semaphore.h"

class ThreadPool {
 public:

/**
 * Constructs a ThreadPool configured to spawn up to the specified
 * number of threads.
 */
  ThreadPool(size_t numThreads);

/**
 * Schedules the provided thunk (which is something that can
 * be invoked as a zero-argument function without a return value)
 * to be executed by one of the ThreadPool's threads as soon as
 * all previously scheduled thunks have been handled.
 */
  void schedule(const std::function<void(void)>& thunk);

/**
 * Blocks and waits until all previously scheduled thunks
 * have been executed in full.
 */
  void wait();

/**
 * Waits for all previously scheduled thunks to execute, and then
 * properly brings down the ThreadPool and any resources tapped
 * over the course of its lifetime.
 */
  ~ThreadPool();
  
 private:
  std::thread dt;   // dispatcher thread handle
  struct worker{
    std::thread thread;
    std::function<void(void)> thunk;
  };
  std::queue<int> availableWorkers; //indices into available workers in wts
  std::vector<worker> wts; // worker thread handles
  std::vector<semaphore> worker_permits;
  std::queue<std::function<void(void)>> thunks;
  semaphore dispatcher_permit;
  semaphore workers_permit;
  std::mutex thunks_lock;
  std::mutex available_lock;
  std::mutex wts_lock;
  std::mutex permits_lock;
  std::condition_variable_any cv;
  std::mutex cv_lock;
  std::mutex int_lock;
  int dispatched_finished;
  bool break_from_loop;
  std::mutex bool_lock;

  void dispatcher();
  void worker(size_t workerID);

/**
 * ThreadPools are the type of thing that shouldn't be cloneable, since it's
 * not clear what it means to clone a ThreadPool (should copies of all outstanding
 * functions to be executed be copied?).
 *
 * In order to prevent cloning, we remove the copy constructor and the
 * assignment operator.  By doing so, the compiler will ensure we never clone
 * a ThreadPool.
 */
  ThreadPool(const ThreadPool& original) = delete;
  ThreadPool& operator=(const ThreadPool& rhs) = delete;
};

#endif

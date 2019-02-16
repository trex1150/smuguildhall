/**
 * File: thread-pool.cc
 * --------------------
 * Presents the implementation of the ThreadPool class.
 */

#include "thread-pool.h"
#include "ostreamlock.h"
#include "assert.h"
#include <iostream>
using namespace std;

void ThreadPool::dispatcher() {
    while (true) {
        dispatcher_permit.wait();
        if (break_from_loop) break;
        workers_permit.wait();
        available_lock.lock();
        int workerID = availableWorkers.front();
        availableWorkers.pop();
        available_lock.unlock();
        thunks_lock.lock();
        function<void(void)> thunk = thunks.front();
        thunks.pop();
        thunks_lock.unlock();
        wts_lock.lock();
        wts[workerID].thunk = thunk;
        wts_lock.unlock();
        worker_permits[workerID].signal();
    }
}

void ThreadPool::worker(size_t workerID) {
    while (true) {
        worker_permits[workerID].wait();
        if (break_from_loop) break;
        function<void(void)> &thunk = wts[workerID].thunk;
        thunk();
        available_lock.lock();
        availableWorkers.push(workerID);
        available_lock.unlock();
        workers_permit.signal();
        cv_lock.lock();
        dispatched_finished--;
        cv_lock.unlock();
        if (dispatched_finished == 0) cv.notify_one();
    }
}


ThreadPool::ThreadPool(size_t numThreads) : wts(numThreads), worker_permits(numThreads), dispatcher_permit(0), workers_permit(0), dispatched_finished(0), break_from_loop(false) {
    dt = thread([this]() {
        dispatcher();
    });
    for (size_t workerID = 0; workerID < numThreads; workerID++) {
        availableWorkers.push(workerID);
        workers_permit.signal();
        wts[workerID].thread = thread([this](size_t workerID) {
            worker(workerID);
        }, workerID);
    }
}

void ThreadPool::schedule(const function<void(void)>& thunk) {
    thunks_lock.lock();
    thunks.push(thunk);
    thunks_lock.unlock();
    dispatcher_permit.signal();
    cv_lock.lock();
    dispatched_finished++;
    cv_lock.unlock();
}

void ThreadPool::wait() {
    cv_lock.lock();
    cv.wait(cv_lock, [this] {return dispatched_finished == 0;});
    cv_lock.unlock();
}

ThreadPool::~ThreadPool() {
    wait();
    bool_lock.lock();
    break_from_loop = true;
    bool_lock.unlock();
    dispatcher_permit.signal();
    for (size_t i = 0; i < worker_permits.size(); i++) {
        worker_permits[i].signal();
    }
    dt.join();
    for (size_t i = 0; i < wts.size(); i++) {
       wts[i].thread.join();
    }
}

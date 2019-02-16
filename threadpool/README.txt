Name: Trevor Rex

Directory structure: This directory contains largely resource files for the threadpool to operate on, as well as some class frameworks such as an html-document and rss-feed and some testing modules. These were all provided by Stanford course faculty. The files I wrote are news-aggregator.cc and threadpool.cc and their respective .h files.

File explanation: thread-pool.cc contains a custom systems-level implementation of a threadpool in C++ leveraging a queue to keep track of the workers. The threapool dispatches workers to complete tasks in parallel and waits for them to finish. Lock prevention is a key concern in this project. The news-aggregator file leverages the threadpool in order to aggregate data from various news articles.

Date: May 2017
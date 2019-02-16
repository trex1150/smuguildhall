/**
 * File: news-aggregator.cc
 * --------------------------------
 * Presents the implementation of the NewsAggregator class.
 */

#include "news-aggregator.h"
#include <iostream>
#include <iomanip>
#include <getopt.h>
#include <libxml/parser.h>
#include <libxml/catalog.h>
// you will almost certainly need to add more system header includes
#include <thread>
// I'm not giving away too much detail here by leaking the #includes below,
// which contribute to the official CS110 staff solution.
#include "rss-feed.h"
#include "rss-feed-list.h"
#include "html-document.h"
#include "html-document-exception.h"
#include "rss-feed-exception.h"
#include "rss-feed-list-exception.h"
#include "utils.h"
#include "ostreamlock.h"
#include "string-utils.h"
#include "thread-pool.h"
using namespace std;

/**
 * Factory Method: createNewsAggregator
 * ------------------------------------
 * Factory method that spends most of its energy parsing the argument vector
 * to decide what rss feed list to process and whether to print lots of
 * of logging information as it does so.
 */
static const string kDefaultRSSFeedListURL = "small-feed.xml";
NewsAggregator *NewsAggregator::createNewsAggregator(int argc, char *argv[]) {
  struct option options[] = {
    {"verbose", no_argument, NULL, 'v'},
    {"quiet", no_argument, NULL, 'q'},
    {"url", required_argument, NULL, 'u'},
    {NULL, 0, NULL, 0},
  };
  
  string rssFeedListURI = kDefaultRSSFeedListURL;
  bool verbose = false;
  while (true) {
    int ch = getopt_long(argc, argv, "vqu:", options, NULL);
    if (ch == -1) break;
    switch (ch) {
    case 'v':
      verbose = true;
      break;
    case 'q':
      verbose = false;
      break;
    case 'u':
      rssFeedListURI = optarg;
      break;
    default:
      NewsAggregatorLog::printUsage("Unrecognized flag.", argv[0]);
    }
  }
  
  argc -= optind;
  if (argc > 0) NewsAggregatorLog::printUsage("Too many arguments.", argv[0]);
  return new NewsAggregator(rssFeedListURI, verbose);
}

/**
 * Method: buildIndex
 * ------------------
 * Initalizex the XML parser, processes all feeds, and then
 * cleans up the parser.  The lion's share of the work is passed
 * on to processAllFeeds, which you will need to implement.
 */
void NewsAggregator::buildIndex() {
  if (built) return;
  built = true; // optimistically assume it'll all work out
  xmlInitParser();
  xmlInitializeCatalog();
  processAllFeeds();
  xmlCatalogCleanup();
  xmlCleanupParser();
}

/**
 * Method: queryIndex
 * ------------------
 * Interacts with the user via a custom command line, allowing
 * the user to surface all of the news articles that contains a particular
 * search term.
 */
void NewsAggregator::queryIndex() const {
  static const size_t kMaxMatchesToShow = 15;
  while (true) {
    cout << "Enter a search term [or just hit <enter> to quit]: ";
    string response;
    getline(cin, response);
    response = trim(response);
    if (response.empty()) break;
    const vector<pair<Article, int> >& matches = index.getMatchingArticles(response);
    if (matches.empty()) {
      cout << "Ah, we didn't find the term \"" << response << "\". Try again." << endl;
    } else {
      cout << "That term appears in " << matches.size() << " article"
           << (matches.size() == 1 ? "" : "s") << ".  ";
      if (matches.size() > kMaxMatchesToShow)
        cout << "Here are the top " << kMaxMatchesToShow << " of them:" << endl;
      else if (matches.size() > 1)
        cout << "Here they are:" << endl;
      else
        cout << "Here it is:" << endl;
      size_t count = 0;
      for (const pair<Article, int>& match: matches) {
        if (count == kMaxMatchesToShow) break;
        count++;
        string title = match.first.title;
        if (shouldTruncate(title)) title = truncate(title);
        string url = match.first.url;
        if (shouldTruncate(url)) url = truncate(url);
        string times = match.second == 1 ? "time" : "times";
        cout << "  " << setw(2) << setfill(' ') << count << ".) "
             << "\"" << title << "\" [appears " << match.second << " " << times << "]." << endl;
        cout << "       \"" << url << "\"" << endl;
      }
    }
  }
}


void NewsAggregator::handleIntersections(std::pair<server, title> a_fields, Article article, std::vector<string> curr_tokens) {
    auto it = tokenIntersections.find(a_fields);
    if (it != tokenIntersections.end()) {
        std::pair<vector<std::string>, url> tokens_and_url = it->second; 
        vector<string> map_tokens = tokens_and_url.first;
        url this_url = tokens_and_url.second;
        tokenIntersections.erase(it);
        sort(curr_tokens.begin(), curr_tokens.end());
        sort(map_tokens.begin(), map_tokens.end());
        std::vector<string> intersected_tokens; 
        set_intersection(curr_tokens.begin(), curr_tokens.end(), map_tokens.begin(), map_tokens.end(), back_inserter(intersected_tokens));
        tokens_and_url.first = intersected_tokens;
        if (article.url < this_url) tokens_and_url.second = article.url;
        mapM.lock();
        tokenIntersections.insert(make_pair(a_fields, tokens_and_url));
        mapM.unlock();
    } else {
        mapM.lock();
        tokenIntersections.insert(make_pair(a_fields, make_pair(curr_tokens, article.url)));
        mapM.unlock();
    }
}

/**
 * Private Constructor: NewsAggregator
 * -----------------------------------
 * Self-explanatory.  You may need to add a few lines of code to
 * initialize any additional fields you add to the private section
 * of the class definition.
 */
NewsAggregator::NewsAggregator(const string& rssFeedListURI, bool verbose): 
  log(verbose), rssFeedListURI(rssFeedListURI), built(false), xmlPool(3), articlePool(20) {}

void NewsAggregator::processArticles(Article article) {
    std::pair<server, title> a_fields;
    a_fields.first = getURLServer(article.url);
    a_fields.second = article.title;
    HTMLDocument document(article.url);
    try {
        document.parse();
    } catch(HTMLDocumentException) {
        cout << "Error 3" << endl;
        //log.somethingorother
        return;
    }
    std::vector<string> curr_tokens(document.getTokens());
    handleIntersections(a_fields, article, curr_tokens);
}

void NewsAggregator::processFeeds(std::pair<url, title> pair) {    
    RSSFeed feed(pair.first);
    try {
        feed.parse();
    } catch(RSSFeedException) {
        cout << "Error 2" << endl;
        //log.somethingorother
        return;
    }
    const std::vector<Article>& articles = feed.getArticles();
    vector<thread> article_threads;
    for (const Article& article: articles) {
        string server = getURLServer(article.url);
        /*
        semaphoreM.lock();
        unique_ptr<semaphore>& up = server_limits[server];
        if (up == nullptr) {
            up.reset(new semaphore(8));
        }
        semaphoreM.unlock();
        */
        setM.lock();
        if (urls.find(article.url) != urls.end()) {
            setM.unlock();
            continue;
        }
        urls.insert(article.url);
        setM.unlock();
       // article_permits.wait();
       // up->wait();
        articlePool.schedule([this, article] {
            processArticles(article);
        });
        /*article_threads.push_back(thread([this](semaphore& s, Article article, unique_ptr<semaphore>& up) {
            s.signal(on_thread_exit);
            up->signal(on_thread_exit);
            this->processArticles(s, article);
        }, ref(article_permits), article, ref(up)));  */
    }
}

void NewsAggregator::compileIndex() {
    for(map<pair<server, title>, pair<vector<std::string>, url>>::iterator it = tokenIntersections.begin(); it != tokenIntersections.end(); it++) {
        Article article;
        article.url = it->second.second;
        article.title = it->first.second;
        index.add(article, it->second.first);
    }
}

/**
 * Private Method: processAllFeeds
 * -------------------------------
 * Downloads and parses the encapsulated RSSFeedList, which itself
 * leads to RSSFeeds, which themsleves lead to HTMLDocuemnts, which
 * can be collectively parsed for their tokens to build a huge RSSIndex.
 * 
 * The vast majority of your Assignment 5 work has you implement this
 * method using multithreading while respecting the imposed constraints
 * outlined in the spec.
 */

void NewsAggregator::processAllFeeds() {
    RSSFeedList rssFeedList(rssFeedListURI);
    try {
        rssFeedList.parse();
    } catch(RSSFeedListException) {
        cout << "Error 1" << endl;
        return;
    }
    const std::map<url, title> &feeds = rssFeedList.getFeeds();
    vector<thread> feed_threads;
    for (const std::pair<url, title>& pair: feeds) {
        xmlPool.schedule([this, pair] {
            processFeeds(pair);
       });
    }
    xmlPool.wait();
    articlePool.wait();
    compileIndex();
}

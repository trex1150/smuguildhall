// This is the CPP file you will edit and turn in.
// Also remove these comments here and add your own, along with
// comments on every function and on complex code sections.
// TODO: write comment header for this file; remove this comment

#include "trailblazer.h"
#include "vector.h"
#include "graph.h"
#include "basicgraph.h"
#include "queue.h"
#include "pqueue.h"
#include "stack.h"

using namespace std;

bool depthFirstSearchHelper(BasicGraph& graph, Vertex* start, Vertex* end, Vector<Vertex*>& path);
Set<Vertex*> mergeClusters(Set<Vertex*> startSet, Set<Vertex*> endSet);

/* depthFirstSearch()
 * Given a graph, returns the first path it finds between two given vertices. Calls a recursive helper method
 * that implements the actual algorithm.
 * Parameters: BasicGraph& graph, Vertex* start, Vertex* end
 * Return values: Vector<Vertex>* path
 * */

Vector<Vertex*> depthFirstSearch(BasicGraph& graph, Vertex* start, Vertex* end) {
    graph.resetData();
    Vector<Vertex*> path;
    if (depthFirstSearchHelper(graph, start, end, path)) {
        return path;
    } else {
        path.remove(0); //in the case it finds nothing, remove the only element from path and return it
    }
    return path;
}

/* depthFirstSearchHelper()
 * Recursive backtracking algorithm implemented by depthFirstSearch() that returns whether or not a path exists
 * from a given start and end vertex. Updates a vector of paths as it recursively backtracks through the given
 * graph, updating the colors of the vertices it visits depending on where it is in the algorithm.
 * Parameters: BasicGraph& graph, Vertex* start, Vertex* end, Vector<Vertex*>& path
 * Return values: bool
 * */

bool depthFirstSearchHelper(BasicGraph& graph, Vertex* start, Vertex* end, Vector<Vertex*>& path) {
    start->visited = true;
    path.add(start);
    start->setColor(GREEN);
    if (start == end) return true;
    for (Edge* e: graph.getEdgeSet(start)) {
        if (!e->finish->visited) {
            if (depthFirstSearchHelper(graph, e->finish, end, path)) {
                return true;
            } else {
                path.remove(path.size()-1);
                e->finish->setColor(GRAY);
            }
        } else {
            continue;
        }
    }
    return false;
}

/* breathFirstSearch()
 * Finds the shortest distance between two vertices (assuming an unweighted graph) using a queue algorithm
 * that enqueues partial paths, returning a path as soon as the start and end vertices are the same.
 * Parameters: BasicGraph& graph, Vertex* start, Vertex* end
 * Return values: Vector<Vertex*> path
 * */
Vector<Vertex*> breadthFirstSearch(BasicGraph& graph, Vertex* start, Vertex* end) {
    graph.resetData();
    Vector<Vertex*> path;
    path.add(start);
    Queue<Vector<Vertex*>> pathQ;
    start->setColor(YELLOW);
    pathQ.enqueue(path);
    start->visited = true;
    while (!pathQ.isEmpty()) {
        Vector<Vertex*> currPath = pathQ.dequeue();
        Vertex* currNode = currPath[currPath.size()-1];
        if (currNode == end) return currPath;
        currNode->setColor(GREEN);
        for (Edge* e: graph.getEdgeSet(currNode)) {
            if (!e->finish->visited) {
                e->finish->visited = true;
                e->finish->setColor(YELLOW);
                Vector<Vertex*> currPathCopy = currPath;
                currPathCopy.add(e->finish);
                e->finish->previous = currNode;
                pathQ.enqueue(currPathCopy);
            }
        }
    }
    path.remove(0); //in the case it finds nothing, remove the only element from path and return it
    return path;
}

/* dijkstrasAlgorithm()
 * Operates exactly the same as breathFirstSearch, but uses a PriorityQueue algorithm instead of a Queue algorithm.
 * When enqueueing partial paths in dijkstras, includes the total cost of the path so far.
 * Parameters: BasicGraph& graph, Vertex* start, Vertex* end
 * Return values: Vector<Vertex*> path
 * */
Vector<Vertex*> dijkstrasAlgorithm(BasicGraph& graph, Vertex* start, Vertex* end) {
    graph.resetData();
    for (Vertex* v: graph.getVertexSet()) {
        v->cost = POSITIVE_INFINITY;
    }
    Vector<Vertex*> path;
    start->cost = 0;
    path.add(start);
    PriorityQueue<Vector<Vertex*>> pathQ;
    start->setColor(YELLOW);
    pathQ.enqueue(path, start->cost);
    start->visited = true;
    while (!pathQ.isEmpty()) {
        Vector<Vertex*> currPath = pathQ.dequeue();
        Vertex* currNode = currPath[currPath.size()-1];
        int costCurr = 0;
        for (int i=0; i < currPath.size(); i++) {
            costCurr += currPath[i]->cost;
        }
        currNode->setColor(GREEN);
        if (currNode == end) return currPath;
        for (Edge* e: graph.getEdgeSet(currNode)) {
            if (!e->finish->visited) {
                e->finish->visited = true;
                e->finish->setColor(YELLOW);
                e->finish->previous = currNode;
                int costPrev = costCurr + e->cost;
                Vector<Vertex*> currPathCopy = currPath;
                e->finish->cost = e->cost;
                currPathCopy.add(e->finish);
                pathQ.enqueue(currPathCopy, costPrev);
            }
        }
    }
    path.remove(0);
    return path;
}

/* aStar()
 * Operates the exact same way as dijkstras algorithm, but includes a hueristic that underestimates the distance
 * between a given node and the end node that it's trying to find. This directs the search in the correct direction
 * and, in general, prevents it from straying away from the correct path.
 * Parameters: BasicGraph& graph, Vertex* start, Vertex* end
 * Return values: Vector<Vertex*> path;
 * */

Vector<Vertex*> aStar(BasicGraph& graph, Vertex* start, Vertex* end) {
    graph.resetData();
    for (Vertex* v: graph.getVertexSet()) {
        v->cost = POSITIVE_INFINITY;
    }
    Vector<Vertex*> path;
    start->cost = heuristicFunction(start, end);
    path.add(start);
    PriorityQueue<Vector<Vertex*>> pathQ;
    start->setColor(YELLOW);
    pathQ.enqueue(path, start->cost);
    start->visited = true;
    while (!pathQ.isEmpty()) {
        Vector<Vertex*> currPath = pathQ.dequeue();
        Vertex* currNode = currPath[currPath.size()-1];
        int costCurr = 0;
        for (int i=0; i < currPath.size(); i++) {
            costCurr += currPath[i]->cost;
        }
        currNode->setColor(GREEN);
        if (currNode == end) return currPath;
        for (Edge* e: graph.getEdgeSet(currNode)) {
            if (!e->finish->visited) {
                e->finish->visited = true;
                e->finish->setColor(YELLOW);
                e->finish->previous = currNode;
                int costPrev = costCurr + e->cost;
                Vector<Vertex*> currPathCopy = currPath;
                e->finish->cost = e->cost;
                currPathCopy.add(e->finish);
                pathQ.enqueue(currPathCopy, costPrev+heuristicFunction(e->finish, end));
            }
        }
    }
    path.remove(0);
    return path;
}

/* kruskal()
 * Algorithm that, given a graph, connects each of the nodes in a way such that creates a minimum spanning tree
 * of the given graph. Beginning with a set of set of vertex*'s, the algorithm puts each of the nodes from the graph
 * into its own set. It then queues up each of the graph's edges based on their cost. The algorithm dequeues an edge,
 * then goes through the set of sets to see if a set already contains the endpoints of that edge (indicating their
 * connectedness). If not, the two edges' sets are removed, merged, and then readded to the set of sets. At the end
 * of this process, the dequeued edge is added to the minimum spanning tree set. Once this problem is done, the mst set
 * will contain the minimum spanning tree of the given graph which a maze can then be generated from.
 * Paremeters: BasicGraph& graph
 * Retrun values: Set<Edge*> mst
 * */

Set<Edge*> kruskal(BasicGraph& graph) {
    Set<Set<Vertex*>> mstSet;
    Set<Edge*> mst;
    PriorityQueue<Edge*> edgeQ;
    for (Edge* e: graph.getEdgeSet()) {
        edgeQ.enqueue(e, e->cost);
    }
    for (Vertex* v: graph.getVertexSet()) {
        Set<Vertex*> vertexSet;
        vertexSet.add(v);
        mstSet.add(vertexSet);
    }
    while (!edgeQ.isEmpty()) {
        Edge* edge = edgeQ.dequeue();
        Set<Vertex*> startSet;
        Set<Vertex*> endSet;
        bool found = false;
        for (Set<Vertex*> set: mstSet) {
            if (!(set.contains(edge->start) || set.contains(edge->finish))) {
                continue;
            } else if (set.contains(edge->start) && set.contains(edge->finish)) {
                continue;
            } else if (set.contains(edge->start)) {
               startSet = set;
               found = true;
            } else if (set.contains(edge->finish)) {
               endSet = set;
               found = true;
            }
        }
        if (found) {
            mstSet.remove(startSet);
            mstSet.remove(endSet);
            Set<Vertex*> mergedSet = mergeClusters(startSet, endSet);
            mstSet.add(mergedSet);
            mst.add(edge);
        }
    }

    return mst;
}

/* mergeClusters()
 * Given two sets of Vertex*'s returns a separate set that contains every element from each of the two given sets.
 * Parameters: Set<Vertex*> startSet, Set<Vertex*> endSet
 * Return values: Set<Vertex*> mergedSet
 * */

Set<Vertex*> mergeClusters(Set<Vertex*> startSet, Set<Vertex*> endSet) {
   Set<Vertex*> mergedSet;
   for (Vertex* vertex: startSet) {
       mergedSet.add(vertex);;
   }
   for (Vertex* vertex: endSet) {
       mergedSet.add(vertex);
   }
   return mergedSet;
}

/* This program uses the Huffman coding algorithm for compression.
 *Any file can be compressed by this method, often with substantial
 *savings. Decompression will faithfully reproduce the original.
 * */

#include "encoding.h"
#include "istream"
#include "bitstream.h"
#include "streambuf"
#include "ostream"
#include "filelib.h"
#include "pqueue.h"
#include "HuffmanNode.h"
#include "huffmanutil.h"

void loadPQ(const Map<int, int>& freqTable, PriorityQueue<HuffmanNode*>& treeQ, Vector<int>& keys);
void buildEncodingMapHelper(HuffmanNode* encodingTree, Map<int, string>& encodingMap, string& value);
int decodeDataHelper(ibitstream& input, HuffmanNode* encodingTree, ostream& output);

/* buildFrequencyTable()
 * Parameters: istream& input
 * Return values: Map<int, int> freqTable
 * Takes in an input stream and creates and returns a map with keys of each character in the input, and values
 * of the number of times the respective character appears.
 * */

Map<int, int> buildFrequencyTable(istream& input) {
    Map<int, int> freqTable;
    while (input.peek() != -1) {
        char ch = input.get();
        if (!freqTable.containsKey(ch)) {
            freqTable.put(ch, 1);
        } else {
            int value = freqTable.get(ch);
            freqTable.remove(ch);
            value++;
            freqTable.put(ch, value);
        }
    }
    freqTable.put(256, 1);
    return freqTable;
}

/* buildEncodingTree()
 * Parameters: Map<int, int> freqTable
 * Return values: HuffmanNode* parent
 * Takes in the previous frequency table, and creates a binary tree with the leaves being a node with a character
 * value. Returns the parent of the entire tree.
 * */

HuffmanNode* buildEncodingTree(const Map<int, int>& freqTable) {
    PriorityQueue<HuffmanNode*> treeQ;
    Vector<int> keys = freqTable.keys();
    loadPQ(freqTable, treeQ, keys);
    HuffmanNode* parent;
    if (treeQ.size() == 1) {
        parent = new HuffmanNode(PSEUDO_EOF, 1, NULL, NULL);
        return parent;
    }
    while (treeQ.size() > 1) {
        HuffmanNode* node1 = treeQ.dequeue();
        HuffmanNode* node2 = treeQ.dequeue();
        int freqSum = node1->count + node2->count;
        parent = new HuffmanNode(NOT_A_CHAR, freqSum, node1, node2);
        treeQ.enqueue(parent, freqSum);
    }
    return parent;
}

/* loadPQ()
 * Parameters: Map<int, int>& freqTable, PriorityQueue<HuffmanNode*>& treeQ, Vector<int>& keys
 * Return values: None
 * A method that loads the priority queue used within buildEncodingTree's algorithm. Takes in the frequency table,
 * an empty priority queue, and a vector of the keys from the frequency table. Puts each key and value from the map
 * into a node and then enqueues each node.
 * */

void loadPQ(const Map<int, int>& freqTable, PriorityQueue<HuffmanNode*>& treeQ, Vector<int>& keys) {
    for (int i=0; i < keys.size(); i++) {
        int key = keys[i];
        int value = freqTable.get(key);
        HuffmanNode* node = new HuffmanNode(key, value, NULL, NULL);
        treeQ.enqueue(node, value);
    }
}

/* buildEncodingMap()
 * Parameters: HuffmanNode* encodingTree
 * Return values: Map<int, string> buildEncodingMap
 * Takes in the parent node encodingTree to the tree of nodes created in the buildEncodingTree method. Traverses
 * the tree, assigning binary values to the characters depending on the path taken to the character's node.
 * */

Map<int, string> buildEncodingMap(HuffmanNode* encodingTree) {
    Map<int, string> encodingMap;
    if (encodingTree->zero == NULL && encodingTree->one == NULL) {
        encodingMap.put(PSEUDO_EOF, "");
        return encodingMap;
    }
    string value = "";
    buildEncodingMapHelper(encodingTree, encodingMap, value);
    return encodingMap;
}

/* buildEncodingMapHelper()
 * Parameters: HuffmanNode* encodingTree, Map<int, string>& encodingMap, string& value
 * Return values: none
 * A helper method for buildEncodingMap that takes the same parent node as the previous method, the map that
 * buildEncodingMap returns, and a string that keeps track of the traversions through the tree so that we can
 * find the character's binary association.
 * */

void buildEncodingMapHelper(HuffmanNode* encodingTree, Map<int, string>& encodingMap, string& value) {
    int key = 0;
    string valueCopy = value;
    if (encodingTree == NULL) {
        //nothing
    } else {
        if (encodingTree->zero != NULL) {
            key = encodingTree->zero->character;
            value += '0';
            if (key != 257) { //edge case
                encodingMap.put(key, value);
            }
            buildEncodingMapHelper(encodingTree->zero, encodingMap, value);
        }
        if (encodingTree->one != NULL) {
            key = encodingTree->one->character;
            valueCopy += '1';
            if (key!= 257) { //edge case
                encodingMap.put(key,valueCopy);
            }
            buildEncodingMapHelper(encodingTree->one, encodingMap, valueCopy);
        }
    }
}

/* encodeData()
 * Parameters: istream& input, Map<int, string>& encodingMap, obitstream& output)
 * Return values: none
 * Method that takes in an input stream, reads it one character at a time, then writes its associated binary
 * representation to an output stream by reading it in the map.
 * */

void encodeData(istream& input, const Map<int, string>& encodingMap, obitstream& output) {
    while (input.peek() != -1) {
        char ch = input.get();
        string value = encodingMap.get(ch);
        for (int i=0; i < value.length(); i++) {
            string bitString = charToString(value[i]);
            int bit = stringToInteger(bitString);
            output.writeBit(bit);

        }
    }
    string value = encodingMap.get(PSEUDO_EOF);
    for (int i=0; i < value.length(); i++) {
        string bitString = charToString(value[i]);
        int bit = stringToInteger(bitString);
        output.writeBit(bit);

    }

}

/* decodeData()
 * Parameters: ibitstream& input, HuffmanNode* encodingTree, ostream& output
 * Return values: none
 * Method that reads a given input stream then recursively traverses the encodingTree until it finds a leaf, then
 * printing its character representation and writing it to the output stream.
 * */

void decodeData(ibitstream& input, HuffmanNode* encodingTree, ostream& output) {
    while (true) {
        if (decodeDataHelper(input, encodingTree, output) == PSEUDO_EOF) {
            break;
        }
    }
}

/* decodeDataHelper()
 * Parameters: input, HuffmanNode* encodingTree, ostream& output
 * Return values: int decodeDataHelper()
 * Helper method to decodeData that recursively traverses the encodingTree, writing it to the output if it isn't
 * PSEUDO_EOF. It then returns the character associated with it so that the main decodeData method knows when
 * to break.
 * */

int decodeDataHelper(ibitstream& input, HuffmanNode* encodingTree, ostream& output) {
    if (encodingTree->isLeaf()) {
        if (encodingTree->character != PSEUDO_EOF) {
            output.put(encodingTree->character);
        }
        return encodingTree->character;
    }
    int bit = input.readBit();
    if (bit==0) {
        return decodeDataHelper(input, encodingTree->zero, output);
    } else {
        return decodeDataHelper(input, encodingTree->one, output);
    }
}

/* compress()
 * Parameters: istream& input, obitstream& output
 * Return values: none
 * Calls the previous methods to compress the given input stream and write the compressed version to the output stream.
 * Outputs the frequency table to the beginning of the file so that decompress has the character-binary associations.
 * */
void compress(istream& input, obitstream& output) {
    Map<int, int> freqTable = buildFrequencyTable(input);
    output << freqTable;
    rewindStream(input);
    HuffmanNode* encodingTree = buildEncodingTree(freqTable);
    Map<int, string> encodingMap = buildEncodingMap(encodingTree);
    encodeData(input, encodingMap, output);
    freeTree(encodingTree);
}

/* decompress()
 * Parameters: ibitstream& input, ostream& output
 * Return values: none
 * Creates an empty frequency table map, loads it with the beginning contents of the file, then uses the table
 * to call the previous methods and eventually decodeData.
 * */

void decompress(ibitstream& input, ostream& output) {
    Map<int, int> freqTable;
    input >> freqTable;
    HuffmanNode* encodingTree = buildEncodingTree(freqTable);
    Map<int, string> encodingMap = buildEncodingMap(encodingTree);
    decodeData(input, encodingTree, output);
    freeTree(encodingTree);
}

/* freeTree()
 * Parameters: HuffmanNode* node
 * Return values: none
 * Uses a given parent node to recursively traverse the associated tree and delete all its nodes.
 * */
void freeTree(HuffmanNode* node) {
    if (node->isLeaf()) {
        delete node;
    }
    if (node->one != NULL) {
        freeTree(node->one);
    }
    if (node->zero != NULL) {
        freeTree(node->zero);
    }
}

Name: Trevor Rex

Directory structure: The code for this submission is contained in the /src folder. The /lib and /res folders contain libraries and resource files in order for the program to run.

Explanation/description: hummanutil.cpp, HuffmanNode.cpp, and huffmanmain.cpp were provided by the Stanford computer science department. These files take care of basic file reading and bit manipulation. The code I wrote is the entirety of encoding.cpp file, where the meat of the Huffman Encoding algorithm takes place. encoding.cpp builds a frequency table of each character in a file and then creates a binary encoding tree with the leaves of the tree being a node with a character value. These values are queued into a priority queue so that they can be loaded into an encoding map where each character can be assigned an encoded value for compression. These methods can be leveraged for file compression and decompression. 

Date written: March 2016
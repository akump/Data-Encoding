
// DE6B.java CS5125/6025 Cheng 2019
// Huffman decoder
// Usage:  java DE6B < encoded > original

import java.io.*;
import java.util.*;

public class DE6B {

  int[][] codetree = null;
  int buf = 0;
  int position = 0;
  int actualNumberOfSymbols = 0;
  int filesize = 0;

  void readTree() { // read Huffman tree
    try {
      actualNumberOfSymbols = System.in.read();
      codetree = new int[actualNumberOfSymbols * 2 - 1][2];
      for (int i = 0; i < actualNumberOfSymbols * 2 - 1; i++) {
        codetree[i][0] = System.in.read();
        codetree[i][1] = System.in.read();
      }
      for (int i = 0; i < 3; i++) { // read filesize
        int a = System.in.read();
        filesize |= a << (i * 8);
      }
    } catch (IOException e) {
      System.err.println(e);
      System.exit(1);
    }
  }

  int inputBit() { // get one bit from System.in
    if (position == 0)
      try {
        buf = System.in.read();
        if (buf < 0) {
          return -1;
        }

        position = 0x80;
      } catch (IOException e) {
        System.err.println(e);
        return -1;
      }
    int t = ((buf & position) == 0) ? 0 : 1;
    position >>= 1;
    return t;
  }

  void decode() { // Your two lines of code for updating k are needed for this to work.
    int bit = -1; // next bit from compressed file: 0 or 1. no more bit: -1
    int k = 0; // index to the Huffman tree array; k = 0 is the root of tree
    int n = 0; // number of symbols decoded, stop the while loop when n == filesize
    int numBits = 0; // number of bits in the compressed file
    FileOutputStream file = null;
    try {
      file = new FileOutputStream("uncompressed.txt");
    } catch (FileNotFoundException ex) {
      System.err.println("Could not open file.");
    }
    while ((bit = inputBit()) >= 0) {
      k = codetree[k][bit];
      numBits++;
      if (codetree[k][0] == 0) {
        try {
          file.write(codetree[k][1]);
        } catch (IOException ex) {
          System.err.println("Failed to write");
        }
        System.out.write(codetree[k][1]);
        if (n++ == filesize)
          break;
        k = 0;

      }
    }
    System.out.printf("\n\n-----\n" + "%d bytes in uncompressed file.\n", filesize);
    System.out.printf("%d bytes in compressed file.\n", numBits / 8);
    System.out.printf("Compression ratio of %f.", (double) numBits / (double) filesize);
    System.out.flush();
  }

  public static void main(String[] args) {
    DE6B de6 = new DE6B();
    de6.readTree();
    de6.decode();
  }
}

// DE1C.java CS5125/6025 Cheng 2019
// Implementing AES key expansion
// Usage: java DE1C key 

import java.io.*;
import java.util.*;

public class DE1C{

  static final int numberOfBits = 8;
  static final int fieldSize = 1 << numberOfBits;
  static final int irreducible = 0x11b;
  static final int logBase = 3;
  static final byte[][] A = new byte[][] {
            {1, 1, 1, 1, 1, 0, 0, 0},
            {0, 1, 1, 1, 1, 1, 0, 0},
 	    {0, 0, 1, 1, 1, 1, 1, 0},
            {0, 0, 0, 1, 1, 1, 1, 1},
	    {1, 0, 0, 0, 1, 1, 1, 1},
            {1, 1, 0, 0, 0, 1, 1, 1},
            {1, 1, 1, 0, 0, 0, 1, 1},
            {1, 1, 1, 1, 0, 0, 0, 1}
	};
  static final byte[] B = new byte[] { 0, 1, 1, 0, 0, 0, 1, 1};  
  int[] alog = new int[fieldSize];
  int[] log = new int[fieldSize];
  int[] S = new int[fieldSize];
  static final int blockSize = 16;
  static final int numberOfRounds = 11;
  int[] state = new int[blockSize];
  int[][] roundKey = new int[numberOfRounds][blockSize];  
  String hexkey = null;

  int modMultiply(int a, int b, int m){
    int product = 0;
    for (; b > 0; b >>= 1){
      if ((b & 1) > 0) product ^= a;
      a <<= 1;
      if ((a & fieldSize) > 0) a ^= m;
    }
    return product;
  }    

  void makeLog(){
    alog[0] = 1;
    for (int i = 1; i < fieldSize; i++)
      alog[i] = modMultiply(logBase, alog[i - 1], irreducible);
    for (int i = 1; i < fieldSize; i++) log[alog[i]] = i;
  }

  int logMultiply(int a, int b){
    return (a == 0 || b == 0) ? 0 : alog[(log[a] + log[b]) % (fieldSize - 1)];
  }

  int multiplicativeInverse(int a){
    return alog[fieldSize - 1 - log[a]];
  }

  void buildS(){
     int[] bitColumn = new int[8];
     for (int i = 0; i < fieldSize; i++){
       int inverse = i < 2 ? i : multiplicativeInverse(i);
       for (int k = 0; k < 8; k++)
           bitColumn[k] = inverse >> (7 - k) & 1;
       S[i] = 0;
       for (int k = 0; k < 8; k++){
          int bit = B[k];
          for (int l = 0; l < 8; l++)
            if (bitColumn[l] == 1) bit ^= A[k][l];
          S[i] ^= bit << 7 - k;
       }
    }
  }


 void readKey(String filename){
   Scanner in = null;
   try {
     in = new Scanner(new File(filename));
   } catch (FileNotFoundException e){
     System.err.println(filename + " not found");
     System.exit(1);
   }
   hexkey = in.nextLine();
   in.close();
 }

 void expandKey(){
   for (int i = 0; i < blockSize; i++) roundKey[0][i] = 
     Integer.parseInt(hexkey.substring(i * 2, (i + 1) * 2), 16);
   int rcon = 1;
   for (int i = 1; i < numberOfRounds; i++){  
     roundKey[i][0] = S[roundKey[i-1][13]] ^ rcon;
     rcon <<= 1; if (rcon > 0xFF) rcon ^= irreducible;
     roundKey[i][1] = S[roundKey[i-1][14]];
     roundKey[i][2] = S[roundKey[i-1][15]];
     roundKey[i][3] = S[roundKey[i-1][12]];
     for (int k = 0; k < 4; k++) 
        roundKey[i][k] ^= roundKey[i-1][k];
     for (int k = 4; k < blockSize; k++) 
        roundKey[i][k] = roundKey[i][k-4] ^ roundKey[i-1][k];
     for (int k = 0; k < blockSize; k++)
        System.out.print(Integer.toString(roundKey[i][k], 16) + " ");
     System.out.println();
   }
 }

public static void main(String[] args){
   if (args.length < 1){
     System.err.println("Usage: java DE1C key");
     return;
   }
   DE1C de1 = new DE1C();
   de1.makeLog();
   de1.buildS(); 
   de1.readKey(args[0]);
   de1.expandKey();
}
}

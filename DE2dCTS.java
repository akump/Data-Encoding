// DE2dCTS.java CS5125/6025 Cheng 2019
// Implementing AES decryption with CTS mode, IV = 0
// Usage: java DE2dCTS key < DE2testCTS.de2 > original.txt

import java.io.*;
import java.util.*;

public class DE2dCTS{

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
  static final byte[][] Gi = new byte[][] {
            {14, 9, 13, 11},
            {11, 14, 9, 13},
            {13, 11, 14, 9},
            {9, 13, 11, 14}
        };
  int[] alog = new int[fieldSize];
  int[] log = new int[fieldSize];
  int[] S = new int[fieldSize];
  int[] Si = new int[fieldSize];
  static final int blockSize = 16;
  static final int numberOfRounds = 11;
  int[] state = new int[blockSize];
  int[] inBlock = new int[blockSize];
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
       Si[S[i]] = i;
    }
  }

 int readBlock(){
   byte[] data = new byte[blockSize];
   int len = 0;
   try {
     len = System.in.read(data);
   } catch (IOException e){
     System.err.println(e.getMessage());
     System.exit(1);
   }
   if (len <= 0) return len;
   for (int i = 0; i < len; i++){
     if (data[i] < 0) inBlock[i] = data[i] + fieldSize;
     else inBlock[i] = data[i];
   }
   return len;
 }

  void inverseSubBytes(){
    for (int i = 0; i < blockSize; i++) 
      state[i] = Si[state[i]];
  }

 void inverseShiftRows(){
   // Your code from DE1B
   int temp = state[2]; state[2] = state[10]; state[10] = temp;
   temp = state[6]; state[6] = state[14]; state[14] = temp;
   temp = state[1]; state[1] = state[13]; state[13] = state[9]; 
   state[9] = state[5]; state[5] = temp;
   temp = state[3]; state[3] = state[7]; state[7] = state[11];
   state[11] = state[15]; state[15] = temp;
 }

  void inverseMixColumns(){
   int[] temp = new int[4];
   for (int k = 0; k < 4; k++){
    for (int i = 0; i < 4; i++){
      temp[i] = 0;
      for (int j = 0; j < 4; j++)  
        temp[i] ^= logMultiply(Gi[j][i], state[k * 4 + j]);
    }
    for (int i = 0; i < 4; i++) state[k * 4 + i] = temp[i];
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
   }
 }

 void inverseAddRoundKey(int round){  // Your code from DE1B
   for (int k = 0; k < blockSize; k++) 
      state[k] ^= roundKey[numberOfRounds - 1 - round][k]; 
 }

  void blockDecipher(){
    inverseAddRoundKey(0);
    for (int i = 1; i < numberOfRounds; i++){
      inverseSubBytes();
      inverseShiftRows();
      inverseAddRoundKey(i);
      if (i < numberOfRounds - 1) inverseMixColumns();
    }
  }

 void writeBlock(int[] block, int len){
   byte[] data = new byte[blockSize];
   for (int i = 0; i < len; i++)
     data[i] = (byte)(block[i]);   
   System.out.write(data, 0, len);
 }

 void addBlock(int[] destination, int[] source){
   for (int k = 0; k < blockSize; k++) 
      destination[k] ^= source[k];
 }

 void copyBlock(int[] destination, int[] source){
   for (int k = 0; k < blockSize; k++) 
      destination[k] = source[k];
 }

 void decrypt(){
   int[] inBlock2 = new int[blockSize];  // need to save two cipher blocks
   for (int k = 0; k < blockSize; k++) inBlock2[k] = 0; // IV = 0
   int[] inBlock1 = new int[blockSize];
   int len = readBlock();
   copyBlock(inBlock1, inBlock);  // save inBlock
   while ((len = readBlock()) >= 0)  // a new inBlock after readBlock
     if (len == blockSize){  // not end yet
         copyBlock(state, inBlock1);  // setup state with previous cipher block
         blockDecipher();
         addBlock(state, inBlock2);  // add the second previous cipher block
         writeBlock(state, blockSize);  
           // write the plaintext block from the previous cipher block
         copyBlock(inBlock2, inBlock1);
         copyBlock(inBlock1, inBlock);  // update the saved cipher blocks
     }else{  // last block has len bytes
         // step1: copy inBlock1 to state and do blockDecipher
         // step2: save state in inBlock1 for the last plaintext block
         // step3: copy inBlock to state
         // step4: copy the last blockSize - len bytes from inBlock1 to state
         // step5: do blockDecipher
         // step6: add inBlock2 to state and this is the second-to-the-last 
         //         plaintext block and write this plaintext block out
         // step7: Now add inBlock to inBlock1 and write the first len bytes out.
     }
   System.out.flush();
 }


public static void main(String[] args){
   if (args.length < 1){
     System.err.println("Usage: java DE2dCTS key < DE2testCTS.de2 > original.txt");
     return;
   }
   DE2dCTS de2 = new DE2dCTS();
   de2.makeLog();
   de2.buildS(); 
   de2.readKey(args[0]);
   de2.expandKey();
   de2.decrypt();
}
}

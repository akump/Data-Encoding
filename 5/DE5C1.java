// DE5C1.java CS5125/6025 Cheng 2019
// RSA Key generation 
// three lines for e, n, and d. 
// Usage:  java DE5C1 > rsaKeys.txt
// e, n are the public key and d is the private one

import java.lang.*;
import java.util.*;
import java.math.*;

public class DE5C1{

   Random rand = new Random();
   BigInteger p = new BigInteger(1024, 200, rand);
   BigInteger q = new BigInteger(1024, 200, rand);
   BigInteger n = p.multiply(q);
   BigInteger phi = p.subtract(BigInteger.ONE).multiply(
                  q.subtract(BigInteger.ONE));
   BigInteger e = new BigInteger("65537");
   BigInteger d = e.modInverse(phi);

   void printKey(){
     System.out.println(e.toString(16));
     System.out.println(n.toString(16));
     System.out.println(d.toString(16));
   }

 public static void main(String[] args){
   DE5C1 de5 = new DE5C1();
   de5.printKey();
 }
}

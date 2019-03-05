
// DE5A.java CS5125/6025 Cheng 2019
// DSA signing and verifying
// Usage: java DE5A

import java.io.*;
import java.util.*;
import java.math.*;

public class DE5A {
  BigInteger q = null;
  BigInteger privateKey;
  BigInteger publicKey;
  Random random = new Random();
  BigInteger message;

  void readQ(String filename) {
    Scanner in = null;
    try {
      in = new Scanner(new File(filename));
    } catch (FileNotFoundException e) {
      System.err.println(filename + " not found");
      System.exit(1);
    }
    String hexQ = in.nextLine();
    in.close();
    q = new BigInteger(hexQ, 16);
  }

  void generateKeyPair() {
    Random random = new Random();
    privateKey = new BigInteger(1235, random);
    BigInteger temp = new BigInteger("2");
    publicKey = temp.modPow(privateKey, q);
    message = new BigInteger(1235, random);
    System.out.println("Message " + message.toString(16));
  }

  void DSA() { // Figure 13.4 of book assuming m = H(M)
    Random random = new Random();
    BigInteger one = new BigInteger("1");
    BigInteger two = new BigInteger("2");
    BigInteger p = q.subtract(one).divide(two); // Let us use DHGroup5 again. p = (q-1)/2 is prime.
    BigInteger h = two;
    BigInteger g = h.modPow(p.subtract(one).divide(q), p); // h = 2 then g = 4
    BigInteger m = new BigInteger(1235, random).mod(q); // message < p
    BigInteger x = new BigInteger(1235, random).mod(q.subtract(one)).add(one); // private key < p
    BigInteger y = g.modPow(x, p); // public key y = g power x mod q
    BigInteger k = new BigInteger(1235, random).mod(q.subtract(one)).add(one); // per-message secret number k < p
    BigInteger r = g.modPow(k, p).mod(q); // r = g power k mod q mod p
    BigInteger s = k.modInverse(q).multiply(m.add(x.multiply(r))).mod(q); // s = modinverse of k times (m + xr) mod p
    // (r,s) is the signature for m
    BigInteger w = s.modInverse(q); // w = inverse of s mod p
    BigInteger u1 = m.multiply(w).mod(q); // u1 = mw mod p
    BigInteger u2 = r.multiply(w).mod(q); // u2 = rw mod p
    BigInteger v = g.modPow(u1, p).multiply(y.modPow(u2, p)).mod(p).mod(q); // v = g power u1 times y power u2 mod q mod
    // Is v the same as r?
    System.out.println("r = " + r.toString());
    System.out.println("v = " + v.toString());

    if (v.equals(r)) {
      System.out.println("V equals R");
    } else {
      System.out.println("V does not equal R");
    }
  }

  public static void main(String[] args) {
    DE5A de5 = new DE5A();
    de5.readQ("DHgroup5.txt");
    de5.generateKeyPair();
    de5.DSA();
  }
}

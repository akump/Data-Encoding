
// DE5B.java CS5125/6025 Cheng 2019
// Elliptic curve digital signature algorithm
// Usage: java DE5B ECP256.txt

import java.math.*;
import java.io.*;
import java.util.*;

class Point {
  public BigInteger x;
  public BigInteger y;
  static Point O = new Point(null, null);

  public Point(BigInteger xx, BigInteger yy) {
    x = xx;
    y = yy;
  }

  public String toString() {
    return this.equals(O) ? "O" : "(" + x.toString(16) + ",\n" + y.toString(16) + ")";
  }
}

public class DE5B {

  static BigInteger three = new BigInteger("3");
  static final int privateKeySize = 255;
  BigInteger p; // modulus
  Point G; // base point
  BigInteger a; // curve parameter
  BigInteger b; // curve parameter
  BigInteger n; // order of G
  BigInteger privateKey;
  Point publicKey;
  Random random = new Random();

  void readCurveSpecs(String filename) {
    Scanner in = null;
    try {
      in = new Scanner(new File(filename));
    } catch (FileNotFoundException e) {
      System.err.println(filename + " not found");
      System.exit(1);
    }
    p = new BigInteger(in.nextLine(), 16);
    n = new BigInteger(in.nextLine(), 16);
    a = new BigInteger(in.nextLine(), 16);
    b = new BigInteger(in.nextLine(), 16);
    G = new Point(new BigInteger(in.nextLine(), 16), new BigInteger(in.nextLine(), 16));
    in.close();
  }

  Point add(Point P1, Point P2) {
    if (P1.equals(Point.O))
      return P2;
    if (P2.equals(Point.O))
      return P1;
    if (P1.x.equals(P2.x))
      if (P1.y.equals(P2.y))
        return selfAdd(P1);
      else
        return Point.O;
    BigInteger t1 = P1.x.subtract(P2.x).mod(p);
    BigInteger t2 = P1.y.subtract(P2.y).mod(p);
    BigInteger k = t2.multiply(t1.modInverse(p)).mod(p); // slope
    t1 = k.multiply(k).subtract(P1.x).subtract(P2.x).mod(p); // x3
    t2 = P1.x.subtract(t1).multiply(k).subtract(P1.y).mod(p); // y3
    return new Point(t1, t2);
  }

  Point selfAdd(Point P) {
    if (P.equals(Point.O))
      return Point.O; // O+O=O
    BigInteger t1 = P.y.add(P.y).mod(p); // 2y
    BigInteger t2 = P.x.multiply(P.x).mod(p).multiply(three).add(a).mod(p); // 3xx+a
    BigInteger k = t2.multiply(t1.modInverse(p)).mod(p); // slope or tangent
    t1 = k.multiply(k).subtract(P.x).subtract(P.x).mod(p); // x3 = kk-x-x
    t2 = P.x.subtract(t1).multiply(k).subtract(P.y).mod(p); // y3 = k(x-x3)-y
    return new Point(t1, t2);
  }

  Point multiply(Point P, BigInteger n) {
    if (n.equals(BigInteger.ZERO))
      return Point.O;
    int len = n.bitLength(); // position preceding the most significant bit 1
    Point product = P;
    for (int i = len - 2; i >= 0; i--) {
      product = selfAdd(product);
      if (n.testBit(i))
        product = add(product, P);
    }
    return product;
  }

  void generateKeys() {
    privateKey = new BigInteger(privateKeySize, random);
    publicKey = multiply(G, privateKey);
  }

  void ECDSA() { // 13.5 of book
    BigInteger message = new BigInteger(160, random);  // this is e = H(m) in book
    System.out.println("message: " + message.toString(16));
    BigInteger k = new BigInteger(privateKeySize, random);

    Point kG = multiply(G, k); // k times G
    BigInteger r = kG.x.mod(n); // r = kG.x mod n
    // s = k's modInverse times (message plus privateKeyB times r) mod n
    BigInteger s = k.modInverse(n).multiply(privateKey.multiply(r).add(message)).mod(n);
    // if r or s == 0 redo k
    // (r,s) is the signature for message (digest)
    if (r.equals(Point.O) || s.equals(Point.O)) {ECDSA();}
    System.out.println("r: " + r.toString(16));
    System.out.println("s: " + s.toString(16));

    BigInteger w = s.modInverse(n); // w is s's multiplicative inverse mod n
    BigInteger u1 = w.multiply(message); // u1 = message times w
    BigInteger u2 = w.multiply(r); // u2 = r times w

    // X is u1 times G + u2 times publicKeyB
    Point X = add(multiply(publicKey, u2), multiply(G, u1));
    BigInteger v = X.x.mod(n);
      if (X.equals(Point.O))
      System.out.println("Rejected");// reject the signature
    else if (X.x.mod(n).equals(r))
      System.out.println("Accepted");// accept the signature
    else
      System.out.println("Rejected");// reject the signature
  }

  public static void main(String[] args) {
    DE5B de5 = new DE5B();
    de5.readCurveSpecs(args[0]);
    de5.generateKeys();
    de5.ECDSA();
  }
}

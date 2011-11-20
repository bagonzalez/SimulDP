package org.opensourcephysics.numerics.specialfunctions;

/**
 * A Key for objects, such as quantum wave functions, that can be identified by integers.
 * 
 * @author Wolfgang Christian
 */
public class QNKey {
    int n, k;

    protected QNKey(int n, int k) {
      this.n = n;
      this.k = k;
    }

    public boolean equals(Object key) {
      if(key==null)return false;
      return (((QNKey)key).n==n)&&(((QNKey)key).k==k);
    }

    public int hashCode() {
      return 1031*n+k;
    }
}

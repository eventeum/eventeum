package net.consensys.eventeum.utils;

import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicReference;

public final class AtomicBigInteger {

  private final AtomicReference<BigInteger> valueHolder = new AtomicReference<>();

  public AtomicBigInteger(BigInteger bigInteger) {
    valueHolder.set(bigInteger);
  }

  public BigInteger get() {
    return valueHolder.get();
  }

  public void increment() {
    for (; ; ) {
      BigInteger current = valueHolder.get();
      BigInteger next = current.add(BigInteger.ONE);
      if (valueHolder.compareAndSet(current, next)) {
        return;
      }
    }
  }
}

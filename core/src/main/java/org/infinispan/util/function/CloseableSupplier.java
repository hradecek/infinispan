package org.infinispan.util.function;

import java.util.function.Supplier;

public interface CloseableSupplier<T> extends Supplier<T>, AutoCloseable {
   @Override
   default void close() {
      // Does nothing by default
   }
}

package java11;

import gov.nasa.jpf.util.test.TestJPF;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;


public class AccumulatorTest extends TestJPF {
  @Test
  public void accumulatorTest() {
    AtomicInteger acc = new AtomicInteger(0);
    IntStream.range(0, 10).forEach(x -> acc.accumulateAndGet(x, (n, m) -> n + m));
    System.out.println("Result: " + acc);
  }
}

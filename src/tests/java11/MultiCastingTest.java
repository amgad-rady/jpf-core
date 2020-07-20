package java11;

import gov.nasa.jpf.util.test.TestJPF;
import org.junit.Test;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.ToIntFunction;

import static java.util.Comparator.comparingInt;
import static java.util.Map.Entry.comparingByValue;

public class MultiCastingTest extends TestJPF {
//  public interface X {}
//
//  public interface Y extends X {
//    X lambda();
//  }
//
//  public class A implements Y {
//    @Override
//    public String toString() {
//      return "This is an A";
//    }
//
//    @Override
//    public A lambda() {
//      return this;
//    }
//  }
//
//  public A lambdaFunction() {
//    A a = new A();
//    return (A & X)
//      () -> a;
//  }
//
//  @Test
//  public void testCasting() {
//    A a = lambdaFunction();
//    System.out.println(a);
//  }


//  public static <K, V extends Comparable<? super V>> Comparator<Map.Entry<K, V>> comparingByValue() {
//    return (Comparator<Map.Entry<K, V>> & Serializable)
//      (c_1, c_2) -> c_1.getValue().compareTo(c_2.getValue());
//  }

//  public static <T> Comparator<T> comparingInt(ToIntFunction<? super T> keyExtractor) {
//    return (Comparator<T>)
//      (c1, c2) -> Integer.compare(keyExtractor.applyAsInt(c1), keyExtractor.applyAsInt(c2));
//  }

  @Test
  public void testMapEntryMethod() {
    if (verifyNoPropertyViolation()) {
      Map<Integer, Integer> map = new HashMap<>();
      map.put(0, 1);
      map.put(1, 2);
      Iterator<Map.Entry<Integer, Integer>> entries = map.entrySet().iterator();
      Map.Entry<Integer, Integer> first = entries.next();
      Map.Entry<Integer, Integer> second = entries.next();
      Comparator<Map.Entry<Integer, Integer>> lambda = comparingByValue();
      int result = lambda.compare(first, second);
    }
  }

  @Test
  public void testComparingInt() {
    if (verifyNoPropertyViolation()) {
      ToIntFunction<Integer> keyExtractor = (x) -> 0;
      Comparator<Integer> comparator = comparingInt(keyExtractor);
      int result = comparator.compare(0, 1);
    }
  }
}

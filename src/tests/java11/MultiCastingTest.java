package java11;

import gov.nasa.jpf.util.test.TestJPF;
import org.junit.Test;

import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MultiCastingTest extends TestJPF {
  public interface Area {
    double area();
  }

  public interface Circumference {
    double circumference();
  }

  public interface Dimension {
    int dimension();
  }

  public class UnitSquare implements Area, Circumference, Dimension {
    @Override
    public double area() {
      return 1.0;
    }

    @Override
    public double circumference() {
      return 4.0;
    }

    @Override
    public int dimension() {
      return 2;
    }
  }

  @Test
  public void testShapeCast() {
    Object sq = new UnitSquare();
    sq = (Area & Circumference & Dimension) sq;
  }

  public static <K, V extends Comparable<? super V>> Comparator<Map.Entry<K, V>> comparingByValue() {
    return
      (c_1, c_2) -> c_1.getValue().compareTo(c_2.getValue());
  }

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
}

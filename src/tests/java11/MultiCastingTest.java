package java11;

import gov.nasa.jpf.util.test.TestJPF;
import org.junit.Test;

import java.util.Comparator;
import java.util.Map;

public class MultiCastingTest extends TestJPF {
  public interface Area {
    double area();
  }

  public interface Circumference {
    double circumference();
  }

  public class UnitSquare implements Area, Circumference {
    public double area() {
      return 1.0;
    }

    public double circumference() {
      return 4.0;
    }
  }

  @Test
  public void testShapeCast() {
    Object sq = new UnitSquare();
    sq = (Area & Circumference) sq;
  }

  @Test
  public void testMapEntryMethod() {
    if (verifyNoPropertyViolation()) {
      Comparator<Map.Entry<Object, Integer>> lambda = Map.Entry.comparingByValue();
    }
  }
}

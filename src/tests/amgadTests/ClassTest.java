package amgadTests;

import gov.nasa.jpf.util.test.TestJPF;
import org.junit.Test;

import java.lang.reflect.Field;

public class ClassTest extends TestJPF {
  @Test
  public void getResourceTest() {
    if (verifyNoPropertyViolation()){
      Class c = ClassLoader.class;
      for (Field f : c.getDeclaredFields()) {
        System.err.println();
        System.err.println("Field name: " + f.getName());
        System.err.println("Field type: " + f.getType());
        System.err.println();
      }
      assertNotNull(c.getResource("Class.class"));
//      assertNotNull(c.getResource("/java/lang/Class.class"));
//      assertNull(c.getResource("java/lang/Class.class"));
//      assertEquals(c.getResource("Class.class"),c.getResource("/java/lang/Class.class"));
//      assertNull(c.getResource("not_existing_resources"));
    }
  }
}

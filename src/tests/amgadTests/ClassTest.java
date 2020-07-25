package amgadTests;

import gov.nasa.jpf.util.test.TestJPF;
import org.junit.Test;

public class ClassTest extends TestJPF {

  static class InAccessible {
    private InAccessible() {}
  }

  @Test
  public void testNewInstanceFailAccess () throws ReflectiveOperationException {
    if (verifyUnhandledException("java.lang.IllegalAccessException")){
      Class<?> clazz = amgadTests.ClassTest.InAccessible.class;
      clazz.getDeclaredConstructor().newInstance();
    }
  }

  @Test
  public void getResourceTest() {
    if (verifyNoPropertyViolation()){
      Class c = ClassLoader.class;
      assertNotNull(c.getResource("Class.class"));
      assertNotNull(c.getResource("/java/lang/Class.class"));
      assertNull(c.getResource("java/lang/Class.class"));
      assertEquals(c.getResource("Class.class"),c.getResource("/java/lang/Class.class"));
      assertNull(c.getResource("not_existing_resources"));
    }
  }
}

package dk.brightworks.webmethods;

import dk.brightworks.autowirer.Autowirer;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class WebMethodMapTest {
    @WebService("/ws1")
    public static class Ws1 {
        @WebMethod
        public void m1() {

        }

        @WebMethod
        public void m2() {

        }
    }

    @Test
    public void sunshine() {
        Autowirer autowirer = new Autowirer();
        autowirer.add(new WebMethodMap(), new Ws1());
        autowirer.init();
        assertEquals("dk.brightworks.webmethods.WebMethodMapTest$Ws1::m1", "" + autowirer.lookupInstance(WebMethodMap.class).findInvocation("/ws1/m1"));
        assertEquals("dk.brightworks.webmethods.WebMethodMapTest$Ws1::m2", "" + autowirer.lookupInstance(WebMethodMap.class).findInvocation("/ws1/m2"));
        assertEquals("null", "" + autowirer.lookupInstance(WebMethodMap.class).findInvocation("/wsx/m2"));
    }
}

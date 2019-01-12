package dk.brightworks.webmethods;

import dk.brightworks.autowirer.wire.Autowired;
import dk.brightworks.autowirer.Autowirer;
import dk.brightworks.autowirer.wire.Init;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class WebMethodMap {
    public static class Reference {
        private Method method;
        private Object object;

        public Reference(Object object, Method method) {
            this.object = object;
            this.method = method;
        }

        public Method getMethod() {
            return method;
        }

        public Object getObject() {
            return object;
        }

        public String toString() {
            return object.getClass().getName() + "::" + method.getName();
        }

    }

    private Map<String, Reference> map = new HashMap<>();

    @Autowired
    Autowirer autowirer;

    @Init
    public void init() {
        for (Object object : autowirer.findServicesWithAnnotation(WebService.class)) {
            String path = object.getClass().getAnnotation(WebService.class).value();
            if (!path.startsWith("/")) path = "/" + path;
            for (Method method : object.getClass().getMethods()) {
                if (method.isAnnotationPresent(WebMethod.class)) {
                    map.put(path + "/" + method.getName(), new Reference(object,method));
                }
            }
        }
    }

    public Reference findInvocation(String path) {
        return map.get(path);
    }

    public Map<String, Reference> getMap() {
        return map;
    }
}

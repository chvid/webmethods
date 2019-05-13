package dk.brightworks.webmethods;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dk.brightworks.autowirer.wire.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class WebMethodServletUtils {
    public static class ErrorResult {
        private String message;
        private String type;

        public ErrorResult(Throwable t) {
            message = t.getMessage();
            type = t.getClass().getName();
        }
    }

    private static Gson gson = new GsonBuilder().setLenient().serializeSpecialFloatingPointValues().create();

    public static void outputObjectResult(HttpServletResponse response, Object result) throws IOException {
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(response.getOutputStream()));
        response.setStatus(200);
        response.setContentType("application/json");
        gson.toJson(result, bw);
        bw.close();
    }

    public static void outputErrorResult(HttpServletResponse response, Throwable t) throws IOException {
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(response.getOutputStream()));
        if (t instanceof HasStatusCode) {
            response.setStatus(((HasStatusCode) t).getStatusCode());
        } else {
            response.setStatus(500);
        }
        response.setContentType("application/json");
        gson.toJson(new ErrorResult(t), bw);
        bw.close();
    }

    private static boolean hasAutowired(Annotation[] as) {
        for (Annotation a : as) {
            if (a.annotationType().equals(Autowired.class)) {
                return true;
            }
        }
        return false;
    }

    public static Object findRequestObject(Method method, Reader reader) {
        int countWithAutowired = 0;
        int nonAutowiredIndex = -1;

        for (int i = 0; i < method.getParameterCount(); i++) {
            if (hasAutowired(method.getParameterAnnotations()[i])) {
                countWithAutowired++;
            } else {
                nonAutowiredIndex = i;
            }
        }

        if (countWithAutowired == method.getParameterCount()) {
            return null;
        } else if (countWithAutowired == method.getParameterCount() - 1) {
            return gson.fromJson(reader, method.getParameterTypes()[nonAutowiredIndex]);
        } else {
            throw new RuntimeException("No suitable signature on " + method);
        }
    }
}

package dk.brightworks.webmethods;

import dk.brightworks.autowirer.Autowirer;
import dk.brightworks.autowirer.invocation.MethodInvocation;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static dk.brightworks.webmethods.WebMethodServletUtils.*;

public class WebMethodServlet extends HttpServlet {
    private static Logger logger = Logger.getLogger(WebMethodServlet.class.getName());

    private static Autowirer autowirer;

    public static Autowirer getAutowirer() {
        return autowirer;
    }

    public synchronized void init() {
        logger.info("Initializing services ...");
        long start = System.currentTimeMillis();

        String packageToScan = getInitParameter("packageToScan");

        if (packageToScan == null) {
            logger.warning("Init parameter scanPath of WebMethodServlet has not been set. This causes slow startup.");
            packageToScan = "";
        }

        long start2 = System.currentTimeMillis();

        autowirer = new Autowirer();

        autowirer.add(new WebMethodMap());
        autowirer.addPackage(packageToScan);

        logger.info("Component scan took " + (System.currentTimeMillis() - start2) + " msec.");

        autowirer.init();

        logger.info("All service has been initialized. (" + (System.currentTimeMillis() - start) + " msec.)");
    }

    public synchronized void destroy() {
        logger.info("Calling shutdown on all services ...");
        long start = System.currentTimeMillis();
        autowirer.shutdown();
        autowirer = null;
        logger.info("All services have been shut down. (" + (System.currentTimeMillis() - start) + " msec.)");
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        WebMethodMap.Reference webMethodReference = autowirer.lookupInstance(WebMethodMap.class).findInvocation(request.getPathInfo());

        if (webMethodReference == null) {
            logger.info(request.getPathInfo() + " not found");
            response.setStatus(404);
        } else {
            Object requestObject = findRequestObject(webMethodReference.getMethod(), request.getReader());

            MethodInvocation invocation =
                    webMethodReference.getMethod().getParameterTypes().length == 0 ?
                            new MethodInvocation(webMethodReference.getMethod(), webMethodReference.getObject()) :
                            new MethodInvocation(webMethodReference.getMethod(), webMethodReference.getObject(), requestObject);

            invocation.addWiredObject(request);
            invocation.addWiredObject(response);

            autowirer.invoke(invocation);

            if (invocation.getResultException() == null) {
                outputObjectResult(response, invocation.getResultObject());
            } else {
                logger.log(Level.INFO, "Returning exception " + invocation.getResultException(), invocation.getResultException());
                outputErrorResult(response, invocation.getResultException());
            }
        }
    }
}

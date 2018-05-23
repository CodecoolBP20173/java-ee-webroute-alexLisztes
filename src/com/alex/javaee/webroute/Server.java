package com.alex.javaee.webroute;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.Arrays;

import com.alex.javaee.annotations.PathParam;
import com.alex.javaee.annotations.WebRoute;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class Server {

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/", new MyHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String requestPath = t.getRequestURI().getPath();
            String response = "";
            Method[] methods = this.getClass().getMethods();

            for(Method m : methods) {
                Annotation[] annotations = m.getDeclaredAnnotations();

                for (Annotation a : annotations) {

                    if (a instanceof WebRoute) {
                        WebRoute annotation = (WebRoute) a;

                        String compareWith = "/" + requestPath.split("/")[1];


                        if (annotation.path().startsWith(compareWith)) {

                            try {
                                response = (String) m.invoke(this, t);
                                break;
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                e.printStackTrace();
                                response = "There was an error:\n" + e.getMessage();
                            }
                        }
                    }
                }
            }

            if (response.equals("")) {
                response = "There was an error!";
                t.sendResponseHeaders(404, response.getBytes().length);
            } else {
                t.sendResponseHeaders(200, response.getBytes().length);
            }
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

        @WebRoute(path="/test")
        public String inTest(HttpExchange t) {
            return "Showing the test page!";
        }

        @WebRoute(path="/user/<username>")
        public String inUser(HttpExchange t, @PathParam(parameter = "username") String username) {
            return "Username: " + username;
        }
    }
}

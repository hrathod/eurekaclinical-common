package org.eurekaclinical.common.servlet;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import javax.inject.Inject;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.inject.Singleton;
import org.eurekaclinical.standardapis.props.CasEurekaClinicalProperties;


@Singleton
public class PostMessageLoginServlet extends HttpServlet {

    private final CasEurekaClinicalProperties properties;

    @Inject
    public PostMessageLoginServlet(CasEurekaClinicalProperties inProperties) {
        this.properties = inProperties;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try (PrintWriter out = new PrintWriter(new OutputStreamWriter(resp.getOutputStream()))) {
            out.println("<html><body><script type=\"text/javascript\">");
            out.println("window.postMessage(200, '" + this.properties.getUrl() + "');");
            out.println("</script>");
        }
    }
}

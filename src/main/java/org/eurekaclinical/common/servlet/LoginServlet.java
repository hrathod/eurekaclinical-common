package org.eurekaclinical.common.servlet;

import javax.inject.Inject;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.inject.Singleton;


@Singleton
public class LoginServlet extends HttpServlet {

    @Inject
    public LoginServlet() {
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
    }
}

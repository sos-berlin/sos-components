package com.sos.joc2.poc.jetty.servlets;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class BlockingServletExample extends HttpServlet {

    private static final long serialVersionUID = 1131387267005476044L;

    protected void doGet(
      HttpServletRequest request, 
      HttpServletResponse response)
      throws ServletException, IOException {
 
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println("{ \"status\": \" 🙉 Jetty 🙉  läuft! 🙊 \"   }");
    }
    
}
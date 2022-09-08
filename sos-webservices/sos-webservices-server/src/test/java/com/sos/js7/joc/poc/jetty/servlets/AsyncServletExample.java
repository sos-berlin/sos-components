package com.sos.js7.joc.poc.jetty.servlets;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AsyncServletExample extends HttpServlet {

    private static final long serialVersionUID = 4011618121469690668L;
    private static String HEAVY_RESOURCE = "This is some heavy resource that will be served in an async way";

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

      ByteBuffer content = ByteBuffer.wrap(HEAVY_RESOURCE.getBytes(StandardCharsets.UTF_8));

      AsyncContext async = request.startAsync();
      ServletOutputStream out = response.getOutputStream();
      out.setWriteListener(new WriteListener() {
          @Override
          public void onWritePossible() throws IOException {
              while (out.isReady()) {
                  if (!content.hasRemaining()) {
                      response.setStatus(200);
                      async.complete();
                      return;
                  }
                  out.write(content.get());
              }
          }

          @Override
          public void onError(Throwable t) {
              getServletContext().log("Async Error", t);
              async.complete();
          }
      });
  }

}
package com.sample;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.asynchttpclient.AsyncHandler;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.HttpResponseBodyPart;
import org.asynchttpclient.HttpResponseHeaders;
import org.asynchttpclient.HttpResponseStatus;

import io.netty.handler.codec.http.HttpHeaders;


/**
 * @author wangto
 *
 */
public class App {

  public static AsyncHttpClient asyncHttpClient() {
    return new DefaultAsyncHttpClient(
        new DefaultAsyncHttpClientConfig.Builder().setConnectTimeout(5000).setRequestTimeout(5000)
            .setThreadPoolName("asyncHttpClient").build());
  }

  public static void main(String[] args) throws InterruptedException, ExecutionException {
    AsyncHttpClient async = asyncHttpClient();
    Future<String> f =
        async.prepareGet("http://www.example.com/").execute(new AsyncHandler<String>() {
          private ByteArrayOutputStream bytes = new ByteArrayOutputStream();

          @Override
          public State onStatusReceived(HttpResponseStatus status) throws Exception {
            int statusCode = status.getStatusCode();
            // The Status have been read
            // If you don't want to read the headers,body or stop processing the response
            if (statusCode >= 500) {
              return State.ABORT;
            }

            return null;
          }

          @Override
          public State onHeadersReceived(HttpResponseHeaders h) throws Exception {
            HttpHeaders headers = h.getHeaders();
            // The headers have been read
            // If you don't want to read the body, or stop processing the response
            return State.ABORT;
          }

          @Override
          public State onBodyPartReceived(HttpResponseBodyPart bodyPart) throws Exception {
            bytes.write(bodyPart.getBodyPartBytes());
            return State.CONTINUE;
          }

          @Override
          public String onCompleted() throws Exception {
            // Will be invoked once the response has been fully read or a ResponseComplete exception
            // has been thrown.
            // NOTE: should probably use Content-Encoding from headers
            return bytes.toString("UTF-8");
          }

          @Override
          public void onThrowable(Throwable t) {}
        });
    String bodyResponse = f.get();
    System.out.println(bodyResponse);
  }


}

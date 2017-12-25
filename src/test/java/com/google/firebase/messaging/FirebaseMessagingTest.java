package com.google.firebase.messaging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.TestOnlyImplFirebaseTrampolines;
import com.google.firebase.auth.MockGoogleCredentials;
import com.google.firebase.testing.TestResponseInterceptor;
import java.io.ByteArrayOutputStream;
import org.junit.After;
import org.junit.Test;

public class FirebaseMessagingTest {

  @After
  public void tearDown() {
    TestOnlyImplFirebaseTrampolines.clearInstancesForTest();
  }

  @Test
  public void testNoProjectId() {
    FirebaseOptions options = new FirebaseOptions.Builder()
        .setCredentials(new MockGoogleCredentials("test-token"))
        .build();
    FirebaseApp.initializeApp(options);
    try {
      FirebaseMessaging.getInstance();
      fail("No error thrown for missing project ID");
    } catch (IllegalArgumentException expected) {
      // expected
    }
  }

  @Test
  public void testNullMessage() throws Exception {
    FirebaseOptions options = new FirebaseOptions.Builder()
        .setCredentials(new MockGoogleCredentials("test-token"))
        .setProjectId("test-project")
        .build();
    FirebaseApp.initializeApp(options);

    FirebaseMessaging messaging = FirebaseMessaging.getInstance();
    TestResponseInterceptor interceptor = new TestResponseInterceptor();
    messaging.setInterceptor(interceptor);
    try {
      messaging.sendAsync(null);
      fail("No error thrown for null message");
    } catch (NullPointerException expected) {
      // expected
    }

    assertNull(interceptor.getResponse());
  }

  @Test
  public void testSend() throws Exception {
    MockLowLevelHttpResponse response = new MockLowLevelHttpResponse()
        .setContent("{\"name\": \"mock-name\"}");
    MockHttpTransport transport = new MockHttpTransport.Builder()
        .setLowLevelHttpResponse(response)
        .build();
    FirebaseOptions options = new FirebaseOptions.Builder()
        .setCredentials(new MockGoogleCredentials("test-token"))
        .setProjectId("test-project")
        .setHttpTransport(transport)
        .build();
    FirebaseApp app = FirebaseApp.initializeApp(options);

    FirebaseMessaging messaging = FirebaseMessaging.getInstance();
    assertSame(messaging, FirebaseMessaging.getInstance(app));

    TestResponseInterceptor interceptor = new TestResponseInterceptor();
    messaging.setInterceptor(interceptor);
    String resp = messaging.sendAsync(Message.builder().setTopic("test-topic").build()).get();
    assertEquals("mock-name", resp);

    assertNotNull(interceptor.getResponse());
    HttpRequest request = interceptor.getResponse().getRequest();
    assertEquals("POST", request.getRequestMethod());
    String url = "https://fcm.googleapis.com/v1/projects/test-project/messages:send";
    assertEquals(url, request.getUrl().toString());
    assertEquals("Bearer test-token", request.getHeaders().getAuthorization());

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    request.getContent().writeTo(out);
    assertEquals("{\"message\":{\"topic\":\"test-topic\"}}", out.toString());
  }
}
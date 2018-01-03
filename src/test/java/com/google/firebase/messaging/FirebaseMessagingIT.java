package com.google.firebase.messaging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.firebase.testing.IntegrationTestUtils;
import org.junit.BeforeClass;
import org.junit.Test;

public class FirebaseMessagingIT {

  private static final String TEST_REGISTRATION_TOKEN =
      "fGw0qy4TGgk:APA91bGtWGjuhp4WRhHXgbabIYp1jxEKI08ofj_v1bKhWAGJQ4e3arRCWzeTfHaLz83mBnDh0a"
          + "PWB1AykXAVUUGl2h1wT4XI6XazWpvY7RBUSYfoxtqSWGIm2nvWh2BOP1YG501SsRoE";

  @BeforeClass
  public static void setUpClass() throws Exception {
    IntegrationTestUtils.ensureDefaultApp();
  }

  @Test
  public void testSend() throws Exception {
    FirebaseMessaging messaging = FirebaseMessaging.getInstance();
    Message message = Message.builder()
        .setNotification(new Notification("Title", "Body"))
        .setAndroidConfig(AndroidConfig.builder()
            .setRestrictedPackageName("com.demoapps.hkj")
            .build())
        .setWebpushConfig(WebpushConfig.builder().putHeader("X-Custom-Val", "Foo").build())
        .setTopic("foo-bar")
        .build();
    String id = messaging.sendAsync(message, true).get();
    assertTrue(!Strings.isNullOrEmpty(id));
  }

  @Test
  public void testSubscribe() throws Exception {
    FirebaseMessaging messaging = FirebaseMessaging.getInstance();
    TopicManagementResponse results = messaging.subscribeToTopicAsync(
        ImmutableList.of(TEST_REGISTRATION_TOKEN), "mock-topic").get();
    assertEquals(1, results.getSuccessCount() + results.getFailureCount());
  }

  @Test
  public void testUnsubscribe() throws Exception {
    FirebaseMessaging messaging = FirebaseMessaging.getInstance();
    TopicManagementResponse results = messaging.unsubscribeFromTopicAsync(
        ImmutableList.of(TEST_REGISTRATION_TOKEN), "mock-topic").get();
    assertEquals(1, results.getSuccessCount() + results.getFailureCount());
  }
}
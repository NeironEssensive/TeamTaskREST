package com.example.team_task.repository;

import com.example.team_task.entity.Notification;
import com.example.team_task.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class NotificationRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private NotificationRepository notificationRepository;

    private User testUser;
    private User otherUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setName("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setRole(User.Role.USER);

        otherUser = new User();
        otherUser.setName("otheruser");
        otherUser.setEmail("other@example.com");
        otherUser.setPassword("password");
        otherUser.setRole(User.Role.USER);

        entityManager.persistAndFlush(testUser);
        entityManager.persistAndFlush(otherUser);
    }

    @Test
    void findByUserIdOrderByCreatedAtDesc_UserHasNotifications_ReturnsOrderedList() {
        Notification notif1 = createNotification("notif-1", testUser, "First notification");
        Notification notif2 = createNotification("notif-2", testUser, "Second notification");

        entityManager.persistAndFlush(notif1);
        entityManager.persistAndFlush(notif2);

        List<Notification> notifications = notificationRepository
                .findByUserIdOrderByCreatedAtDesc(testUser.getId());

        assertThat(notifications).hasSize(2);
        assertThat(notifications.get(0).getMessage()).isEqualTo("Second notification");
        assertThat(notifications.get(1).getMessage()).isEqualTo("First notification");
    }

    @Test
    void findByUserIdOrderByCreatedAtDesc_UserHasNoNotifications_ReturnsEmptyList() {
        List<Notification> notifications = notificationRepository
                .findByUserIdOrderByCreatedAtDesc(testUser.getId());

        assertThat(notifications).isEmpty();
    }

    @Test
    void findByUserIdOrderByCreatedAtDesc_OnlyReturnsUserNotifications() {
        Notification userNotif = createNotification("notif-1", testUser, "User notification");
        Notification otherNotif = createNotification("notif-2", otherUser, "Other notification");

        entityManager.persistAndFlush(userNotif);
        entityManager.persistAndFlush(otherNotif);

        List<Notification> notifications = notificationRepository
                .findByUserIdOrderByCreatedAtDesc(testUser.getId());

        assertThat(notifications).hasSize(1);
        assertThat(notifications.get(0).getUser().getId()).isEqualTo(testUser.getId());
    }

    @Test
    void countByUserIdAndIsReadFalse_UserHasUnreadNotifications_ReturnsCorrectCount() {
        Notification unread1 = createNotification("notif-1", testUser, "Unread 1");
        Notification unread2 = createNotification("notif-2", testUser, "Unread 2");
        Notification readNotif = createNotification("notif-3", testUser, "Read notification");
        readNotif.setRead(true);

        entityManager.persistAndFlush(unread1);
        entityManager.persistAndFlush(unread2);
        entityManager.persistAndFlush(readNotif);

        long count = notificationRepository.countByUserIdAndIsReadFalse(testUser.getId());

        assertThat(count).isEqualTo(2);
    }

    @Test
    void countByUserIdAndIsReadFalse_NoUnreadNotifications_ReturnsZero() {
        Notification readNotif = createNotification("notif-1", testUser, "Read notification");
        readNotif.setRead(true);

        entityManager.persistAndFlush(readNotif);

        long count = notificationRepository.countByUserIdAndIsReadFalse(testUser.getId());

        assertThat(count).isEqualTo(0);
    }

    @Test
    void findByNotificationId_ExistingNotification_ReturnsNotification() {
        Notification notif = createNotification("notif-uuid-abc", testUser, "Test notification");
        entityManager.persistAndFlush(notif);

        Optional<Notification> result = notificationRepository
                .findByNotificationId("notif-uuid-abc");

        assertThat(result).isPresent();
        assertThat(result.get().getNotificationId()).isEqualTo("notif-uuid-abc");
        assertThat(result.get().getMessage()).isEqualTo("Test notification");
    }

    @Test
    void findByNotificationId_NonExistentNotification_ReturnsEmpty() {
        Optional<Notification> result = notificationRepository
                .findByNotificationId("nonexistent-id");

        assertThat(result).isEmpty();
    }

    private Notification createNotification(String notificationId, User user, String message) {
        Notification notification = new Notification();
        notification.setNotificationId(notificationId);
        notification.setUser(user);
        notification.setType("TASK_ASSIGNED");
        notification.setTitle("Test title");
        notification.setMessage(message);
        return notification;
    }
}
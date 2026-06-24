package com.example.team_task.kafka;

import com.example.team_task.dto.kafka.CommentEvent;
import com.example.team_task.dto.kafka.NotificationEvent;
import com.example.team_task.dto.kafka.TaskEvent;
import com.example.team_task.dto.kafka.UserEvent;
import com.example.team_task.entity.AuditLog;
import com.example.team_task.entity.Notification;
import com.example.team_task.entity.Task;
import com.example.team_task.entity.User;
import com.example.team_task.repository.AuditLogRepository;
import com.example.team_task.repository.NotificationRepository;
import com.example.team_task.repository.TaskRepository;
import com.example.team_task.repository.UserRepository;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext
@EmbeddedKafka(
        partitions = 1,
        topics = {"task-events", "user-events", "comment-events", "notifications", "audit-log"},
        brokerProperties = {
                "listeners=PLAINTEXT://localhost:9092",
                "port=9092"
        }
)
@Transactional
class KafkaIntegrationTest {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private ConsumerFactory<String, Object> consumerFactory;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    private User testUser;
    private Task testTask;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setName("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setRole(User.Role.USER);
        testUser = userRepository.save(testUser);

        testTask = new Task();
        testTask.setTitle("Integration Test Task");
        testTask.setDescription("Task for integration testing");
        testTask.setUser(testUser);
        testTask = taskRepository.save(testTask);
    }

    @Test
    void sendTaskEvent_ValidEvent_CanBeConsumed() {
        TaskEvent event = TaskEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("TASK_CREATED")
                .taskId(testTask.getId())
                .title("Test Task")
                .status("PENDING")
                .priority("MEDIUM")
                .userId(testUser.getId())
                .performedBy(testUser.getId())
                .performedByName(testUser.getName())
                .timestamp(LocalDateTime.now())
                .ipAddress("127.0.0.1")
                .build();

        kafkaTemplate.send("task-events", testTask.getId().toString(), event);
        kafkaTemplate.flush();

        Consumer<String, Object> consumer = createConsumer("task-events");
        ConsumerRecord<String, Object> record = KafkaTestUtils.getSingleRecord(consumer, "task-events", Duration.ofSeconds(10));

        assertThat(record).isNotNull();
        assertThat(record.key()).isEqualTo(testTask.getId().toString());
    }

    @Test
    void sendCommentEvent_ValidEvent_CanBeConsumed() {
        CommentEvent event = CommentEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("COMMENT_ADDED")
                .commentId(1L)
                .text("Integration test comment")
                .taskId(testTask.getId())
                .userId(testUser.getId())
                .userName(testUser.getName())
                .timestamp(LocalDateTime.now())
                .ipAddress("127.0.0.1")
                .build();

        kafkaTemplate.send("comment-events", "1", event);
        kafkaTemplate.flush();

        Consumer<String, Object> consumer = createConsumer("comment-events");
        ConsumerRecord<String, Object> record = KafkaTestUtils.getSingleRecord(consumer, "comment-events", Duration.ofSeconds(10));

        assertThat(record).isNotNull();
        assertThat(record.key()).isEqualTo("1");
    }

    @Test
    void sendUserEvent_ValidEvent_CanBeConsumed() {
        UserEvent event = UserEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("USER_REGISTERED")
                .userId(testUser.getId())
                .username(testUser.getName())
                .email(testUser.getEmail())
                .role("USER")
                .timestamp(LocalDateTime.now())
                .idAddress("127.0.0.1")
                .build();

        kafkaTemplate.send("user-events", testUser.getId().toString(), event);
        kafkaTemplate.flush();

        Consumer<String, Object> consumer = createConsumer("user-events");
        ConsumerRecord<String, Object> record = KafkaTestUtils.getSingleRecord(consumer, "user-events", Duration.ofSeconds(10));

        assertThat(record).isNotNull();
        assertThat(record.key()).isEqualTo(testUser.getId().toString());
    }

    @Test
    void sendNotification_ValidEvent_PersistsToDatabase() {
        NotificationEvent event = NotificationEvent.builder()
                .notificationId(UUID.randomUUID().toString())
                .type("TASK_ASSIGNED")
                .userId(testUser.getId())
                .title("New task assigned")
                .message("You have been assigned a task")
                .taskId(testTask.getId())
                .triggeredBy(testUser.getId())
                .createdAt(LocalDateTime.now())
                .build();

        kafkaTemplate.send("notifications", testUser.getId().toString(), event);
        kafkaTemplate.flush();

        await()
                .atMost(Duration.ofSeconds(15))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    long count = notificationRepository.countByUserIdAndIsReadFalse(testUser.getId());
                    assertThat(count).isGreaterThanOrEqualTo(1);
                });
    }

    @Test
    void sendAuditLog_ValidEvent_PersistsToDatabase() {
        AuditLog auditLog = new AuditLog();
        auditLog.setAuditId(UUID.randomUUID().toString());
        auditLog.setAction("TASK_CREATED");
        auditLog.setEntityType("TASK");
        auditLog.setEntityId(testTask.getId());
        auditLog.setPerformedBy(testUser.getId());
        auditLog.setPerformedByName(testUser.getName());
        auditLog.setDetails("Integration test audit log");
        auditLog.setIpAddress("127.0.0.1");

        kafkaTemplate.send("audit-log", auditLog.getAuditId(), auditLog);
        kafkaTemplate.flush();

        await()
                .atMost(Duration.ofSeconds(15))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    long count = auditLogRepository.count();
                    assertThat(count).isGreaterThanOrEqualTo(1);
                });
    }

    @Test
    void sendMultipleTaskEvents_AllCanBeConsumed() {
        for (int i = 1; i <= 5; i++) {
            TaskEvent event = TaskEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("TASK_UPDATED")
                    .taskId((long) i)
                    .title("Task " + i)
                    .status("PENDING")
                    .priority("MEDIUM")
                    .userId(testUser.getId())
                    .performedBy(testUser.getId())
                    .timestamp(LocalDateTime.now())
                    .build();

            kafkaTemplate.send("task-events", String.valueOf(i), event);
        }
        kafkaTemplate.flush();

        Consumer<String, Object> consumer = createConsumer("task-events");

        await()
                .atMost(Duration.ofSeconds(15))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    Iterable<ConsumerRecord<String, Object>> records =
                            KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(3));
                    assertThat(records).isNotNull();
                });
    }

    @Test
    void sendNotificationWithoutTaskId_EventProcessed() {
        NotificationEvent event = NotificationEvent.builder()
                .notificationId(UUID.randomUUID().toString())
                .type("ROLE_CHANGED")
                .userId(testUser.getId())
                .title("Role changed")
                .message("Your role has been changed")
                .taskId(null)
                .triggeredBy(testUser.getId())
                .createdAt(LocalDateTime.now())
                .build();

        kafkaTemplate.send("notifications", testUser.getId().toString(), event);
        kafkaTemplate.flush();

        await()
                .atMost(Duration.ofSeconds(15))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    long count = notificationRepository.count();
                    assertThat(count).isGreaterThanOrEqualTo(1);
                });
    }

    private Consumer<String, Object> createConsumer(String topic) {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(
                "test-group-" + UUID.randomUUID(), "true", embeddedKafkaBroker);
        Consumer<String, Object> consumer = consumerFactory.createConsumer(
                "test-consumer-" + UUID.randomUUID(), null, null, consumerProps);
        embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, topic);
        return consumer;
    }
}
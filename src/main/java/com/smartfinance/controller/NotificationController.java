package com.smartfinance.controller;

import com.smartfinance.dao.NotificationDAO;
import com.smartfinance.model.Notification;
import com.smartfinance.model.User;
import com.smartfinance.service.OverspendingService;
import com.smartfinance.util.AnimationUtils;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.util.ArrayList;

public class NotificationController {
    private final User currentUser;
    private final NotificationDAO notificationDAO;
    private final OverspendingService overspendingService;
    private VBox notificationList;

    public NotificationController(User user) {
        this.currentUser = user;
        this.notificationDAO = new NotificationDAO();
        this.overspendingService = new OverspendingService();
    }

    public VBox createView() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));

        HBox titleRow = new HBox(16);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Notifications");
        title.getStyleClass().add("page-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button checkBtn = new Button("Check Overspending");
        checkBtn.getStyleClass().add("btn-primary");
        checkBtn.setOnAction(e -> {
            ArrayList<String> alerts = overspendingService.detectOverspending(currentUser.getUserId());
            if (alerts.isEmpty()) {
                notificationDAO.insert(new Notification(currentUser.getUserId(),
                    "No overspending detected. Your spending is normal!",
                    java.time.LocalDate.now(), false));
            }
            refreshNotifications();
        });

        Button markAllBtn = new Button("Mark All Read");
        markAllBtn.getStyleClass().add("btn-secondary");
        markAllBtn.setOnAction(e -> { notificationDAO.markAllAsRead(currentUser.getUserId()); refreshNotifications(); });

        Button clearBtn = new Button("Clear All");
        clearBtn.getStyleClass().add("btn-danger");
        clearBtn.setOnAction(e -> { notificationDAO.deleteAll(currentUser.getUserId()); refreshNotifications(); });

        titleRow.getChildren().addAll(title, spacer, checkBtn, markAllBtn, clearBtn);

        int unread = notificationDAO.getUnreadCount(currentUser.getUserId());
        Label unreadLabel = new Label(unread + " unread notification" + (unread != 1 ? "s" : ""));
        unreadLabel.getStyleClass().add("page-subtitle");

        notificationList = new VBox(12);
        refreshNotifications();

        root.getChildren().addAll(titleRow, unreadLabel, notificationList);
        javafx.application.Platform.runLater(() -> AnimationUtils.staggerChildren(root, 60));
        return root;
    }

    private void refreshNotifications() {
        notificationList.getChildren().clear();
        ArrayList<Notification> notifications = notificationDAO.findByUserId(currentUser.getUserId());

        if (notifications.isEmpty()) {
            VBox empty = new VBox(12);
            empty.setAlignment(Pos.CENTER);
            empty.setPadding(new Insets(60));
            Label icon = new Label("\uD83D\uDD14");
            icon.setStyle("-fx-font-size: 48px;");
            Label text = new Label("No notifications yet");
            text.getStyleClass().add("label-muted");
            empty.getChildren().addAll(icon, text);
            notificationList.getChildren().add(empty);
            return;
        }

        for (Notification n : notifications) {
            notificationList.getChildren().add(createItem(n));
        }
        AnimationUtils.staggerChildren(notificationList, 50);
    }

    private HBox createItem(Notification n) {
        HBox item = new HBox(16);
        item.setPadding(new Insets(16));
        item.setAlignment(Pos.CENTER_LEFT);
        item.getStyleClass().add("notification-item");
        if (!n.isRead()) item.getStyleClass().add("notification-item-unread");

        Label icon = new Label(n.isRead() ? "\uD83D\uDD14" : "\uD83D\uDD15");
        icon.setStyle("-fx-font-size: 24px;");

        VBox content = new VBox(4);
        Label msg = new Label(n.getMessage());
        msg.setWrapText(true);
        msg.setMaxWidth(600);
        msg.getStyleClass().add("label-bold");
        Label date = new Label(n.getDate().toString());
        date.getStyleClass().add("label-muted");
        content.getChildren().addAll(msg, date);

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        VBox actions = new VBox(6);
        actions.setAlignment(Pos.CENTER_RIGHT);
        if (!n.isRead()) {
            Button readBtn = new Button("\u2713");
            readBtn.getStyleClass().add("btn-success");
            readBtn.setStyle("-fx-padding: 4 10;");
            readBtn.setOnAction(e -> { notificationDAO.markAsRead(n.getNotificationId()); refreshNotifications(); });
            actions.getChildren().add(readBtn);
        }
        Button delBtn = new Button("\u2716");
        delBtn.getStyleClass().add("btn-danger");
        delBtn.setStyle("-fx-padding: 4 10;");
        delBtn.setOnAction(e -> { notificationDAO.delete(n.getNotificationId()); refreshNotifications(); });
        actions.getChildren().add(delBtn);

        item.getChildren().addAll(icon, content, sp, actions);
        AnimationUtils.addHoverScale(item, 1.01);
        return item;
    }
}

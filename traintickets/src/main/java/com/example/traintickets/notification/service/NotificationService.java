package com.example.traintickets.notification.service;

import com.example.traintickets.booking.model.Booking;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@Service
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final boolean emailEnabled;
    private final String fromAddress;

    public NotificationService(ObjectProvider<JavaMailSender> mailSenderProvider,
                               @Value("${app.notifications.email.enabled:false}") boolean emailEnabled,
                               @Value("${app.notifications.email.from:no-reply@traintickets.local}") String fromAddress) {
        this.mailSenderProvider = mailSenderProvider;
        this.emailEnabled = emailEnabled;
        this.fromAddress = fromAddress;
    }

    public void sendBookingConfirmation(Booking booking) {
        String subject = "Train ticket booking confirmation #" + booking.getId();
        String text = """
                Your booking has been confirmed.

                Booking ID: %d
                Trip ID: %d
                Train: %s
                Route: %s
                Segment: %s to %s
                Departure: %s
                Tickets: %d
                Passengers:
                %s
                """.formatted(
                booking.getId(),
                booking.getTrip().getId(),
                booking.getTrip().getTrain().getName(),
                booking.getTrip().getRoute().getName(),
                booking.getFromStation().getName(),
                booking.getToStation().getName(),
                booking.getTrip().getDepartureDateTime().format(DATE_TIME_FORMATTER),
                booking.getTicketCount(),
                passengerNames(booking)
        );
        sendEmail(booking.getCustomerEmail(), subject, text,
                "Booking confirmation email queued for {} booking {}", booking.getCustomerEmail(), booking.getId());
    }

    public void sendDelayNotification(Booking booking, int delayMinutes, String reason) {
        String subject = "Delay update for booking #" + booking.getId();
        String text = """
                A delay has been applied to your trip.

                Booking ID: %d
                Trip ID: %d
                Train: %s
                Route: %s
                Segment: %s to %s
                Updated delay: %d minutes
                Reason: %s
                """.formatted(
                booking.getId(),
                booking.getTrip().getId(),
                booking.getTrip().getTrain().getName(),
                booking.getTrip().getRoute().getName(),
                booking.getFromStation().getName(),
                booking.getToStation().getName(),
                delayMinutes,
                reason
        );
        sendEmail(booking.getCustomerEmail(), subject, text,
                "Delay email queued for {} booking {}: {} minutes, reason={}",
                booking.getCustomerEmail(), booking.getId(), delayMinutes, reason);
    }

    private void sendEmail(String to, String subject, String text, String fallbackLogMessage, Object... fallbackLogArgs) {
        if (!emailEnabled) {
            log.info(fallbackLogMessage, fallbackLogArgs);
            return;
        }

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.warn("Email notifications are enabled, but no JavaMailSender is available");
            log.info(fallbackLogMessage, fallbackLogArgs);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            log.info("Email sent to {} with subject {}", to, subject);
        } catch (MailException ex) {
            log.warn("Could not send email to {} with subject {}: {}", to, subject, ex.getMessage());
            log.info(fallbackLogMessage, fallbackLogArgs);
        }
    }

    private String passengerNames(Booking booking) {
        String names = booking.getTickets().stream()
                .map(ticket -> ticket.getPassengerName() == null || ticket.getPassengerName().isBlank()
                        ? "Passenger"
                        : ticket.getPassengerName())
                .collect(Collectors.joining("\n"));
        return names.isBlank() ? "Passenger" : names;
    }
}

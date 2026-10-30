package com.localexperts.service;

import com.localexperts.model.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;

// ── Order Email Service ───────────────────────────────────────
// Sends two emails on every new order:
//   1. To Business OWNER  → "New Order / Booking Received"
//   2. To CUSTOMER        → "Your Order is Confirmed"
@Service
public class OrderEmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    // ── Entry point: send both emails ─────────────────────────
    public void sendOrderEmails(Order order) {
        try {
            sendOwnerEmail(order);
        } catch (Exception e) {
            System.err.println("❌ Owner email failed: " + e.getMessage());
        }
        try {
            sendCustomerEmail(order);
        } catch (Exception e) {
            System.err.println("❌ Customer email failed: " + e.getMessage());
        }
    }

    // ── 1. Email to Business Owner ────────────────────────────
    private void sendOwnerEmail(Order order) throws Exception {
        MimeMessage msg = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(order.getOwnerEmail());
        helper.setSubject("🔔 New " + getTypeName(order.getBusinessCategory())
                + " Order — " + order.getBusinessName());
        helper.setText(buildOwnerEmailHtml(order), true);

        mailSender.send(msg);
        System.out.println("✅ Owner email sent → " + order.getOwnerEmail());
    }

    // ── 2. Email to Customer ──────────────────────────────────
    private void sendCustomerEmail(Order order) throws Exception {
        MimeMessage msg = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(order.getCustomerEmail());
        helper.setSubject("✅ Your " + getTypeName(order.getBusinessCategory())
                + " Booking is Confirmed — " + order.getBusinessName());
        helper.setText(buildCustomerEmailHtml(order), true);

        mailSender.send(msg);
        System.out.println("✅ Customer email sent → " + order.getCustomerEmail());
    }

    // ── Owner Email HTML ──────────────────────────────────────
    private String buildOwnerEmailHtml(Order order) {
        return "<!DOCTYPE html><html><body style='font-family:Arial,sans-serif;max-width:600px;margin:auto;'>"
            + "<div style='background:#6c63ff;padding:20px;border-radius:12px 12px 0 0;text-align:center;'>"
            + "<h2 style='color:#fff;margin:0;'>🔔 New Booking Received!</h2></div>"
            + "<div style='background:#f9f9f9;padding:24px;border-radius:0 0 12px 12px;'>"
            + "<h3 style='color:#333;'>Business: " + order.getBusinessName() + "</h3>"
            + "<p style='color:#666;'>A new <b>" + getTypeName(order.getBusinessCategory())
            + "</b> order has been placed. Details below:</p>"
            + "<table style='width:100%;border-collapse:collapse;margin:16px 0;'>"
            + "<tr style='background:#6c63ff11;'><td style='padding:10px;font-weight:bold;width:40%;'>Order ID</td>"
            + "<td style='padding:10px;'>#" + order.getId() + "</td></tr>"
            + "<tr><td style='padding:10px;font-weight:bold;'>Customer Name</td>"
            + "<td style='padding:10px;'>" + order.getCustomerName() + "</td></tr>"
            + "<tr style='background:#6c63ff11;'><td style='padding:10px;font-weight:bold;'>Customer Email</td>"
            + "<td style='padding:10px;'>" + order.getCustomerEmail() + "</td></tr>"
            + "<tr><td style='padding:10px;font-weight:bold;'>Customer Phone</td>"
            + "<td style='padding:10px;'>" + order.getCustomerPhone() + "</td></tr>"
            + buildExtraFieldsTable(order)
            + (order.getNotes() != null && !order.getNotes().isEmpty()
                ? "<tr><td style='padding:10px;font-weight:bold;'>Special Notes</td>"
                  + "<td style='padding:10px;'>" + order.getNotes() + "</td></tr>" : "")
            + "</table>"
            + "<p style='color:#888;font-size:13px;margin-top:20px;'>Please confirm or contact the customer promptly.</p>"
            + "<p style='color:#888;font-size:12px;'>— LocalExperts+ Team</p>"
            + "</div></body></html>";
    }

    // ── Customer Email HTML ───────────────────────────────────
    private String buildCustomerEmailHtml(Order order) {
        return "<!DOCTYPE html><html><body style='font-family:Arial,sans-serif;max-width:600px;margin:auto;'>"
            + "<div style='background:#00b894;padding:20px;border-radius:12px 12px 0 0;text-align:center;'>"
            + "<h2 style='color:#fff;margin:0;'>✅ Booking Confirmed!</h2></div>"
            + "<div style='background:#f9f9f9;padding:24px;border-radius:0 0 12px 12px;'>"
            + "<p style='color:#333;font-size:16px;'>Hi <b>" + order.getCustomerName() + "</b>,</p>"
            + "<p style='color:#555;'>Your <b>" + getTypeName(order.getBusinessCategory())
            + "</b> booking at <b>" + order.getBusinessName() + "</b> has been received successfully! 🎉</p>"
            + "<table style='width:100%;border-collapse:collapse;margin:16px 0;'>"
            + "<tr style='background:#00b89411;'><td style='padding:10px;font-weight:bold;width:40%;'>Order ID</td>"
            + "<td style='padding:10px;'>#" + order.getId() + "</td></tr>"
            + buildExtraFieldsTable(order)
            + (order.getNotes() != null && !order.getNotes().isEmpty()
                ? "<tr><td style='padding:10px;font-weight:bold;'>Your Notes</td>"
                  + "<td style='padding:10px;'>" + order.getNotes() + "</td></tr>" : "")
            + "</table>"
            + "<p style='color:#555;'>The business owner will contact you shortly to confirm.</p>"
            + "<p style='color:#888;font-size:12px;margin-top:20px;'>Thank you for using LocalExperts+!</p>"
            + "</div></body></html>";
    }

    // ── Build extra fields table rows ─────────────────────────
    private String buildExtraFieldsTable(Order order) {
        if (order.getExtraFields() == null || order.getExtraFields().isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        boolean alternate = false;
        for (var entry : order.getExtraFields().entrySet()) {
            String bg = alternate ? "background:#f0f0f0;" : "";
            sb.append("<tr style='" + bg + "'><td style='padding:10px;font-weight:bold;'>")
              .append(formatKey(entry.getKey()))
              .append("</td><td style='padding:10px;'>")
              .append(entry.getValue())
              .append("</td></tr>");
            alternate = !alternate;
        }
        return sb.toString();
    }

    // ── Helpers ───────────────────────────────────────────────
    private String getTypeName(String category) {
        if (category == null) return "Service";
        return switch (category.toLowerCase()) {
            case "hotels"     -> "Hotel / Room";
            case "food"       -> "Food";
            case "healthcare" -> "Healthcare / Appointment";
            case "services"   -> "Service";
            case "grocery"    -> "Grocery";
            case "education"  -> "Education / Admission";
            default           -> category;
        };
    }

    private String formatKey(String key) {
        // camelCase → Title Case with spaces
        return key.replaceAll("([A-Z])", " $1")
                  .replaceAll("^.", String.valueOf(Character.toUpperCase(key.charAt(0))))
                  .trim();
    }
}

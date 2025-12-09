package com.vnpt.mini_project_java.service.email;


import com.vnpt.mini_project_java.entity.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    @Async
    public void sendOrderEmailAsync(String email, Order order) {
        try {
            Thread.sleep(30 * 1000);
            sendOrderEmail(email, order);
            System.out.println("Email sent successfully");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Async
    public void sendCancelOrderEmailAsync(String email, Order order) {
        try {
            Thread.sleep(30 * 1000);
            sendCancelOrderEmail(email, order);
            System.out.println("Email sent successfully");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void sendOrderEmail(String toEmail, Order order) {
        String subject = "[Mini-Shop] Xác nhận Đặt hàng #" + order.getOrderID();

        String body = "Xin chào " + order.getReceiverName() + ",\n\n" +
                "Đơn hàng của bạn đã được tiếp nhận thành công!\n\n" +
                "Mã đơn hàng: #" + order.getOrderID() + "\n" +
                "Ngày đặt: " + order.getOrderDateImport() + "\n" +
                "Tổng tiền: " + String.format("%.0f", order.getOrderTotal()) + " VNĐ\n\n" +
                "Phương thức thanh toán: " + order.getPaymentMethod() + "\n" +
                "Địa chỉ giao hàng: " + order.getShippingAddress() + "\n\n" +
                "Chúng tôi sẽ thông báo khi đơn hàng được xác nhận.\n\n" +
                "Trân trọng,\nMini-Shop Support";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("lamnguyen4791@gmail.com");
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }

    public void sendCancelOrderEmail(String to, Order order) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Đơn hàng #" + order.getOrderID() + " đã bị hủy");
            message.setText(
                    "Xin chào " + order.getReceiverName() + ",\n\n" +
                            "Đơn hàng của bạn đã bị hủy:\n" +
                            "Mã đơn: #" + order.getOrderID() + "\n" +
                            "Ngày đặt: " + order.getOrderDateImport() + "\n" +
                            "Tổng tiền: " + String.format("%.0f", order.getOrderTotal()) + " VNĐ\n\n" +
                            "Nếu có thắc mắc, vui lòng liên hệ với chúng tôi.\n\n" +
                            "Cảm ơn ,\nMini-Shop Support!"
            );
            mailSender.send(message);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}

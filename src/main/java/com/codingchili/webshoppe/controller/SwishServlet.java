package com.codingchili.webshoppe.controller;

import com.codingchili.webshoppe.Properties;
import com.codingchili.webshoppe.model.*;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.CompletableFuture;

/**
 * @author Robin Duda
 *
 * Swish payment integration.
 */
@WebServlet("/swish")
public class SwishServlet extends HttpServlet {
    private static final String QRG_SWISH_API_V_1_PREFILLED = "/qrg-swish/api/v1/prefilled";
    private static final String MPC_GETSWISH_NET = "mpc.getswish.net";
    private static final int PORT = 443;
    private static final String IMAGE_SVG = "image/svg+xml";
    private static Vertx vertx = Vertx.vertx();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        CompletableFuture<Void> qrImage = new CompletableFuture<>();
        int orderId = Integer.parseInt(req.getParameter("orderId"));

        vertx.createHttpClient(new HttpClientOptions().setSsl(true))
                .post(PORT, MPC_GETSWISH_NET, QRG_SWISH_API_V_1_PREFILLED)
                .handler(response -> {

                    if (response.statusCode() >= 300) {
                        req.setAttribute("message", "Failed to load order details, try later.");
                        Forwarding.to("error.jsp", req, resp);
                    }

                    response.bodyHandler(buffer -> {
                        try (OutputStream out = resp.getOutputStream()) {
                            resp.setContentType(IMAGE_SVG);
                            out.write(buffer.getBytes());
                            qrImage.complete(null);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });

                })
                .putHeader("Content-Type", "application/json")
                .end(createQRRequest(Session.getAccount(req), orderId));

        // can't exit the handler before the response is written - the stream will be closed.
        qrImage.join();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        req.setAttribute("message", "Thank you for your payment. Your order will be processed as soon as possible.");
        Forwarding.to("success.jsp", req, resp);
    }

    private String createQRRequest(Account account, int orderId) {
        Order order = OrderManager.getOrderById(account, orderId);

        String json = new JsonObject()
                .put("format", "svg")
                .put("message", new JsonObject()
                        .put("value", "order " +order.getOrderId())
                        .put("editable", false))
                .put("payee",
                        new JsonObject()
                                .put("value", Properties.get().getSwishReceiver())
                                .put("editable", false))
                .put("amount", new JsonObject()
                        .put("value", order.getTotal())
                        .put("editable", false))
                .put("size", 600)          // n/a for svg
                .put("border", 1)
                .put("transparent", false) // n/a for svg
                .encode();
        return json;
    }
}
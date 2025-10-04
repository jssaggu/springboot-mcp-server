package com.saggu.ai.mcp.service;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

@Service
public class OrderMcpServerService {

    @Tool(description = "Get the status of an order by its ID.")
    public String getOrderStatus(@ToolParam(description = "Order Status e.g. orderId 1234") String orderId) {
        return switch (orderId.toLowerCase()) {
            case "1234" -> "Shipped";
            case "2222" -> "Payment Pending";
            case "9999" -> "Payment Rejected. Attention Required.";
            default -> "Order not found.";
        };
    }
}

package com.deepak.paymentService.service;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.deepak.paymentService.Exception.PaymentException;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

@Service
public class RazorpayGatewayService {

    @Value("${razorpay.key-id}")
    private String keyId;

    @Value("${razorpay.key-secret}")
    private String keySecret;


    // =========================================================
    // CREATE RAZORPAY ORDER
    // =========================================================

    public Order createOrder(JSONObject options) {

        try {

            System.out.println("======================================");
            System.out.println("RAZORPAY ORDER REQUEST");
            System.out.println(options.toString());
            System.out.println("======================================");


            // -------------------------------------------------
            // Create Razorpay Client
            // -------------------------------------------------

            RazorpayClient razorpay =
                    new RazorpayClient(
                            keyId,
                            keySecret
                    );


            // -------------------------------------------------
            // Create Razorpay Order
            // -------------------------------------------------

            Order razorpayOrder =
                    razorpay.orders.create(options);


            // -------------------------------------------------
            // Print Razorpay Response
            // -------------------------------------------------

            System.out.println("======================================");
            System.out.println("RAZORPAY ORDER CREATED");
            System.out.println(
                    "Razorpay Order ID = "
                            + razorpayOrder.get("id")
            );

            System.out.println(
                    "Amount = "
                            + razorpayOrder.get("amount")
            );

            System.out.println(
                    "Currency = "
                            + razorpayOrder.get("currency")
            );

            System.out.println(
                    "Status = "
                            + razorpayOrder.get("status")
            );

            System.out.println("======================================");


            return razorpayOrder;


        } catch (RazorpayException e) {

            System.err.println("======================================");
            System.err.println("RAZORPAY ORDER CREATION ERROR");
            System.err.println(
                    "Message = "
                            + e.getMessage()
            );
            System.err.println("======================================");


            throw new PaymentException(
                    "Unable to create Razorpay Order: "
                            + e.getMessage()
            );
        }
    }
}
package com.deepak.paymentService.Util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.razorpay.Utils;

@Component
public class RazorpaySignatureUtil {

	@Value("${razorpay.webhook-secret}")
	private String webhookSecret;


	
	// verify signature
	public boolean verifySignature(String payload, String signature) {

		try {

			return Utils.verifyWebhookSignature(payload, signature, webhookSecret);

		} catch (Exception e) {

			return false;
		}
	}
}

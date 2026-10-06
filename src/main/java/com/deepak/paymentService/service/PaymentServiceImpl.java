package com.deepak.paymentService.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.deepak.paymentService.DTO.NotificationRequestDto;
import com.deepak.paymentService.DTO.OrderItemResponseDto;
import com.deepak.paymentService.DTO.PaymentRequestDto;
import com.deepak.paymentService.DTO.PaymentResponseDto;
import com.deepak.paymentService.DTO.PaymentVerifyReq;
import com.deepak.paymentService.DTO.PaymentVerifyResDto;
import com.deepak.paymentService.DTO.orderResponseDto;
import com.deepak.paymentService.Exception.PaymentException;
import com.deepak.paymentService.Repository.PaymentRepository;
import com.deepak.paymentService.client.CartClient;
import com.deepak.paymentService.client.InventoryClient;
import com.deepak.paymentService.client.NotificationClient;
import com.deepak.paymentService.client.OrderClient;
import com.deepak.paymentService.entity.Payment;
import com.deepak.paymentService.entity.PaymentStatus;
import com.deepak.paymentService.kafka.PaymentKafkaProducer;
import com.deepak.paymentService.kafka.PaymentSuccessEvent;
import com.razorpay.Order;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

	@Value("${razorpay.key-id}")
	private String keyId;

	private final PaymentRepository paymentRepository;

	private final OrderClient orderClient;

	private final InventoryClient inventoryClient;

	private final CartClient cartClient;

	private final RazorpayGatewayService razorpayGatewayService; // NAYA — retry/rate-limit ke saath Razorpay call

	private final NotificationClient notificationclient;

	private final PaymentKafkaProducer paymentKafkaProducer;

	@Override
	public PaymentResponseDto createOrder(PaymentRequestDto request) {

		PaymentResponseDto response = new PaymentResponseDto();

		// =========================================================
		// 1. VALIDATE PAYMENT METHOD
		// =========================================================

		if (request.getPaymentMethod() == null || request.getPaymentMethod().isBlank()) {

			throw new PaymentException("Payment method is required");
		}

		String paymentMethod = request.getPaymentMethod().toUpperCase();

		// =========================================================
		// 2. COD PAYMENT
		// =========================================================

		if (paymentMethod.equals("COD")) {

			// -----------------------------------------------------
			// Check if payment already exists
			// -----------------------------------------------------

			Optional<Payment> existingPayment = paymentRepository.findByOrderId(request.getOrderId());

			// if exiting payment already created in paymentdb
			if (existingPayment.isPresent()) {

				Payment existing = existingPayment.get();

				response.setPaymentId(existing.getPaymentId());

				response.setOrderId(existing.getOrderId());

				response.setAmount(existing.getAmount());

				response.setCurrency(existing.getCurrency());

				response.setPaymentStatus(existing.getPaymentStatus());

				response.setRazorpayOrderId(existing.getRazorpayOrderId());

				response.setKey(keyId);

				response.setMessage("Payment already exists for this order PAY The amount ...");

				return response;
			}

			// -----------------------------------------------------
			// Else: Create COD Payment & save in paymentdb
			// -----------------------------------------------------

			Payment payment = new Payment();

			payment.setOrderId(request.getOrderId());

			payment.setAmount(request.getAmount());

			payment.setCurrency("INR");

			payment.setPaymentMethod("COD");

			// COD payment is collected at delivery
			payment.setPaymentStatus(PaymentStatus.PENDING);

			// No Razorpay involved in COD
			payment.setRazorpayOrderId(null);

			payment.setRazorpayPaymentId(null);

			payment.setRazorpaySignature(null);

			payment.setTransactionId(null);

			// payment.setPaymentLink(null);

			// payment.setRazorpayPaymentLinkId(null);

			payment.setPaymentDate(null);

			Payment savedPayment = paymentRepository.save(payment);

			// -----------------------------------------------------
			// COD Response
			// -----------------------------------------------------

			response.setPaymentId(savedPayment.getPaymentId());

			response.setOrderId(savedPayment.getOrderId());

			response.setAmount(savedPayment.getAmount());

			response.setCurrency(savedPayment.getCurrency());

			response.setPaymentStatus(savedPayment.getPaymentStatus());

			response.setRazorpayOrderId(null);

			response.setKey(keyId);

			response.setMessage("Cash on Delivery selected");

			return response;
		}

		// =========================================================
		// 3. ONLINE PAYMENT
		// =========================================================

		if (paymentMethod.equals("ONLINE")) {

			// -----------------------------------------------------
			// Check existing payment for this internal order | exist : if user close the
			// browser before payment
			// -----------------------------------------------------

			Optional<Payment> existingPayment = paymentRepository.findByOrderId(request.getOrderId());

			if (existingPayment.isPresent()) {

				Payment existing = existingPayment.get();

				// -------------------------------------------------
				// Existing pending payment
				// -------------------------------------------------

				if (existing.getPaymentStatus() == PaymentStatus.PENDING && existing.getRazorpayOrderId() != null) {

					response.setPaymentId(existing.getPaymentId());

					response.setOrderId(existing.getOrderId());

					response.setAmount(existing.getAmount());

					response.setCurrency(existing.getCurrency());

					response.setPaymentStatus(existing.getPaymentStatus());

					response.setRazorpayOrderId(existing.getRazorpayOrderId());

					response.setKey(keyId);

					response.setMessage("Existing Razorpay order reused");

					return response;
				}

				// -------------------------------------------------
				// Already paid
				// -------------------------------------------------

				if (existing.getPaymentStatus() == PaymentStatus.PAID) {

					throw new PaymentException("Payment already completed for orderId: " + request.getOrderId());
				}
			}

			// =====================================================
			// 4. VALIDATE AMOUNT
			// =====================================================

			if (request.getAmount() == null || request.getAmount() <= 0) {

				throw new PaymentException("Payment amount must be greater than zero");
			}

			// =====================================================
			// 5. CONVERT RUPEES TO PAISE
			// =====================================================

			long amountInPaise = Math.round(request.getAmount() * 100);

			// =====================================================
			// 6. CREATE RAZORPAY ORDER
			// =====================================================

			try {

				JSONObject options = new JSONObject();

				// Razorpay amount is always in paise
				options.put("amount", amountInPaise);

				options.put("currency", "INR");

				// Your internal order number
				options.put("receipt", request.getOrderNumber());

				// =================================================
				// NOTES
				// =================================================

				JSONObject notes = new JSONObject();

				notes.put("internalOrderId", String.valueOf(request.getOrderId()));

				notes.put("orderNumber", request.getOrderNumber());

				options.put("notes", notes);

				// =================================================
				// CALL RAZORPAY API
				// =================================================

				Order razorpayOrder =

						razorpayGatewayService.createOrder(options);

				// =================================================
				// 7. GET RAZORPAY ORDER ID
				// =================================================

				String razorpayOrderId = razorpayOrder.get("id").toString();

				System.out.println("====================================");

				System.out.println("Razorpay Order Created");

				System.out.println("Internal Order ID = " + request.getOrderId());

				System.out.println("Razorpay Order ID = " + razorpayOrderId);

				System.out.println("Amount = " + request.getAmount());

				System.out.println("====================================");

				// =================================================
				// 8. SAVE PAYMENT | for new Online payment | or if not existing
				// razaorpayorderid
				// =================================================

				Payment payment = new Payment();

				payment.setOrderId(request.getOrderId());

				payment.setAmount(request.getAmount());

				payment.setCurrency("INR");

				payment.setPaymentMethod("ONLINE");

				// Customer has NOT paid yet
				payment.setPaymentStatus(PaymentStatus.PENDING);

				// IMPORTANT
				// Save Razorpay Order ID
				payment.setRazorpayOrderId(razorpayOrderId);

				// These will be populated after payment
				payment.setRazorpayPaymentId(null);

				payment.setRazorpaySignature(null);

				payment.setTransactionId(null);

				// payment.setPaymentLink(null);

				// payment.setRazorpayPaymentLinkId(null);

				payment.setPaymentDate(null);

				Payment savedPayment = paymentRepository.save(payment);

				// =================================================
				// 9. CREATE RESPONSE
				// =================================================

				response.setPaymentId(savedPayment.getPaymentId());

				response.setOrderId(savedPayment.getOrderId());

				response.setAmount(savedPayment.getAmount());

				response.setCurrency(savedPayment.getCurrency());

				response.setPaymentStatus(savedPayment.getPaymentStatus());

				// VERY IMPORTANT
				// Frontend needs this for Razorpay Checkout
				response.setRazorpayOrderId(savedPayment.getRazorpayOrderId());

				// Razorpay public key
				response.setKey(keyId);

				response.setMessage("Razorpay order created successfully");

				return response;

			} catch (PaymentException e) {

				throw e;

			} catch (Exception e) {

				e.printStackTrace();

				throw new PaymentException("Unable to create  Razorpay order: " + e.getMessage());
			}
		}

		// =========================================================
		// 4. INVALID PAYMENT METHOD
		// =========================================================

		throw new PaymentException("Unsupported payment method: " + request.getPaymentMethod());
	}
	// payment verification

	@Override
	public PaymentVerifyResDto verify(PaymentVerifyReq req) {

		PaymentVerifyResDto res = new PaymentVerifyResDto();

		return null;
	}

	// called after verify the payment signature...... | to update data
	@Override
	@Transactional
	public void handlePaymentCaptured(JSONObject json, String signature) {

		System.out.println("===== PAYMENT CAPTURED START =====");

		// =========================================================
		// 1. GET PAYMENT ENTITY FROM RAZORPAY WEBHOOK
		// =========================================================

		JSONObject paymentEntity = json.getJSONObject("payload").getJSONObject("payment").getJSONObject("entity");

		String razorpayPaymentId = paymentEntity.getString("id");

		String razorpayOrderId = paymentEntity.optString("order_id", null);

		String paymentStatus = paymentEntity.getString("status");

		System.out.println("Razorpay Payment ID = " + razorpayPaymentId);

		System.out.println("Razorpay Order ID = " + razorpayOrderId);

		System.out.println("Payment Status = " + paymentStatus);

		// =========================================================
		// 2. ONLY PROCESS CAPTURED PAYMENT
		// =========================================================

		if (!"captured".equalsIgnoreCase(paymentStatus)) {

			System.out.println("Payment is not captured. Current status = " + paymentStatus);

			return;
		}

		// =========================================================
		// 3. GET INTERNAL ORDER ID FROM RAZORPAY NOTES
		// =========================================================

		if (!paymentEntity.has("notes") || !paymentEntity.getJSONObject("notes").has("internalOrderId")) {

			throw new RuntimeException("internalOrderId missing in webhook notes");
		}

		Long internalOrderId = Long.valueOf(paymentEntity.getJSONObject("notes").getString("internalOrderId"));

		System.out.println("Internal Order ID from notes = " + internalOrderId);

		// =========================================================
		// 4. FIND PAYMENT
		// =========================================================

		Payment paymentData = paymentRepository.findByOrderId(internalOrderId)
				.orElseThrow(() -> new RuntimeException("Payment not found for Order ID: " + internalOrderId));

		System.out.println("Payment found. Internal Payment ID = " + paymentData.getPaymentId());

		// =========================================================
		// 5. DUPLICATE WEBHOOK PROTECTION
		// =========================================================

		if (paymentData.getPaymentStatus() == PaymentStatus.PAID) {

			System.out.println("Payment already processed. " + "Ignoring duplicate webhook."

			);

			return;
		}

		// =========================================================
		// 6. CREATE TRANSACTION ID..
		// =========================================================

		String transactionId = "TXN-" + UUID.randomUUID();

		// =========================================================
		// 7. UPDATE PAYMENT
		// =========================================================

		paymentData.setPaymentStatus(PaymentStatus.PAID);

		paymentData.setRazorpayPaymentId(razorpayPaymentId);

		paymentData.setRazorpayOrderId(razorpayOrderId);

		paymentData.setRazorpaySignature(signature);

		paymentData.setPaymentDate(LocalDateTime.now());

		paymentData.setTransactionId(transactionId);

		paymentRepository.save(paymentData);

		System.out.println("Payment status updated to PAID");

		// =========================================================
		// 8. PREPARE KAFKA EVENT
		// =========================================================

		PaymentSuccessEvent event = new PaymentSuccessEvent();
		event.setOrderId(paymentData.getOrderId());
		event.setTransactionId(paymentData.getTransactionId());
		event.setAmount(paymentData.getAmount());
		event.setPaymentMethod(paymentData.getPaymentMethod());
		event.setPaymentStatus(paymentData.getPaymentStatus());
		event.setPaymentId(paymentData.getPaymentId());

		// =========================================================
		// 9. PUBLISH EVENT      [FOR UPDATE > INVENTORY STOCK, ORDER STATUS,]
		// =========================================================
		paymentKafkaProducer.publishPaymentSuccess(event);

	}

	@Override
	public PaymentResponseDto getPayment(Long orderId) {

		// 1. check order payment exists for particular order or not
		Payment payment = paymentRepository.findByOrderId(orderId)
				.orElseThrow(() -> new RuntimeException("Payment not found"));

		// 2 if payment found then send info. to the user as response

		PaymentResponseDto response = new PaymentResponseDto();

		// 3. Set payment details
		response.setPaymentId(payment.getPaymentId());

		response.setOrderId(payment.getOrderId());

		response.setAmount(payment.getAmount());

		response.setCurrency(payment.getCurrency());

		response.setPaymentStatus(payment.getPaymentStatus());

		// response.setPaymentLink(payment.getPaymentLink());

		response.setRazorpayOrderId(payment.getRazorpayOrderId());

		response.setKey(keyId);

		response.setMessage("Payment details fetched successfully");

		return response;

	}

	@Override
	public boolean verifySignature(String payload, String signature) {
		// TODO Auto-generated method stub
		return false;
	}

}

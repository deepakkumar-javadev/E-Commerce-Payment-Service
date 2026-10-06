# E-Commerce Payment Service

Payment Service is a Spring Boot microservice responsible for managing payment-related operations in the e-commerce application. It supports both **Cash on Delivery (COD)** and **online payments using Razorpay**.

The service works with the **Order Service** and uses **Apache Kafka** for asynchronous payment events and communication between microservices.

---

## Features

* Supports Cash on Delivery (COD) payments
* Supports online payments using Razorpay
* Creates and manages payment records
* Tracks payment status
* Handles successful and failed payments
* Integrates with Order Service
* Publishes payment events using Kafka
* Supports payment confirmation through Razorpay webhook
* Uses MySQL for payment data persistence
* Registers with Eureka Service Discovery
* Secured communication between microservices

---

## Technologies Used

* **Java 17**
* **Spring Boot**
* **Spring Data JPA**
* **Spring Security**
* **Spring Cloud Eureka**
* **Apache Kafka**
* **MySQL**
* **Razorpay**
* **Maven**

---

## Payment Types

### 1. Cash on Delivery

For COD orders, the Payment Service creates a payment record with a pending status.

```text
Customer
   ↓
Order Service
   ↓
Payment Service
   ↓
COD Payment Created
   ↓
Payment Status: PENDING
   ↓
Order Processing
```

The payment can be completed when the order is delivered.

---

### 2. Online Payment

For online payments, the Payment Service integrates with Razorpay.

```text
Customer
   ↓
Order Service
   ↓
Payment Service
   ↓
Razorpay Order
   ↓
Customer Completes Payment
   ↓
Razorpay
   ↓
Webhook
   ↓
Payment Service
   ↓
Payment Status Updated
   ↓
Kafka Event
   ↓
Order Service
```

---

## Payment Flow

The Payment Service participates in the overall order and payment workflow.

```text
                    Customer
                       │
                       ▼
                ┌──────────────┐
                │ Order Service│
                │    :8086     │
                └──────┬───────┘
                       │
                       ▼
               ┌───────────────┐
               │Payment Service│
               │     :8087     │
               └───────┬───────┘
                       │
              ┌────────┴────────┐
              │                 │
              ▼                 ▼
        ┌──────────┐       ┌──────────┐
        │   COD    │       │ Razorpay │
        └──────────┘       └────┬─────┘
                                │
                                ▼
                           Payment
                           Success
                                │
                                ▼
                            Kafka Event
                                │
                                ▼
                         Order Service
```

---

## Kafka Integration

Kafka is used for asynchronous communication between services.

After successful online payment, the Payment Service publishes a payment success event.

Example event:

```text
payment.success
```

The Order Service can consume this event and update the corresponding order/payment status.

```text
Payment Service
      │
      │ payment.success
      ▼
    Kafka
      │
      ▼
Order Service
```

This helps keep the microservices loosely coupled.

---

## Razorpay Integration

For online payments, Razorpay is used as the external payment gateway.

The general flow is:

```text
1. Customer places an online order
2. Order Service requests payment processing
3. Payment Service creates Razorpay payment/order details
4. Customer completes payment on Razorpay
5. Razorpay sends payment result/webhook
6. Payment Service validates the payment
7. Payment status is updated
8. Payment success event is published through Kafka
9. Order Service processes the payment event
```

---

## Database

Payment information is stored in MySQL.

```text
Database: ecompaymentdb
```

The service uses **Spring Data JPA** for database operations.

---

## Service Configuration

```text
Service Name: ECOM-PAYMENT-SERVICE
Port: 8087
Database: ecompaymentdb
```

The service registers itself with the Eureka Server for service discovery.

```text
Payment Service
      │
      ▼
Eureka Server
    :8761
```

---

## Microservices Communication

The Payment Service communicates with other services as part of the e-commerce workflow.

```text
                   ┌─────────────────┐
                   │   API Gateway   │
                   │      :8080      │
                   └────────┬────────┘
                            │
                            ▼
                   ┌─────────────────┐
                   │  Order Service  │
                   │      :8086      │
                   └────────┬────────┘
                            │
                            ▼
                   ┌─────────────────┐
                   │ Payment Service │
                   │      :8087      │
                   └────────┬────────┘
                            │
                    ┌───────┴────────┐
                    ▼                ▼
                 MySQL           Razorpay
                    │
                    ▼
                 Kafka
```

---

## Project Structure

```text
src/main/java
└── com.example.payment
    ├── controller
    ├── service
    ├── repository
    ├── entity
    ├── dto
    ├── config
    └── kafka
```

### Main Layers

**Controller**
Handles incoming payment-related REST requests.

**Service**
Contains payment processing and business logic.

**Repository**
Handles database operations using Spring Data JPA.

**Entity**
Represents payment-related database tables.

**DTO**
Used for transferring payment data between services and APIs.

**Kafka**
Handles asynchronous payment events.

**Config**
Contains security, Kafka, Eureka, and other service configurations.

---

## Security

The Payment Service is integrated with the application's security architecture using **Spring Security and JWT-based authentication**.

Requests between microservices can carry the authorization token to ensure that protected APIs are accessed securely.

---

## Role in the E-Commerce Application

The Payment Service is responsible for managing the payment part of the order lifecycle.

It provides support for:

* COD payment creation
* Online payment processing
* Razorpay integration
* Payment status management
* Payment success handling
* Payment event publishing
* Communication with Order Service

This allows the Order Service to focus on **order management**, while the Payment Service handles **payment-related responsibilities**.

---

## Order and Payment Relationship

```text
Order Created
     │
     ▼
Payment Created
     │
     ├── COD
     │     └── PENDING
     │
     └── ONLINE
           │
           ▼
       Razorpay
           │
           ▼
     Payment Success
           │
           ▼
      Kafka Event
           │
           ▼
    Order Confirmation
```

---

## Future Enhancements

* Payment refund support
* Payment retry mechanism
* Transaction reconciliation
* Additional payment gateways
* Improved payment failure handling
* Payment history APIs

---

## Tech stack used : 

Java | Spring Boot | Microservices | REST APIs | MySQL | Kafka | Docker

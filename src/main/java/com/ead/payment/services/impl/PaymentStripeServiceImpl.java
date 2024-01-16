package com.ead.payment.services.impl;

import com.ead.payment.enums.PaymentControl;
import com.ead.payment.models.CreditCardModel;
import com.ead.payment.models.PaymentModel;
import com.ead.payment.services.PaymentStripeService;
import com.stripe.Stripe;
import com.stripe.exception.CardException;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.param.PaymentIntentConfirmParams;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.PaymentMethodCreateParams;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Log4j2
public class PaymentStripeServiceImpl implements PaymentStripeService {

    @Value(value = "${ead.stripe.secretKey}")
    private String stripeSecretKey;

    @Override
    public PaymentModel processStripePayment(PaymentModel paymentModel, CreditCardModel creditCardModel) {
        Stripe.apiKey = stripeSecretKey;
        String paymentIntentId = null;
        try {
            // step 1: payment intent
            // //https://stripe.com/docs/api/payment_intents/create
//            List<Object> paymentMethodTypes = new ArrayList<>();
//            paymentMethodTypes.add("card");
//            Map<String, Object> paramsPaymentIntent = new HashMap<>();
//            paramsPaymentIntent.put("amount", paymentModel.getValuePaid().multiply(new BigDecimal("100")).longValue());
//            paramsPaymentIntent.put("currency", "brl");
//            paramsPaymentIntent.put("payment_method_types", paymentMethodTypes);
//            PaymentIntent paymentIntent = PaymentIntent.create(paramsPaymentIntent);
//            paymentIntentId = paymentIntent.getId();

            PaymentIntentCreateParams paramsPaymentIntent =
                    PaymentIntentCreateParams.builder()
                            .setAmount(paymentModel.getValuePaid().multiply(new BigDecimal("100")).longValue())
                            .setCurrency("brl")
                            .build();
            PaymentIntent paymentIntent = PaymentIntent.create(paramsPaymentIntent);
            log.info("Payment intent OK: {}", paymentIntent);

            paymentIntentId = paymentIntent.getId();

//          step 2: payment method
//          https://stripe.com/docs/api/payment_methods/create
//            Map<String, Object> card = new HashMap<>();
//            card.put("number", creditCardModel.getCreditCardNumber().replaceAll(" ", ""));
//            card.put("exp_month", creditCardModel.getExpirationDate().split("/")[0]);
//            card.put("exp_year", creditCardModel.getExpirationDate().split("/")[1]);
//            card.put("cvc", creditCardModel.getCvvCode());
//            Map<String, Object> paramsPaymentMethod = new HashMap<>();
//            paramsPaymentMethod.put("type", "card");
//            paramsPaymentMethod.put("card", card);
//            PaymentMethod paymentMethod = PaymentMethod.create(paramsPaymentMethod);

//            PaymentMethodCreateParams params =
//                    PaymentMethodCreateParams.builder()
//                            .setType(PaymentMethodCreateParams.Type.CARD)
//                            .setCard(
//                                    PaymentMethodCreateParams.CardDetails.builder()
//                                            .setNumber(creditCardModel.getCreditCardNumber().replaceAll(" ", ""))
//                                            .setExpMonth(Long.valueOf(creditCardModel.getExpirationDate().split("/")[0]))
//                                            .setExpYear(Long.valueOf(creditCardModel.getExpirationDate().split("/")[1]))
//                                            .setCvc(creditCardModel.getCvvCode())
//                                            .build()
//                            )
//                            .build();
//            PaymentMethod paymentMethod = PaymentMethod.create(params);
//            log.info("Payment method OK: {}", paymentMethod);

            //step 3: confirmation payment intent
            // https://stripe.com/docs/api/payment_intents/confirm
//            Map<String, Object> paramsPaymentConfirm = new HashMap<>();
//            paramsPaymentConfirm.put("payment_method", paymentMethod.getId());
//            PaymentIntent confirmPaymentIntent = paymentIntent.confirm(paramsPaymentConfirm);

            PaymentIntent resource = PaymentIntent.retrieve(paymentIntentId);
            PaymentIntentConfirmParams paramsPaymentConfirm =
                    PaymentIntentConfirmParams.builder()
                            .setPaymentMethod("pm_card_visa")
                            .build();
            PaymentIntent confirmPaymentIntent = resource.confirm(paramsPaymentConfirm);
            log.info("Confirm payment intent OK: {}", confirmPaymentIntent);

            if (confirmPaymentIntent.getStatus().equals("succeeded")) {
                paymentModel.setPaymentControl(PaymentControl.EFFECTED);
                paymentModel.setPaymentMessage("Payment effected - Payment intent: " + paymentIntentId);
                paymentModel.setPaymentCompletionDate(LocalDateTime.now(ZoneId.of("UTC")));
            } else {
                paymentModel.setPaymentControl(PaymentControl.ERROR);
                paymentModel.setPaymentMessage("Payment error - Payment intent: " + paymentIntentId);
            }
        } catch (CardException cardException) {
            log.error("A payment error occurred: {}", cardException.getMessage());
            try {
                paymentModel.setPaymentControl(PaymentControl.REFUSED);
                PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
                paymentModel.setPaymentMessage(
                        "Payment refused - Payment intent: " + paymentIntentId +
                        "caused by: " + paymentIntent.getLastPaymentError().getCode() +
                        "message: " + paymentIntent.getLastPaymentError().getMessage()
                );
            } catch (Exception e) {
                paymentModel.setPaymentMessage("Payment refused - Payment intent: " + paymentIntentId);
                log.error("Error. Might unrelated to Stripe: {}", e.getMessage());
            }
        } catch (Exception e) {
            paymentModel.setPaymentControl(PaymentControl.ERROR);
            paymentModel.setPaymentMessage("Payment error - Payment intent: " + paymentIntentId);
            log.error("Error. Might unrelated to Stripe: {}", e.getMessage());
        }
        return paymentModel;
    }
}

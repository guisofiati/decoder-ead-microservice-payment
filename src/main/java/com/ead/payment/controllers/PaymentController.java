package com.ead.payment.controllers;

import com.ead.payment.dtos.PaymentRequestDto;
import com.ead.payment.enums.PaymentControl;
import com.ead.payment.models.PaymentModel;
import com.ead.payment.models.UserModel;
import com.ead.payment.services.PaymentService;
import com.ead.payment.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class PaymentController {

    final UserService userService;
    final PaymentService paymentService;

    @PreAuthorize("hasAnyRole('USER')")
    @PostMapping("users/{userId}/payments")
    public ResponseEntity<Object> requestPayment(@PathVariable(value = "userId")UUID userId,
                                                 @RequestBody @Valid PaymentRequestDto paymentRequestDto) {
        Optional<UserModel> userModelOptional = userService.findById(userId);
        if (userModelOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        Optional<PaymentModel> paymentModelOptional = paymentService.findLastPaymentByUser(userModelOptional.get());
        if (paymentModelOptional.isPresent()) {
            var paymentModel = paymentModelOptional.get();
            if (paymentModel.getPaymentControl().equals(PaymentControl.REQUESTED)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Payment already requested");
            }
            if (paymentModel.getPaymentControl().equals(PaymentControl.EFFECTED) &&
                    paymentModel.getPaymentExpirationDate().isAfter(LocalDateTime.now(ZoneId.of("UTC")))) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Payment already effected");
            }
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(paymentService.requestPayment(paymentRequestDto, userModelOptional.get()));
    }
}

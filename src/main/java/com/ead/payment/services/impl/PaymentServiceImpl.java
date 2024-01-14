package com.ead.payment.services.impl;

import com.ead.payment.dtos.PaymentCommandDto;
import com.ead.payment.dtos.PaymentRequestDto;
import com.ead.payment.enums.PaymentControl;
import com.ead.payment.models.CreditCardModel;
import com.ead.payment.models.PaymentModel;
import com.ead.payment.models.UserModel;
import com.ead.payment.publishers.PaymentCommandPublisher;
import com.ead.payment.repositories.CreditCardRepository;
import com.ead.payment.repositories.PaymentRepository;
import com.ead.payment.services.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
public class PaymentServiceImpl implements PaymentService {

    final CreditCardRepository creditCardRepository;
    final PaymentRepository paymentRepository;
    final PaymentCommandPublisher paymentCommandPublisher;

    @Transactional
    @Override
    public PaymentModel requestPayment(PaymentRequestDto paymentRequestDto, UserModel userModel) {
        var creditCardModel = new CreditCardModel();
        var creditCardModelOptional = creditCardRepository.findByUser(userModel);

        if (creditCardModelOptional.isPresent()) {
            creditCardModel = creditCardModelOptional.get();
        }

        BeanUtils.copyProperties(paymentRequestDto, creditCardModel);
        creditCardModel.setUser(userModel);
        creditCardRepository.save(creditCardModel);

        var paymentModel = new PaymentModel();
        paymentModel.setPaymentControl(PaymentControl.REQUESTED);
        paymentModel.setPaymentRequestDate(LocalDateTime.now(ZoneId.of("UTC")));
        paymentModel.setPaymentExpirationDate(LocalDateTime.now(ZoneId.of("UTC")).plusDays(30));
        paymentModel.setLastDigitsCreditCard(paymentRequestDto.getCreditCardNumber().substring(paymentRequestDto.getCreditCardNumber().length() - 4));
        paymentModel.setValuePaid(paymentRequestDto.getValuePaid());
        paymentModel.setUser(userModel);
        paymentRepository.save(paymentModel);

        try {
            var paymentCommandDto = new PaymentCommandDto();
            paymentCommandDto.setUserId(userModel.getUserId());
            paymentCommandDto.setPaymentId(paymentModel.getPaymentId());
            paymentCommandDto.setCardId(creditCardModel.getCardId());
            paymentCommandPublisher.publishPaymentCommand(paymentCommandDto);
            log.debug("payment command sent successfully: {}", paymentCommandDto);
        } catch (Exception e) {
            log.warn("Error sending payment command: {}", e.getMessage());
        }

        return paymentModel;
    }

    @Override
    public Optional<PaymentModel> findLastPaymentByUser(UserModel userModel) {
        return paymentRepository.findTopByUserOrderByPaymentRequestDateDesc(userModel);
    }

    @Override
    public Page<PaymentModel> findAllByUser(Specification<PaymentModel> spec, Pageable pageable) {
        return paymentRepository.findAll(spec, pageable);
    }

    @Override
    public Optional<PaymentModel> findPaymentByUser(UUID userId, UUID paymentId) {
        return paymentRepository.findPaymentByUser(userId, paymentId);
    }
}

package com.example.workflow.delegates;


import com.example.workflow.dto.VatDto;
import com.example.workflow.exceptions.OtpExceedingException;
import com.example.workflow.model.OtpGeneration;
import com.example.workflow.repository.OtpGenerationRepository;
import com.example.workflow.services.AdminConfigRollsProvider;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;
import java.util.logging.Logger;

@Component
public class GenerateOTPDelegate implements JavaDelegate {
    private final Logger logger = Logger.getLogger(GenerateOTPDelegate.class.getName());
    @Autowired
    AdminConfigRollsProvider rollsProvider;
    @Autowired
    OtpGenerationRepository otpGenerationRepository;

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {

        String vat =(String) delegateExecution.getVariable("vat_reg_number");
        String maxNumOfOtpMultiDevices = "100";
        Integer maxNoOtp = Integer.valueOf(maxNumOfOtpMultiDevices);
        String num=(String)delegateExecution.getVariable("otp-number");
        int no=Integer.parseInt(num);

        logger.info("Creating a number of {} :  new otps." + no  );
        if (no > maxNoOtp)
            throw new OtpExceedingException("Otp requests exceeding the configured maximum number otp, limit = [" + maxNoOtp + "].");
        List<String> otps = new ArrayList<>();
        int otpLength =Integer.parseInt("6") ;

        for (int i = 1; i <= no; i++) {

            String otp = makeRandomOtp(otpLength);

            Date otpCreationDate = new Date();

            OtpGeneration otpGeneration = new OtpGeneration();
            otpGeneration.setOtp(otp);
            otpGeneration.setOtpCreationDate(otpCreationDate);
            otpGeneration.setVatNumber(vat);
            otpGeneration.setStatus(true);

            otpGenerationRepository.save(otpGeneration);
            otps.add(otp);
            logger.info("Otp created {} : at time {} :" + otp +  otpCreationDate);

        }
        delegateExecution.setVariable("otps",otps);


    }
    private String makeRandomOtp(int length) throws NoSuchAlgorithmException {
        StringBuilder builder = new StringBuilder("");
        Random random = SecureRandom.getInstanceStrong();

        for(int i = 1; i <= length; i ++) {
            int value = random.nextInt(9 + 0);
            builder.append(value);
        }

        return builder.toString();
    }
}

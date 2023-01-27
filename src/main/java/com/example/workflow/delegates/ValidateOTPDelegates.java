package com.example.workflow.delegates;

import com.example.workflow.dto.CommonConstant;
import com.example.workflow.dto.ValidationResponse;
import com.example.workflow.dto.Error;
import com.example.workflow.exceptions.TimePeriodException;
import com.example.workflow.model.OtpGeneration;
import com.example.workflow.repository.OtpGenerationRepository;
import com.owlike.genson.Genson;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import spinjar.com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Component
public class ValidateOTPDelegates implements JavaDelegate {
    private final Logger logger = Logger.getLogger(ValidateOTPDelegates.class.getName());
    @Value("${OTP_VALIDITY}")
    private double otpValidity;
    @Autowired
    OtpGenerationRepository otpGenerationRepository;
    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        ValidationResponse validationResponse=new ValidationResponse();

        String otp=(String) delegateExecution.getVariable("v_otp");
        String vat=(String) delegateExecution.getVariable("vatNumber");

        logger.info("Validating Otp {} :   and Vat {} :" +otp +vat);

        OtpGeneration otpGeneration = otpGenerationRepository.findByOtpAndVat(otp, vat);


        if (otpGeneration == null) {
            logger.info("invalid");
            validationResponse= createValidationResponse(CommonConstant.NOT_VALID, CommonConstant.EXISTENCE_ERROR_CODE,
                    CommonConstant.EXISTENCE_ERROR_CATEGORY, CommonConstant.VAT_EXISTENCE_ERROR_MESSAGE);
        }

        otpGeneration.setStatus(false);
        otpGenerationRepository.save(otpGeneration);

        List<OtpGeneration> existedOtps = otpGenerationRepository.findByOtpByOtpCreationDateDesc(otp);

        if (existedOtps.isEmpty()) {
            delegateExecution.setVariable("valid",false);
            logger.info("invalid");
            validationResponse= createValidationResponse(CommonConstant.NOT_VALID, CommonConstant.EXISTENCE_ERROR_CODE, CommonConstant.EXISTENCE_ERROR_CATEGORY, CommonConstant.OTP_EXISTENCE_ERROR_MESSAGE);
        }

        long creationDate = existedOtps.get(0).getOtpCreationDate().getTime();
        long currentDate = new Date().getTime();
        long different = currentDate - creationDate;
        double elapsedMinutes = different /otpValidity;


        String timePeriod = "Minutes";
        String value = "15";
        Integer valueO = Integer.valueOf(value);
        long  configuredMinutes=getConfiguredMinutes(timePeriod,valueO);

        if (elapsedMinutes > configuredMinutes) {
            delegateExecution.setVariable("valid",false);
            logger.info("invalid");

            validationResponse= createValidationResponse(CommonConstant.NOT_VALID, CommonConstant.EXPIRATION_ERROR_CODE, CommonConstant.EXPIRATION_ERROR_CATEGORY, CommonConstant.OTP_EXPIRATION_ERROR_MESSAGE);
        }
        else {

            validationResponse =createValidationResponse(CommonConstant.VALID, null, null, null);}
        delegateExecution.setVariable("valid",false);
//        Genson genson = new Genson.Builder().useClassMetadata(true).create();
//
//        String result = genson.serialize(validationResponse);


        logger.info("valid");

        ObjectMapper mapper = new ObjectMapper();
        String result = mapper.writeValueAsString(validationResponse);
        delegateExecution.setVariable("validationResult",result);

    }
    private ValidationResponse createValidationResponse(String result, String errorCode, String errorCategory, String errorMessage) {

        ValidationResponse validationResponse = new ValidationResponse();

        validationResponse.setResult(result);


        Error[] errors=new Error[1];

        Error error = new Error();
        error.setErrorCode(errorCode);
        error.setErrorCategory(errorCategory);
        error.setErrorMessage(errorMessage);

        errors[0] = error;
        validationResponse.setError(errors);

        return validationResponse;
    }
    private long getConfiguredMinutes(String timePeriod, int value){
        if(timePeriod.equalsIgnoreCase("Days")){
            return (long) value * 24 * 60;
        }
        else if(timePeriod.equalsIgnoreCase("Hours")){
            return (long) value * 60 ;
        }
        else if(timePeriod.equalsIgnoreCase("Minutes")){
            return value;
        }
        else if(timePeriod.equalsIgnoreCase("seconds")){
            return (long) value/60 ;
        }
        else{
            throw new TimePeriodException("there is no such Period : " +timePeriod);
        }
    }


}

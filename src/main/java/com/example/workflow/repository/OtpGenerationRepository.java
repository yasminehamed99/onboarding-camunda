package com.example.workflow.repository;

import com.example.workflow.model.OtpGeneration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OtpGenerationRepository extends JpaRepository<OtpGeneration,Long> {

    @Query("SELECT o FROM OtpGeneration o WHERE o.otp =:otp order by o.otpCreationDate desc")
    List<OtpGeneration> findByOtpByOtpCreationDateDesc(@Param("otp") String otp);

    @Query("SELECT og FROM OtpGeneration og WHERE og.otp =:otp AND og.vatNumber =:vat AND og.status = true")
    OtpGeneration findByOtpAndVat(@Param("otp") String otp,@Param("vat") String vat);

    List<OtpGeneration> findByOtp(String otp);

    int countByVatNumber(String vat);

}

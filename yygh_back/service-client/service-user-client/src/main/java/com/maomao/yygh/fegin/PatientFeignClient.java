package com.maomao.yygh.fegin;

import com.maomao.yygh.model.user.Patient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author starsea
 * @date 2022-02-06
 */
@FeignClient(value = "service-user")
@Repository
public interface PatientFeignClient {

    /**
     * 获取就诊人
     * @param id
     * @return
     */
    @GetMapping("/api/user/patient/inner/get/{id}")
//    Patient getPatient(@PathVariable("id") Long id);
    Patient getPatientOrder(@PathVariable("id") Long id);
}

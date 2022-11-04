package com.maomao.yygh.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maomao.yygh.cmn.client.DictFeignClient;
import com.maomao.yygh.enums.DictEnum;
import com.maomao.yygh.model.user.Patient;
import com.maomao.yygh.user.mapper.PatientMapper;
import com.maomao.yygh.user.service.PatientService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PatientServiceImpl extends ServiceImpl<PatientMapper, Patient> implements PatientService {
    @Autowired
    private PatientMapper patientMapper;
    @Autowired
    private DictFeignClient dictFeignClient;
    //获取就诊人列表
    @Override
    public List<Patient> findAllUserId(Long userId) {
        //根据userid查询所有就诊人信息列表
        QueryWrapper<Patient> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        List<Patient> patients = patientMapper.selectList(wrapper);
        //添加额外信息(通过远程调用，查找字典表数据)
        patients.stream().forEach(item->{
            this.packPatient(item);
        });
        return patients;
    }
    //根据id获取就诊人信息
    @Override
    public Patient getPatientId(Long id) {
        Patient patient = patientMapper.selectById(id);
        return this.packPatient(patient);
    }

    //其他参数封装
    private Patient packPatient(Patient patient) {
        // 根据证件类型编码，获取证件类型具体指
        String certificatesTypeString =
                dictFeignClient.getName(DictEnum.CERTIFICATES_TYPE.getDictCode(), patient.getCertificatesType());//联系人证件
        // 联系人证件类型
        String contactsCertificatesTypeString =
                dictFeignClient.getName(DictEnum.CERTIFICATES_TYPE.getDictCode(),patient.getContactsCertificatesType());
        // 省
        String provinceString = dictFeignClient.getName(patient.getProvinceCode());
        // 市
        String cityString = dictFeignClient.getName(patient.getCityCode());
        // 区
        String districtString = dictFeignClient.getName(patient.getDistrictCode());
        patient.getParam().put("certificatesTypeString", certificatesTypeString);
        patient.getParam().put("contactsCertificatesTypeString", contactsCertificatesTypeString);
        patient.getParam().put("provinceString", provinceString);
        patient.getParam().put("cityString", cityString);
        patient.getParam().put("districtString", districtString);
        patient.getParam().put("fullAddress", provinceString + cityString + districtString + patient.getAddress());
        return patient;
    }
}

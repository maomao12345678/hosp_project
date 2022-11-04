package com.maomao.yygh.cmn.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.maomao.yygh.model.cmn.Dict;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface DictService extends IService<Dict> {
    //根据数据id查询子数据列表
    List<Dict> findChildData(Long id);
    //导出数据字典接口
    void exportDictData(HttpServletResponse httpServletResponse);
    //导入数据字典数据
    void importData(MultipartFile file);
    //根据ditcode和value查询数据
    String getDictName(String dictCode, String value);
    //获得子级
    List<Dict> findByDictCode(String dictCode);

}

package com.maomao.yygh.cmn.controller;

import com.maomao.yygh.model.cmn.Dict;
import com.maomao.yygh.cmn.service.DictService;
import com.maomao.yygh.common.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Api(value = "数据字典的接口")
@RestController
@RequestMapping("/admin/cmn/dict")
//@CrossOrigin
public class DictController {
    @Autowired
    private DictService dictService;

    //根据数据id查询子数据列表
    @ApiOperation(value = "根据数据id查询子数据列表")
    @GetMapping("findChildData/{id}")
    public Result findChildData(@PathVariable Long id){
        List<Dict> list = dictService.findChildData(id);
        return Result.ok(list);
    }

    //导出数据字典接口信息
    @GetMapping("exportData")
    public void exportData(HttpServletResponse httpServletResponse){
        dictService.exportDictData(httpServletResponse);
    }
    //导入数据字典数据
    @PostMapping("importData")
    public Result importData(MultipartFile file){
        dictService.importData(file);
        return Result.ok();
    }

    //根据ditcode和value查询数据
    @GetMapping("getName/{dictCode}/{value}")
    public String getName(@PathVariable("dictCode") String dictCode,
                          @PathVariable("value") String value){
        String dictName = dictService.getDictName(dictCode, value);
        return dictName;
    }
    //根据value查询数据
    @GetMapping("getName/{value}")
    public String getName(@PathVariable("value") String value){
        //只根据value返回值，所以签名dictcode的位置为""即可
        String dictName = dictService.getDictName("", value);
        return dictName;
    }
    //根据dictCode查询下级节点
    @ApiOperation(value = "根据dictCode获取下级节点")
    @GetMapping("findByDictCode/{dictCode}")
    public Result findByDictCode(@PathVariable String dictCode){
        List<Dict> list = dictService.findByDictCode(dictCode);
        return Result.ok(list);
    }

}

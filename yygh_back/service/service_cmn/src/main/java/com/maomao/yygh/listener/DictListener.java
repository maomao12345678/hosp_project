package com.maomao.yygh.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.maomao.yygh.model.cmn.Dict;
import com.maomao.yygh.vo.cmn.DictEeVo;
import com.maomao.yygh.cmn.mapper.DictMapper;
import org.springframework.beans.BeanUtils;
//excel的监听器
public class DictListener extends AnalysisEventListener<DictEeVo> {

    private DictMapper dictMapper;
    public DictListener(DictMapper dictMapper) {
        this.dictMapper = dictMapper;
    }

    //每一行进行读取，从第二行开始
    @Override
    public void invoke(DictEeVo dictEeVo, AnalysisContext analysisContext) {
        //调用方法添加到数据库中
        Dict dict = new Dict();
        BeanUtils.copyProperties(dictEeVo, dict);
        //导入表格的时候插入值
        dictMapper.insert(dict);
    }
    //读完之后的操作
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }
}

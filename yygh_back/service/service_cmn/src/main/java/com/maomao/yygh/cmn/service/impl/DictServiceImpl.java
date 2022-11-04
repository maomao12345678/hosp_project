package com.maomao.yygh.cmn.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maomao.yygh.model.cmn.Dict;
import com.maomao.yygh.vo.cmn.DictEeVo;
import com.maomao.yygh.cmn.mapper.DictMapper;
import com.maomao.yygh.cmn.service.DictService;
import com.maomao.yygh.listener.DictListener;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {
    @Autowired
    private DictMapper dictMapper;

    //根据数据id查询子数据列表
    @Override
    //设置使用缓存,把dictList放入缓存中
    @Cacheable(value = "dict", keyGenerator = "keyGenerator")
    public List<Dict> findChildData(Long id) {
        //找出父id为当前id的所有值
        QueryWrapper<Dict> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_id", id);
        List<Dict> dictList = dictMapper.selectList(wrapper);
        //想list中设置每个dict的hasChildren值
        for(Dict dict: dictList){
            Long dictId = dict.getId();
            //看每一个dict中是否有子字典
            boolean isChild = isChildren(dictId);
            dict.setHasChildren(isChild);
        }
        return dictList;
    }
    //判断id下面是否有子节点
    private boolean isChildren(Long id){
        QueryWrapper<Dict> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_id", id);
        Integer count = dictMapper.selectCount(wrapper);
        //如果count为0就是没有子节点，如果大于0就是有子节点
        return count>0;
    }
    //导出数据字典接口
    @Override
    public void exportDictData(HttpServletResponse httpServletResponse) {
        //设置下载信息
        httpServletResponse.setContentType("application/vnd.ms-excel");
        httpServletResponse.setCharacterEncoding("utf-8");
        //防止中文乱码
        String fileName = "dict";
        //Content-disposition以下载的方式打开
        httpServletResponse.setHeader("Content-disposition", "attachment:filename="+fileName);
        //查询数据库(查询字典的所有数据)
        List<Dict> dictList = dictMapper.selectList(null);
        //把Dict对象转换成DictEeVo对象
        List<DictEeVo> dictEeVoList = new ArrayList<>();
        //把dict转换成dictEeVo
        for(Dict dict: dictList){
            DictEeVo dictEeVo = new DictEeVo();
            //把dict与dictEeVo的数据进行对应
            BeanUtils.copyProperties(dict, dictEeVo);
            dictEeVoList.add(dictEeVo);
        }
        //调用方法进行表格写操作
        try {
            EasyExcel.write(httpServletResponse.getOutputStream(), DictEeVo.class).sheet("dict").doWrite(dictEeVoList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //导入数据字典数据
    //数据改变时就情况dict规则中的缓存内容
    @CacheEvict(value = "dict", allEntries = true)
    @Override
    public void importData(MultipartFile file) {
        try {
            //读取表格数据到数据库中,然后显示在数据字典中(设置一个dict的监听者)
            EasyExcel.read(file.getInputStream(), DictEeVo.class, new DictListener(dictMapper)).sheet().doRead();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //根据ditcode和value查询数据返回名字
    @Override
    public String getDictName(String dictCode, String value) {
        //如果dictCode为空
        if(StringUtils.isEmpty(dictCode)){
            //直接根据value查询
            QueryWrapper<Dict> wrapper = new QueryWrapper<>();
            wrapper.eq("value", value);
            Dict dict = dictMapper.selectOne(wrapper);
            return dict.getName();
        }else{//如果dictCode不为空
            //根据数据库表的结构,根据dictcode查询dict对象,查询id值
            Dict codeDict = this.getDictByDictCode(dictCode);
            Long parent_id = codeDict.getId();
            //根据parent_id和value进行查询
            Dict finalDict = dictMapper.selectOne(new QueryWrapper<Dict>()
                    .eq("parent_id", parent_id)
                    .eq("value", value));
            return finalDict.getName();
        }
    }
    //获取下级
    @Override
    public List<Dict> findByDictCode(String dictCode) {
        //根据DictCode获取对应id
        Dict dict = this.getDictByDictCode(dictCode);
        //根据id获取子节点
        List<Dict> childData = this.findChildData(dict.getId());
        return childData;
    }

    //根据dictCode查询dict对象,查询id值
    private Dict getDictByDictCode(String dictCode){
        QueryWrapper<Dict> wrapper = new QueryWrapper<>();
        wrapper.eq("dict_code", dictCode);
        Dict codeDict = dictMapper.selectOne(wrapper);
        return codeDict;
    }

}

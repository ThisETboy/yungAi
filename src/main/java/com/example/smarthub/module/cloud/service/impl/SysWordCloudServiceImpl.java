package com.example.smarthub.module.cloud.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.smarthub.common.exception.BizException;
import com.example.smarthub.module.cloud.dto.WordCloudRequest;
import com.example.smarthub.module.cloud.entity.SysWordCloud;
import com.example.smarthub.module.cloud.mapper.SysWordCloudMapper;
import com.example.smarthub.module.cloud.service.SysWordCloudService;
import com.example.smarthub.module.cloud.vo.WordCloudVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 词云服务实现
 */
@Service
@RequiredArgsConstructor
public class SysWordCloudServiceImpl implements SysWordCloudService {

    private final SysWordCloudMapper sysWordCloudMapper;

    @Override
    public List<WordCloudVO> getWordCloudList(String category) {
        LambdaQueryWrapper<SysWordCloud> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysWordCloud::getStatus, 1)
               .eq(category != null && !category.isEmpty(), SysWordCloud::getCategory, category)
               .orderByDesc(SysWordCloud::getPopularity);
        return convertToVO(sysWordCloudMapper.selectList(wrapper));
    }

    @Override
    public List<String> getCategories() {
        LambdaQueryWrapper<SysWordCloud> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(SysWordCloud::getCategory)
               .eq(SysWordCloud::getStatus, 1)
               .groupBy(SysWordCloud::getCategory);
        return sysWordCloudMapper.selectObjs(wrapper).stream()
                .map(obj -> (String) obj)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public IPage<WordCloudVO> pageWords(int current, int size, String category, String keyword) {
        Page<SysWordCloud> page = new Page<>(current, size);
        LambdaQueryWrapper<SysWordCloud> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(keyword != null && !keyword.isEmpty(), SysWordCloud::getWord, keyword)
               .eq(category != null && !category.isEmpty(), SysWordCloud::getCategory, category)
               .orderByDesc(SysWordCloud::getPopularity);
        return sysWordCloudMapper.selectPage(page, wrapper).convert(item -> {
            WordCloudVO vo = new WordCloudVO();
            vo.setId(item.getId());
            vo.setWord(item.getWord());
            vo.setCategory(item.getCategory());
            vo.setPopularity(item.getPopularity());
            vo.setColor(item.getColor());
            vo.setSource(item.getSource());
            return vo;
        });
    }

    @Override
    public void saveWord(WordCloudRequest request) {
        if (request.getId() != null) {
            SysWordCloud existing = sysWordCloudMapper.selectById(request.getId());
            BizException.throwIfNull(existing, "热词不存在");
            copyToRequest(request, existing);
            sysWordCloudMapper.updateById(existing);
        } else {
            SysWordCloud entity = new SysWordCloud();
            copyToRequest(request, entity);
            sysWordCloudMapper.insert(entity);
        }
    }

    @Override
    public void deleteWord(Long id) {
        SysWordCloud existing = sysWordCloudMapper.selectById(id);
        BizException.throwIfNull(existing, "热词不存在");
        sysWordCloudMapper.deleteById(id);
    }

    private List<WordCloudVO> convertToVO(List<SysWordCloud> list) {
        return list.stream().map(item -> {
            WordCloudVO vo = new WordCloudVO();
            vo.setId(item.getId());
            vo.setWord(item.getWord());
            vo.setCategory(item.getCategory());
            vo.setPopularity(item.getPopularity());
            vo.setColor(item.getColor());
            vo.setSource(item.getSource());
            return vo;
        }).collect(Collectors.toList());
    }

    private void copyToRequest(WordCloudRequest req, SysWordCloud entity) {
        entity.setWord(req.getWord());
        entity.setCategory(req.getCategory());
        entity.setPopularity(req.getPopularity());
        entity.setColor(req.getColor());
        entity.setSource(req.getSource());
        entity.setSourceText(req.getSourceText());
        entity.setStatus(req.getStatus() != null ? req.getStatus() : 1);
        entity.setSortOrder(req.getSortOrder() != null ? req.getSortOrder() : 0);
    }
}

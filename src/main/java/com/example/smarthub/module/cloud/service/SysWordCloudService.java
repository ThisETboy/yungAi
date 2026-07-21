package com.example.smarthub.module.cloud.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.smarthub.module.cloud.dto.WordCloudRequest;
import com.example.smarthub.module.cloud.vo.WordCloudVO;

import java.util.List;

/**
 * 词云服务接口
 */
public interface SysWordCloudService {

    /**
     * 获取词云数据（用于前端渲染）
     * @param category 分类过滤（可选）
     * @return 词云数据列表
     */
    List<WordCloudVO> getWordCloudList(String category);

    /**
     * 获取所有分类列表
     * @return 分类列表
     */
    List<String> getCategories();

    /**
     * 分页查询热词列表
     * @param current 页码
     * @param size 每页大小
     * @param category 分类过滤
     * @param keyword 关键词搜索
     * @return 分页结果
     */
    IPage<WordCloudVO> pageWords(int current, int size, String category, String keyword);

    /**
     * 新增或编辑热词
     * @param request 请求参数
     */
    void saveWord(WordCloudRequest request);

    /**
     * 删除热词
     * @param id 热词ID
     */
    void deleteWord(Long id);
}

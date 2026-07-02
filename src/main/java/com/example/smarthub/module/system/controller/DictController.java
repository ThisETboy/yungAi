package com.example.smarthub.module.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.smarthub.common.response.R;
import com.example.smarthub.module.system.entity.DictData;
import com.example.smarthub.module.system.entity.DictType;
import com.example.smarthub.module.system.mapper.DictDataMapper;
import com.example.smarthub.module.system.mapper.DictTypeMapper;
import com.example.smarthub.module.system.service.DictDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 字典管理控制器
 *
 * 提供字典类型和字典数据的 CRUD 接口
 */
@RestController
@RequestMapping("/api/dict")
@RequiredArgsConstructor
@Tag(name = "数据字典")
public class DictController {

    private final DictTypeMapper dictTypeMapper;
    private final DictDataMapper dictDataMapper;
    private final DictDataService dictDataService;

    /**
     * 分页查询字典类型
     */
    @GetMapping("/types")
    @Operation(summary = "字典类型分页列表")
    @PreAuthorize("hasAuthority('sys:dict:type:list')")
    public R<IPage<DictType>> pageTypes(@RequestParam(defaultValue = "1") int current,
                                        @RequestParam(defaultValue = "10") int size,
                                        @RequestParam(required = false) String dictName) {
        Page<DictType> page = new Page<>(current, size);
        LambdaQueryWrapper<DictType> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(dictName != null && !dictName.isEmpty(), DictType::getDictName, dictName)
               .orderByDesc(DictType::getId);
        return R.ok(dictTypeMapper.selectPage(page, wrapper));
    }

    /**
     * 根据 ID 查询字典类型
     */
    @GetMapping("/types/{id}")
    @Operation(summary = "字典类型详情")
    @PreAuthorize("hasAuthority('sys:dict:type:list')")
    public R<DictType> getTypeById(@PathVariable Long id) {
        return R.ok(dictTypeMapper.selectById(id));
    }

    /**
     * 创建或更新字典类型
     */
    @PostMapping("/types")
    @Operation(summary = "创建/更新字典类型")
    @PreAuthorize("hasAuthority('sys:dict:type:edit')")
    public R<Void> saveType(@Valid @RequestBody DictType dictType) {
        if (dictType.getId() != null) {
            dictTypeMapper.updateById(dictType);
        } else {
            dictTypeMapper.insert(dictType);
        }
        return R.ok();
    }

    /**
     * 删除字典类型
     */
    @DeleteMapping("/types/{id}")
    @Operation(summary = "删除字典类型")
    @PreAuthorize("hasAuthority('sys:dict:type:delete')")
    public R<Void> deleteType(@PathVariable Long id) {
        dictTypeMapper.deleteById(id);
        return R.ok();
    }

    /**
     * 根据字典类型查询数据列表
     */
    @GetMapping("/data/{dictType}")
    @Operation(summary = "根据字典类型查询数据")
    public R<List<DictData>> getDataByType(@PathVariable String dictType) {
        return R.ok(dictDataService.getByDictType(dictType));
    }

    /**
     * 分页查询字典数据
     */
    @GetMapping("/data")
    @Operation(summary = "字典数据分页列表")
    @PreAuthorize("hasAuthority('sys:dict:data:list')")
    public R<IPage<DictData>> pageData(@RequestParam(defaultValue = "1") int current,
                                       @RequestParam(defaultValue = "10") int size,
                                       @RequestParam Long dictTypeId,
                                       @RequestParam(required = false) String dictLabel) {
        Page<DictData> page = new Page<>(current, size);
        LambdaQueryWrapper<DictData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DictData::getDictTypeId, dictTypeId)
               .like(dictLabel != null && !dictLabel.isEmpty(), DictData::getDictLabel, dictLabel)
               .orderByAsc(DictData::getDictSort);
        return R.ok(dictDataMapper.selectPage(page, wrapper));
    }

    /**
     * 创建或更新字典数据
     */
    @PostMapping("/data")
    @Operation(summary = "创建/更新字典数据")
    @PreAuthorize("hasAuthority('sys:dict:data:edit')")
    public R<Void> saveData(@Valid @RequestBody DictData dictData) {
        if (dictData.getId() != null) {
            dictDataMapper.updateById(dictData);
        } else {
            dictDataMapper.insert(dictData);
        }
        return R.ok();
    }

    /**
     * 删除字典数据
     */
    @DeleteMapping("/data/{id}")
    @Operation(summary = "删除字典数据")
    @PreAuthorize("hasAuthority('sys:dict:data:delete')")
    public R<Void> deleteData(@PathVariable Long id) {
        dictDataMapper.deleteById(id);
        return R.ok();
    }
}

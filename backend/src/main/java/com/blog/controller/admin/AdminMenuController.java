package com.blog.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.common.result.Result;
import com.blog.entity.Menu;
import com.blog.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "菜单管理")
@RestController
@RequestMapping("/api/admin/menus")
@RequiredArgsConstructor
public class AdminMenuController {

    private final MenuService menuService;

    @Operation(summary = "菜单列表")
    @GetMapping
    public Result<?> list() {
        LambdaQueryWrapper<Menu> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Menu::getSort);
        List<Menu> menus = menuService.list(wrapper);
        return Result.success(buildTree(menus));
    }

    @Operation(summary = "菜单树")
    @GetMapping("/tree")
    public Result<?> tree() {
        LambdaQueryWrapper<Menu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Menu::getStatus, 1).orderByAsc(Menu::getSort);
        List<Menu> menus = menuService.list(wrapper);
        return Result.success(buildTree(menus));
    }

    @Operation(summary = "创建菜单")
    @PostMapping
    public Result<?> create(@RequestBody Menu menu) {
        menu.setStatus(1);
        menu.setVisible(1);
        menuService.save(menu);
        return Result.success("创建成功");
    }

    @Operation(summary = "更新菜单")
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody Menu menu) {
        menu.setId(id);
        menuService.updateById(menu);
        return Result.success("更新成功");
    }

    @Operation(summary = "删除菜单")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        // 检查是否有子菜单
        LambdaQueryWrapper<Menu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Menu::getParentId, id);
        if (menuService.count(wrapper) > 0) {
            return Result.error("存在子菜单，无法删除");
        }
        menuService.removeById(id);
        return Result.success("删除成功");
    }

    private List<Map<String, Object>> buildTree(List<Menu> menus) {
        Map<Long, List<Menu>> parentMap = menus.stream()
                .collect(Collectors.groupingBy(m -> m.getParentId() == null ? 0L : m.getParentId()));
        
        return buildChildren(parentMap, 0L);
    }

    private List<Map<String, Object>> buildChildren(Map<Long, List<Menu>> parentMap, Long parentId) {
        List<Menu> children = parentMap.getOrDefault(parentId, new ArrayList<>());
        return children.stream().map(menu -> {
            Map<String, Object> node = new java.util.HashMap<>();
            node.put("id", menu.getId());
            node.put("parentId", menu.getParentId());
            node.put("menuName", menu.getMenuName());
            node.put("menuType", menu.getMenuType());
            node.put("path", menu.getPath());
            node.put("component", menu.getComponent());
            node.put("perms", menu.getPerms());
            node.put("icon", menu.getIcon());
            node.put("sort", menu.getSort());
            node.put("visible", menu.getVisible());
            node.put("status", menu.getStatus());
            
            List<Map<String, Object>> childNodes = buildChildren(parentMap, menu.getId());
            if (!childNodes.isEmpty()) {
                node.put("children", childNodes);
            }
            return node;
        }).collect(Collectors.toList());
    }
}

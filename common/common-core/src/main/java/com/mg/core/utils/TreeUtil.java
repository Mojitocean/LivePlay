package com.mg.core.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 通用树结构构建工具类
 */
public class TreeUtil {

    /**
     * 构建树结构
     *
     * @param list           原始列表数据
     * @param idGetter       获取当前节点的 ID
     * @param pidGetter      获取当前节点的父级 ID
     * @param childrenSetter 设置子节点的方法（通常是 setChildren）
     * @param <T>            节点对象类型
     * @param <K>            ID 类型
     * @return 树形结构的列表
     */
    public static <T, K> List<T> buildTree(List<T> list, Function<T, K> idGetter, Function<T, K> pidGetter, BiConsumer<T, List<T>> childrenSetter) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }

        // 将原始列表转换为 Map，key 为 ID，value 为节点对象
        Map<K, T> nodeMap = list.stream().collect(Collectors.toMap(idGetter, Function.identity(), (a, b) -> a));

        List<T> treeList = new ArrayList<>();

        for (T node : list) {
            K pid = pidGetter.apply(node);
            if (pid == null || !nodeMap.containsKey(pid) || pid.equals("0")) {
                // 父 ID 不存在，表示是顶级节点
                treeList.add(node);
            } else {
                // 将当前节点添加到其父节点的 children 中
                T parent = nodeMap.get(pid);
                List<T> children = getOrInitChildren(parent, childrenSetter);
                children.add(node);
            }
        }

        return treeList;
    }

    /**
     * 尝试获取 parent 节点的 children 列表，如果为空则初始化
     *
     * @param node           节点
     * @param childrenSetter 设置子节点的函数
     * @param <T>            节点类型
     * @return children 列表
     */
    private static <T> List<T> getOrInitChildren(T node, BiConsumer<T, List<T>> childrenSetter) {
        try {
            Field childrenField = findChildrenField(node.getClass());
            childrenField.setAccessible(true);
            @SuppressWarnings("unchecked") List<T> children = (List<T>) childrenField.get(node);
            if (children == null) {
                children = new ArrayList<>();
                childrenSetter.accept(node, children);
            }
            return children;
        } catch (Exception e) {
            throw new RuntimeException("获取 children 字段失败：" + e.getMessage(), e);
        }
    }

    /**
     * 向上递归查找名为 "children" 的字段
     *
     * @param clazz 当前类
     * @return children 字段
     * @throws NoSuchFieldException 若未找到字段
     */
    private static Field findChildrenField(Class<?> clazz) throws NoSuchFieldException {
        for (Field field : clazz.getDeclaredFields()) {
            if ("children".equals(field.getName())) {
                return field;
            }
        }
        // 递归查找父类
        Class<?> superClass = clazz.getSuperclass();
        if (superClass != null) {
            return findChildrenField(superClass);
        }
        throw new NoSuchFieldException("未找到 children 字段");
    }

    /**
     * 函数式接口：设置子节点
     */
    @FunctionalInterface
    public interface BiConsumer<T, U> {
        void accept(T t, U u);
    }
}
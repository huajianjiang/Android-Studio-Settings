package com.jhj.expandablerecyclerview.model;

import java.util.List;

/**
 * 父列表项接口，客户端父列表项数据需要实现该基类实现自定义的父列表项数据模型
 * Created by jhj_Plus on 2015/12/23.
 */
public interface ParentItem<CI> {
    /**
     * 获取属于该父列表项的子项列表
     *
     * @return 所属该父列表项的子项列表
     */
    List<CI> getChildItems();

    /**
     * 单独设置该父列表项是否可以展开折叠,不影响 {@link #isInitiallyExpanded()}，但是后期无法再展开折叠，除非再次设置该返回值
     * @return 该父父列表项是否可展开折叠
     */
    boolean isExpandable();

    /**
     * 父列表项初始化时是否展开回调
     *
     * @return 父列表项初始化时是否展开
     */
    boolean isInitiallyExpanded();
}

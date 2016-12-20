package com.jhj.expandablerecyclerview.adapter;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.jhj.expandablerecyclerview.viewholder.BaseViewHolder;
import com.jhj.expandablerecyclerview.viewholder.ChildViewHolder;
import com.jhj.expandablerecyclerview.viewholder.OnParentItemExpandCollapseListener;
import com.jhj.expandablerecyclerview.viewholder.ParentViewHolder;
import com.jhj.expandablerecyclerview.model.ParentItem;
import com.jhj.expandablerecyclerview.model.ParentItemWrapper;
import com.jhj.expandablerecyclerview.utils.Logger;
import com.jhj.expandablerecyclerview.utils.Packager;

import java.util.ArrayList;
import java.util.List;

/**
 * 扩展 {@link RecyclerView.Adapter} 实现可展开折叠的 {@link RecyclerView}
 *
 * <p>
 *     该适配器实现了以下接口:
 *     <ul>
 *         <li>{@link #notifyParentItemInserted(int)}</li>
 *         <li>{@link #notifyParentItemRangeInserted(int, int)}</li>
 *         <li>{@link #notifyParentItemRemoved(int)}</li>
 *         <li>{@link #notifyParentItemRangeRemoved(int, int)}</li>
 *         <li>{@link #notifyParentItemChanged(int)}</li>
 *         <li>{@link #notifyParentItemRangeChanged(int, int)}</li>
 *         <li>{@link #notifyParentItemMoved(int, int)} </li>
 *
 *         <li>{@link #notifyChildItemInserted(int, int, boolean)}</li>
 *         <li>{@link #notifyChildItemRangeInserted(int, int, int, boolean)}</li>
 *         <li>{@link #notifyChildItemRemoved(int, int)}</li>
 *         <li>{@link #notifyChildItemRangeRemoved(int, int, int)}</li>
 *         <li>{@link #notifyChildItemChanged(int, int)}</li>
 *         <li>{@link #notifyChildItemRangeChanged(int, int, int)}</li>
 *         <li>{@link #notifyChildItemMoved(int, int, int, int)} </li>
 *     </ul>
 * </p>
 *
 */
public abstract class ExpandableAdapter<PVH extends ParentViewHolder, CVH extends ChildViewHolder>
        extends RecyclerView.Adapter<BaseViewHolder> implements OnParentItemExpandCollapseListener
{
    private static final String TAG = "ExpandableRVAdapter";

    private static final String SAVED_EXPANSION_STATE="savedExpansionState";

    /**
     * ExpandableRecyclerView 展开折叠模式处理类
     */
    private ExpandCollapseMode mExpandCollapseMode = new ExpandCollapseMode();

    /**
     * 父列表项集合
     */
    private List<? extends ParentItem> mParentItems = null;
    /**
     * 当前显示的列表项(父列表项和所有展开的子列表项)集合
     */
    private List<Object> mItems = null;
    /**
     * 当前所有监听适配器的 RecyclerView 集合
     */
    private List<RecyclerView> mAttachedRecyclerViews = new ArrayList<>(1);

    /**
     * 所有监听父列表项展开折叠状态监听器集合
     */
    private List<OnParentExpandCollapseListener> mExpandCollapseListeners = new ArrayList<>(1);

    public ExpandableAdapter(List<? extends ParentItem> parentItems) {
        init(parentItems);
    }

    private void init(List<? extends ParentItem> parentItems) {
        if (parentItems == null) {
            throw new IllegalArgumentException("parentItems should not be " + "null");
        }
        mParentItems = parentItems;
        mItems = ExpandableAdapterHelper.generateItems(parentItems);
    }


    /**
     * 设置新的数据
     * @param newParentItems 新数据
     */
    public void invalidateParentItems(List<? extends ParentItem> newParentItems) {
        init(newParentItems);
        notifyDataSetChanged();
    }

    /**
     * 设置当前的展开折叠模式
     * @param mode 指定的模式
     * @see ExpandCollapseMode
     */
    public void setExpandCollapseMode(int mode) {
        mExpandCollapseMode.mode = mode;
    }

    /**
     * 查询当前设置的展开折叠模式
     * @return 当前设置的展开折叠模式
     * @see ExpandCollapseMode
     */
    public int getExpandCollapseMode(){
        return mExpandCollapseMode.mode;
    }

    /**
     * 父列表项展开或折叠状态监听接口
     */
    public interface OnParentExpandCollapseListener {

        /**
         * 父列表项展开后的回调
         * <p>
         * <b>注意：当通过调用以下方法操作来改变 parent 中的 child 时，如果强制展开 parent 成功，该方法会被回调</b>
         * <ul>
         *     <li>{@link #notifyChildItemInserted(int, int, boolean)}</li>
         *     <li>{@link #notifyChildItemRangeInserted(int, int, int, boolean)}</li>
         *     <li>{@link #expandParent(int)}</li>
         *     <li>{@link #expandParent(ParentItem)}</li>
         *     <li>{@link #expandAllParent()}  </li>
         * </ul>
         * </p>
         * @param parentPosition 该父列表项在父列表里的位置
         * @param byUser 是否属于用户点击父列表项之后产生的展开事件，用于区分用户手动还是程序触发的
         *
         */
        void onParentExpanded(int parentPosition, int parentAdapterPosition, boolean byUser);

        /**
         * 父列表项折叠后的回调
         * <p>
         *     <b>注意：如果调用以下方法时发现 parent 中返回的 childItems List 已为 null 或者为空，该方法也会被回调</b>
         *     <ul>
         *         <li>{@link #notifyChildItemRemoved(int, int)}</li>
         *         <li>{@link #notifyChildItemRangeRemoved(int, int, int)}</li>
         *     </ul>
         * </p>
         * @param parentPosition 该父列表项在父列表里的位置
         * @param byUser 是否属于用户点击父列表项之后产生的折叠事件，用于区分用户手动还是程序触发的
         */
        void onParentCollapsed(int parentPosition, int parentAdapterPosition, boolean byUser);
    }


    /**
     * 注册一个监听父列表项展开或折叠状态改变监听器.
     * <p>use {@link #addParentExpandCollapseListener(OnParentExpandCollapseListener)}</p>
     * @param listener 监听父列表项展开或折叠状态改变的监听器
     *
     */
    @Deprecated
    public void setParentExpandCollapseListener(OnParentExpandCollapseListener listener)
    {
        addParentExpandCollapseListener(listener);
    }

    /**
     * 注册监听父列表项展开折叠状态监听器
     * @param listener 监听器
     */
    public void addParentExpandCollapseListener(OnParentExpandCollapseListener listener) {
        if (listener==null || mExpandCollapseListeners.contains(listener)) return;
        mExpandCollapseListeners.add(listener);
    }

    /**
     * 取消注册监听父列表项折叠状态的改变
     * @param listener 需要取消注册的监听器
     */
    public void unregisterParentExpandCollapseListener(OnParentExpandCollapseListener listener){
        if (listener==null) return;
        mExpandCollapseListeners.remove(listener);
    }


    /**
     * <p>
     * 更具列表项类型判断并回调{@link #onCreateParentViewHolder(ViewGroup, int)}
     * 或者{@link #onCreateChildViewHolder(ViewGroup, int)}
     * </p>
     * 列表项类型包含 父列表项和子列表项类型,也可通过覆盖{@link #getParentType(int)}
     * 或者{@link #getChildType(int, int)}提供具体父列表项类型或子列表项类型
     *
     * @param parent   用于显示列表项视图{@link android.view.View}的 {@link ViewGroup}
     * @param viewType 列表项类型(父列表项类型或子列表项类型，包含具体的父列表项或子列表项类型)
     * @return 根据适配器 ExpandableRecycleViewAdapter 里的数据位置创建的该位置所代表的列表项视图
     */
    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //本地 ItemView 的类型(parent 或者 child)
        final int localViewType= Packager.getLocalViewType(viewType);
        //外部返回的指定的具体的列表项类型(具体的父或子列表项类型)
        final int clientViewType = Packager.getClientViewType(viewType);

        if (localViewType == Packager.ITEM_VIEW_TYPE_PARENT) {
            //回调并返回父列表项视图 ParentViewHolder
            PVH pvh = onCreateParentViewHolder(parent, clientViewType);
            //注册父列表项视图点击事件监听器,用于监听列表项视图的点击并根据列表项的展开状态触发列表项的展开或折叠回调
            pvh.setClickEvent();
            //注册 ParentItemView 点击回调监听器
            pvh.setParentItemExpandCollapseListener(this);
            return pvh;
        } else if (localViewType == Packager.ITEM_VIEW_TYPE_CHILD) {
            ////回调并返回子列表项视图 ChildViewHolder
            return onCreateChildViewHolder(parent, clientViewType);
        } else {
            throw new IllegalStateException("Incorrect ViewType found");
        }
    }

    /**
     * <p>
     *  根据指定的列表项位置在适配器{@link #ExpandableAdapter}里对应的数据集位置绑定数据到该列表项
     * 视图
     *
     *</p>
     * 判断适配器位置的对应的列表项类型
     * (父或子列表项类型，或者具体的父或子列表项类型)
     * 并回调{@link #onBindParentViewHolder(ParentViewHolder, int, int, ParentItem)}
     * 或者{@link #onBindChildViewHolder(ChildViewHolder, int, int, int, int,Object)}通知更新该列表项位置的视图内容
     * 
     *
     * @param holder 指定列表项位置的 ViewHolder，用于更新指定位置的列表项视图
     * @param position 该列表项在适配器数据集中代表的位置
     */
    @Override
    @SuppressWarnings("unchecked")
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        Object listItem = getItem(position);
        int parentPosition = getParentPosition(position);

//        Logger.e(TAG, "onBindViewHolder---->parentPos=" + parentPosition + ",adapterParentPos=" +
//                position);

        if (listItem instanceof ParentItemWrapper) {
            PVH pvh = (PVH) holder;
            ParentItemWrapper parentItemWrapper = (ParentItemWrapper) listItem;
            //初始化展开折叠状态
            pvh.setExpanded(parentItemWrapper.isExpanded());
            onBindParentViewHolder(pvh, parentPosition, position,
                    parentItemWrapper.getParentItem());
        } else if (listItem == null) {
            throw new IllegalStateException("Incorrect ViewHolder found");
        } else {
            CVH cvh = (CVH) holder;
            onBindChildViewHolder(cvh, parentPosition, getChildPosition(position),
                    getParentAdapterPosition(parentPosition), position, listItem);
        }
    }

    @Override
    public int getItemViewType(int position) {
        //获取指定列表项位置在适配器数据集合里所代表的列表项
        Object listItem = getItem(position);
        //获取指定列表项位置(父或子视图项的位置)的在父列表里位置
        int parentPosition = getParentPosition(position);
        //如果是父列表项类型就回调查询具体的父类型
        //返回父类型和具体的父类型的组合后的列表项类型
        if (listItem instanceof ParentItemWrapper) {
            int parentType = getParentType(parentPosition);
            return Packager.makeItemViewTypeSpec(parentType, Packager.ITEM_VIEW_TYPE_PARENT);
        } else if (listItem == null) {
            throw new IllegalStateException("Null object added");
        } else {
            //回调获取具体的子列表项类型
            //返回子类型和具体子类型的组合后的列表项类型
            int childType = getChildType(parentPosition, getChildPosition(position));
            return Packager.makeItemViewTypeSpec(childType, Packager.ITEM_VIEW_TYPE_CHILD);
        }
    }

    /**
     * 返回指定父列表位置的父列表类型
     * 返回的 int 标识不能为负数
     * @param parentPosition 要查询的父列表类型的位置
     * @return 指定父列表位置的父列表类型
     */
    public int getParentType(int parentPosition) {
        return Packager.ITEM_VIEW_TYPE_DEFAULT;
    }

    /**
     * 返回指定的父列表项位置下从属该父列表的子列表项的位置对应的子列表项类型
     * 返回的 int 标识不能为负数
     * @param parentPosition 该子列表项的从属父列表项位置
     * @param childPosition 子列表项的位置
     * @return 子列表项的类型
     */
    public int getChildType(int parentPosition, int childPosition) {
        return Packager.ITEM_VIEW_TYPE_DEFAULT;
    }

    /**
     * 返回当前所有要显示的列表项(父列表和子列表)数据数量
     * @return 初始化时显示的列表项数量
     */
    @Override
    public int getItemCount() {
        return mItems.size();
    }

    /**
     * 来自{@link #onCreateViewHolder(ViewGroup, int)}的创建指定父列表项视图的回调
     * 创建父列表项用于显示的父列表项视图的 {@link ParentViewHolder}，通过回调返回客户端的所指定的父列表项视图
     * @param parent 用于显示列表项视图的{@link ViewGroup}
     * @param parentType 父列表项的类型，用于提供不同的父列表项视图
     * @return 父列表项所需显示视图的 {@link ParentViewHolder}
     */
    public abstract PVH onCreateParentViewHolder(ViewGroup parent, int parentType);

    /**
     * 来自{@link #onCreateViewHolder(ViewGroup, int)}的创建指定子列表项视图的回调
     * @param child 用于显示列表项视图的{@link ViewGroup}
     * @param childType 子列表项的类型
     * @return 子列表项所需显示视图的 {@link ChildViewHolder}
     */
    public abstract CVH onCreateChildViewHolder(ViewGroup child, int childType);

    /**
     * 来自 {@link #onBindViewHolder(RecyclerView.ViewHolder, int)}的用于绑定视图数据 到{@link PVH}
     * 的回调
     * @param parentViewHolder 用于绑定数据的父列表项的 parentViewHolder
     * @param parentPosition 该父列表项所在父列表里的位置
     * @param parentAdapterPosition 该父列表项在适配器数据集里所对应的位置
     * @param parentItem 和该父列表项绑定的数据源 {@link ParentItem}
     */
    public abstract void onBindParentViewHolder(PVH parentViewHolder, int parentPosition,
            int parentAdapterPosition, ParentItem parentItem);

    /**
     * 来自 {@link #onBindViewHolder(RecyclerView.ViewHolder, int)}的用于绑定数据到{@link CVH}的回调
     * @param childViewHolder 用于显示或更新绑定到 CVH 里的数据
     * @param parentPosition 该子列表项所从属的父列表项在父列表里的位置
     * @param childPosition 该子列表项在子列表里的位置
     * @param parentAdapterPosition 该父列表项在适配器数据集里所对应的位置
     * @param childAdapterPosition 该子列表项在适配器数据集里对应的位置
     * @param childListItem 用于绑定到 CVH 里的数据源
     */
    public abstract void onBindChildViewHolder(CVH childViewHolder, int parentPosition,
            int childPosition, int parentAdapterPosition, int childAdapterPosition,
            Object childListItem);

    /**
     * {@link ParentViewHolder}里父列表项展开回调，用于监听父列表项展开事件并触发相关的展开操作
     * @param parentAdapterPosition 该父列表项在适配器数据集里对应的位置
     */
    @Override
    public boolean onParentItemExpand(int parentAdapterPosition) {
        return expandParentItem(parentAdapterPosition, true, false);
    }
    /**
     * {@link ParentViewHolder} 里父列表项折叠回调，用于监听父列表项折叠事件并触发相关的折叠操作
     * @param parentAdapterPosition 该父列表项在适配器数据集里对应的位置
     */
    @Override
    public boolean onParentItemCollapse(int parentAdapterPosition) {
        return collapseParentItem(parentAdapterPosition, true, false);
    }


    /**
     * 当有 {@code RecyclerView} 监听该适配器时的回调
     * <p>
     *     注意: 同一个适配器可能被多个 {@code RecyclerView} 监听
     * </p>
     * @param recyclerView 开始监听该适配器的 {@code RecyclerView} 实例
     */
    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mAttachedRecyclerViews.add(recyclerView);
    }

    /**
     * 当 {@code RecyclerView} 停止监听之前它所监听的适配器时的回调
     * @param recyclerView 停止监听适配器的 {@code RecyclerView} 实例
     */
    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        mAttachedRecyclerViews.remove(recyclerView);
    }

    /**
     * 获取指定适配器位置数据所代表的列表项
     *
     * @param position 该列表项在适配器数据集中对应的位置
     * @return 指定适配器位置数据所代表的列表项
     */
    public Object getItem(int position) {
        return mItems.get(position);
    }

    /**
     * 根据指定的列表项在适配器数据集中所对应的位置返回该列表项在父列表里的位置
     * 如果该列表项为子列表项就返回该子列表项所属的父列表项在父列表里位置
     * @param adapterPosition 要查询的列表项在适配器里所对应的位置
     * @return 指定列表项在父列表里的位置
     */
    private int getParentPosition(int adapterPosition) {
        if (adapterPosition == RecyclerView.NO_POSITION) return RecyclerView.NO_POSITION;
        if (adapterPosition == 0) return 0;
        Object item = getItem(adapterPosition);
        if (item instanceof ParentItemWrapper) {
            int beforeExpandedChildItemCount = getBeforeExpandedChildCount(adapterPosition);
            return adapterPosition - beforeExpandedChildItemCount;
        } else {
            for (int i = adapterPosition - 1; i >= 0; i--) {
                if (getItem(i) instanceof ParentItemWrapper) {
                    return i - getBeforeExpandedChildCount(i);
                }
            }
        }
        return RecyclerView.NO_POSITION;
    }
    
    
    /**
     * 根据指定的子列表项在适配器数据集中所对应的位置返回该子列表项在子列表里的位置
     * @param childAdapterPosition 子列表项在适配器里对应的位置
     * @return 指定子列表项在子列表里的位置
     */
    private int getChildPosition(int childAdapterPosition) {
        if (childAdapterPosition == RecyclerView.NO_POSITION) return RecyclerView.NO_POSITION;
        int childAdapterPos = RecyclerView.NO_POSITION;
        ParentItemWrapper parentItemWrapper = getParentWrapper(childAdapterPosition);
        if (parentItemWrapper != null) {
            Object childItem = getItem(childAdapterPosition);
            List<?> childItems = parentItemWrapper.getChildItems();
            if (childItems != null) {
                childAdapterPos = childItems.indexOf(childItem);
            }
        }
        return childAdapterPos;
    }


    /**
     * 添加对应 {@code parentWrapperPosition} 位置的父列表项
     *
     * @param parentWrapperPosition 要添加新父列表项所指定的位置
     * @param newParentItemPosition     新添加的父列表项的位置
     * @return 插入要适配器数据集里的列表项的个数(父列表项和其展开的子列表项)
     */
    private int addParentWrapper(int parentWrapperPosition, int newParentItemPosition) {
        if (parentWrapperPosition==RecyclerView.NO_POSITION || newParentItemPosition < 0) return 0;
        ParentItem newParentItem=mParentItems.get(newParentItemPosition);
        ParentItemWrapper newParentItemWrapper = new ParentItemWrapper(newParentItem);
        mItems.add(parentWrapperPosition, newParentItemWrapper);
        int insertedItemCount = 1;
        if (newParentItemWrapper.isInitiallyExpanded()) {
            List<?> childItems = newParentItemWrapper.getChildItems();
            final boolean hasChildren = childItems != null && !childItems.isEmpty();
            newParentItemWrapper.setExpanded(hasChildren);
            if (hasChildren) {
                mItems.addAll(parentWrapperPosition + insertedItemCount, childItems);
                insertedItemCount += childItems.size();
            }
        }
        return insertedItemCount;
    }

    /**
     * 根据指定的子列表项在适配器数据集中所对应的位置返回该子列表项所属的父列表项
     * @param childAdapterPosition 子列表项在适配器里对应的位置
     * @return 指定子列表项所属的父列表项
     */
    private ParentItemWrapper getParentWrapper(int childAdapterPosition) {
        if (childAdapterPosition == RecyclerView.NO_POSITION) return null;
        for (int i = childAdapterPosition - 1; i >= 0; i--) {
            Object listItem = getItem(i);
            if (listItem instanceof ParentItemWrapper) {
                return (ParentItemWrapper) listItem;
            }
        }
        return null;
    }

//    /**
//     * 根据指定的在适配器里对应位置的列表项返回是否是父列表项
//     * @param adapterPosition 列表项在适配器里对应的位置
//     * @return 指定的在适配器里对应位置的列表项返回是否是父列表项
//     */
//    public boolean isParent(int adapterPosition) {
//        return !(adapterPosition >= mItems.size() || adapterPosition < 0) && mItems.get(
//                adapterPosition) instanceof ParentItemWrapper;
//    }

    /**
     * 获取指定的父列表项位置返回该列表项在适配器里对应的位置
     * @param parentPosition 指定的父列表项在父列表里的位置
     * @return 指定的父列表项位置在适配器里对应的位置
     */
    private int getParentAdapterPosition(int parentPosition) {
        if (parentPosition < 0) return RecyclerView.NO_POSITION;
        int parentIndex = -1;
        int listItemCount = mItems.size();
        for (int i = 0; i < listItemCount; i++) {
            Object listItem = mItems.get(i);
            if (listItem instanceof ParentItemWrapper) {
                parentIndex++;
                if (parentIndex == parentPosition) {
                    return i;
                }
            }
        }
        return RecyclerView.NO_POSITION;
    }

    /**
     * 获取指定的子列表项位置在适配器里对应的位置
     * @param parentPosition 要查询的子列表项所属的父列表项的位置
     * @param childPosition 子列表项的位置
     * @return 子列表项位置在适配器里对应的位置
     */
    private int getChildAdapterPosition(int parentPosition, int childPosition) {
        if (parentPosition < 0 || childPosition < 0) return RecyclerView.NO_POSITION;
        int parentAdapterPosition=getParentAdapterPosition(parentPosition);
        return parentAdapterPosition + childPosition + 1;
    }

    /**
     * 获取父列表项之前展开的所有子列表项数量
     * @param parentAdapterPosition 父列表项在适配器中所对应的位置
     * @return 父列表项之前展开的所有子列表项数量
     */
    private int getBeforeExpandedChildCount(int parentAdapterPosition) {
        if (parentAdapterPosition == 0 || parentAdapterPosition == RecyclerView.NO_POSITION) return 0;
        int beforeExpandedChildCount = 0;
        for (int i = 0; i < parentAdapterPosition; i++) {
            if (!(getItem(i) instanceof ParentItemWrapper)) {
                beforeExpandedChildCount++;
            }
        }
        return beforeExpandedChildCount;
    }

    /**
     * Test 方法
     * 通知更新所有 ItemView
     */
    public void notifyAllChanged() {
        notifyItemRangeChanged(0,mItems.size());
    }

    /**
     * 展开指定适配器位置所对应的父列表项
     * @param parentAdapterPosition 父列表项在适配器里所对应的位置
     * @param byUser 是否是用户通过点击展开父列表项的
     * @return 是否展开成功,如果当前已经展开了或者该 Parent 没有 Child，亦或是内部异常发生时返回 false              
     */
    private boolean expandParentItem(int parentAdapterPosition, boolean byUser, boolean force)
    {
        if (parentAdapterPosition==RecyclerView.NO_POSITION) return false;
        Object item = getItem(parentAdapterPosition);
        if (!(item instanceof ParentItemWrapper)) return false;
        ParentItemWrapper parentItemWrapper = (ParentItemWrapper) item;
        //如果非强制展开 Parent 并且当前 parent 无法展开或者当前 Parent 已展开情况下调用无效
        if (!force && (!parentItemWrapper.isExpandable() || parentItemWrapper.isExpanded())) return false;
        //如果强制展开 Parent 并且当前 parent 已展开时调用也无效，例如:程序调用展开同一 parent 方法多次
        if (force && parentItemWrapper.isExpanded()) return false;

        List<?> childItems = parentItemWrapper.getChildItems();
        if (childItems == null || childItems.isEmpty()) return false;

        //保存该父列表项当前为展开状态
        parentItemWrapper.setExpanded(true);

        final int insertPosStart = parentAdapterPosition + 1;
        final int childCount = childItems.size();
        //按照顺序依次将子列表项插入到该父列表项下
        mItems.addAll(insertPosStart, childItems);
        //通知 RecyclerView 指定位置有新的列表项插入，刷新界面
        notifyItemRangeInserted(insertPosStart, childCount);
        //通知所有监听 Parent 展开折叠状态监听器当前 Parent 已展开
        notifyParentExpanded(parentAdapterPosition, byUser);
        //检查当前的展开模式，如果为单项展开模式处理单项 ParentItem 展开逻辑
        checkSingleExpandMode(parentAdapterPosition);

        return true;
    }

    /**
     * 检查并处理当前是否为单项 ParentItem 展开模式逻辑
     * <p>
     *     单项 ParentItem 模式下，记录上次展开的 parentItem 的 position 和 adapterPosition 供下次展开其他 ParentItem
     *     时自动折叠该位置记录下的 parentItem
     * </p>
     * @param currExpandedAdapterPosition 当前已展开的 parentItem 的适配器位置
     */
    private void checkSingleExpandMode(int currExpandedAdapterPosition) {
        if (mExpandCollapseMode.mode != ExpandCollapseMode.MODE_SINGLE_EXPAND) return;
        final int currExpandedPosition = getParentPosition(currExpandedAdapterPosition);
        final int lastExpandedAdapterPosition = mExpandCollapseMode.lastExpandedAdapterPosition;
        final int lastExpandedPosition = mExpandCollapseMode.lastExpandedPosition;
        if (lastExpandedPosition == currExpandedPosition) return;
        Logger.e(TAG, "checkSingleExpandMode " +
                "---->lastExpandedPosition=*" + lastExpandedPosition+"," +
                "lastExpandedAdapterPosition="+lastExpandedAdapterPosition);
        //折叠上次记录的展开位置对应的 parentItem，注意这里需转换的是上次的 position 而非参数 currExpandedAdapterPosition!
        collapseViews(getParentAdapterPosition(lastExpandedPosition), true);
        //将当前展开的 parentItem 相关位置记录为下次自动折叠位置
        mExpandCollapseMode.lastExpandedPosition = currExpandedPosition;
        mExpandCollapseMode.lastExpandedAdapterPosition=currExpandedAdapterPosition;
    }

    /**
     * 通知所有外部已注册监听 Parent 展开折叠状态的监听器当前 Parent
     * <p><b>注意:通知的前提必须已经调用 {@link RecyclerView Adapter} 通知 {@link RecyclerView} 数据改变</b></p>
     * @param parentAdapterPosition 被展开的 Parent 在本地适配器里对应的位置
     * @param byUser 是否是被用户手动展开的
     */
    private void notifyParentExpanded(int parentAdapterPosition, boolean byUser) {
        for (OnParentExpandCollapseListener listener : mExpandCollapseListeners) {
            int beforeExpandedChildCount = getBeforeExpandedChildCount(parentAdapterPosition);
            listener.onParentExpanded(parentAdapterPosition - beforeExpandedChildCount,
                    parentAdapterPosition, byUser);
        }
    }

    /**
     * 折叠指定适配器位置所对应的父列表项
     * @param parentAdapterPosition 父列表项在适配器里所对应的位置
     * @param byUser 是否是用户通过点击展开父列表项的
     */
    private boolean collapseParentItem(int parentAdapterPosition, boolean byUser, boolean force)
    {
        if (parentAdapterPosition==RecyclerView.NO_POSITION) return false;
        Object item = getItem(parentAdapterPosition);
        if (!(item instanceof ParentItemWrapper)) return false;
        ParentItemWrapper parentItemWrapper = (ParentItemWrapper) item;
        if (!force && (!parentItemWrapper.isExpandable() || !parentItemWrapper.isExpanded())) return false;
        if (force && !parentItemWrapper.isExpanded()) return false;
        List<?> childItems = parentItemWrapper.getChildItems();
        if (childItems == null || childItems.isEmpty()) return false;

        //保存该父列表项当前为折叠状态
        parentItemWrapper.setExpanded(false);

        final int collapsePosStart = parentAdapterPosition + 1;
        final int childItemCount = childItems.size();
        //按照顺序依次将该父列表项下的子列表项移除
        mItems.removeAll(childItems);
        //通知 RecyclerView 指定位置有列表项已移除，刷新界面
        notifyItemRangeRemoved(collapsePosStart, childItemCount);
        //通知所有监听 Parent 展开折叠状态监听器当前 Parent 已折叠
        notifyParentCollapsed(parentAdapterPosition, byUser);
        //检查当前的折叠模式，如果为单项折叠模式处理单项 parentItem 折叠逻辑
        checkSingleCollapseMode(parentAdapterPosition);

        return true;
    }

    /**
     * 检查并处理当前是否为单项 ParentItem 折叠模式逻辑
     * <p>
     *     单项 ParentItem 模式下，记录上次折叠的 parentItem 的 position 和 adapterPosition 供下次折叠其他 ParentItem
     *     时自动展开该位置记录下的 parentItem
     * </p>
     * @param currCollapsedAdapterPosition 当前已折叠的 parentItem 的适配器位置
     */
    private void checkSingleCollapseMode(int currCollapsedAdapterPosition) {
        if (mExpandCollapseMode.mode != ExpandCollapseMode.MODE_SINGLE_COLLAPSE) return;
        final int currCollapsedPosition = getParentPosition(currCollapsedAdapterPosition);
        final int lastCollapsedAdapterPosition=mExpandCollapseMode.lastCollapsedAdapterPosition;
        final int lastCollapsedPosition=mExpandCollapseMode.lastCollapsedPosition;
        if (currCollapsedPosition == lastCollapsedPosition) return;
        Logger.e(TAG, "checkSingleCollapseMode " +
                "---->lastCollapsedPosition=*" + lastCollapsedPosition + "," +
                "lastCollapsedAdapterPosition=" + lastCollapsedAdapterPosition);
        //展开上次记录的折叠位置对应的 parentItem，注意这里需转换的是上次的 position 而非参数 currCollapsedAdapterPosition!
        expandViews(getParentAdapterPosition(lastCollapsedPosition), true);
        //将当前折叠的 parentItem 相关位置记录为下次自动展开位置
        mExpandCollapseMode.lastCollapsedPosition = currCollapsedPosition;
        mExpandCollapseMode.lastCollapsedAdapterPosition = currCollapsedAdapterPosition;
    }

    /**
     * 通知所有外部已注册监听 Parent 展开折叠状态的监听器当前 Parent 已折叠
     * <p><b>注意:通知的前提必须已经调用 {@link RecyclerView Adapter} 通知 {@link RecyclerView} 数据改变</b></p>
     * @param parentAdapterPosition 被折叠的 Parent 在本地适配器里对应的位置
     * @param byUser 是否是被用户手动折叠的
     */
    private void notifyParentCollapsed(int parentAdapterPosition, boolean byUser) {
        for (OnParentExpandCollapseListener listener : mExpandCollapseListeners) {
            int beforeExpandedChildCount = getBeforeExpandedChildCount(parentAdapterPosition);
            listener.onParentCollapsed(parentAdapterPosition - beforeExpandedChildCount,
                    parentAdapterPosition, byUser);
        }
    }

    /**
     * 同步 attach 到当前 adapter 的所有 RecyclerView 指定的适配器位置的 parentItem 的展开状态
     * <p>
     *     <b>注意:</b>可能由于 ParentItem 未 laid out 到 RecyclerView 并且 RecyclerView 没有正确回调
     *     {@link #onBindParentViewHolder(ParentViewHolder, int, int, ParentItem)} 方法导致同步
     *     展开状态失败，这里存储待处理的 parentItem 的位置，以至于能够在 {@link #onViewAttachedToWindow(BaseViewHolder)}
     *     时处理状态同步逻辑
     * </p>
     * @param parentAdapterPosition 指定的同步展开状态的 parentItem 的在适配器里的位置
    * @param expanded 指定位置的 ParentItem 是否已展开
     */
    @SuppressWarnings("unchecked")
    private void syncViewExpansionState(int parentAdapterPosition, boolean expanded)
    {
        if (parentAdapterPosition == RecyclerView.NO_POSITION) return;

        //for debug
        final int parentPos = getParentPosition(parentAdapterPosition);

        for (RecyclerView recyclerView : mAttachedRecyclerViews) {
            PVH pvh = (PVH) recyclerView.findViewHolderForAdapterPosition(parentAdapterPosition);
            if (pvh != null && !pvh.isExpanded()) {
                pvh.setExpanded(expanded);
                Logger.e(TAG,"expanded="+expanded);
            } else if (pvh == null && expanded) {
                Logger.e(TAG, "expandViews pvh is null---->parentPos=" + parentPos +
                        ",parentAdapterPos=" +
                        parentAdapterPosition);
                //未 laid out 的 ParentItem ,虽然无法获取并设置展开折叠标识，
                // 这里添加待处理展开逻辑的所有 parentItem 的 position
                // 如果先前已经记录待折叠位置记录，移除该记录并以最新的待展开记录为准!
                if (mPendingCollapsePositions.contains(parentAdapterPosition)) {
                    mPendingCollapsePositions.remove((Integer) parentAdapterPosition);
                }
                mPendingExpandPositions.add(parentAdapterPosition);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void syncViewCollapseState(int parentAdapterPosition, boolean collapsed) {
        Logger.e(TAG,"syncViewCollapseState=>"+collapsed);
        if (parentAdapterPosition == RecyclerView.NO_POSITION) return;

        //for debug
        final int parentPos = getParentPosition(parentAdapterPosition);

        for (RecyclerView recyclerView : mAttachedRecyclerViews) {
            PVH pvh = (PVH) recyclerView.findViewHolderForAdapterPosition(parentAdapterPosition);
            if (pvh != null && pvh.isExpanded()) {
                pvh.setExpanded(!collapsed);
                Logger.e(TAG,"collapsed="+collapsed);
            } else if (pvh == null && collapsed) {
                Logger.e(TAG, "collapseViews pvh is null---->parentPos=" + parentPos +
                        ",parentAdapterPos=" +
                        parentAdapterPosition);
                // 未 laid out 的 ParentItem ,虽然无法获取并设置展开折叠标识，
                // 这里添加待处理折叠逻辑的所有 parentItem 的 position
                // 如果先前已经记录待折叠位置记录，移除该记录并以最新的待展开记录为准!
                if (mPendingExpandPositions.contains(parentAdapterPosition)) {
                    mPendingExpandPositions.remove((Integer) parentAdapterPosition);
                }
                mPendingCollapsePositions.add(parentAdapterPosition);
            }
        }
    }

    /**
     * 展开指定适配器位置所对应的父列表项
     * <p>循环遍历展开所有 attach 到此 adapter 的 {@link RecyclerView} 中指定的{@code parentAdapterPosition}
     * 所对应的 Parent ,并且同步改变对应 View 层(ParentViewHolder) 中的展开折叠状态
     * </p>
     * @param parentAdapterPosition 父列表项在适配器里所对应的位置
     */
    @SuppressWarnings("unchecked")
    private boolean expandViews(int parentAdapterPosition, boolean force) {
        final int parentPos = getParentPosition(parentAdapterPosition);

        Logger.i(TAG, "expandViews---->parentPos=" + parentPos + ",parentAdapterPos=" +
                parentAdapterPosition);

        boolean isSuccessful = expandParentItem(parentAdapterPosition, false, force);

        if (!isSuccessful) {
            Logger.e(TAG, "expandViews failed---->parentPos=" + parentPos + ",parentAdapterPos=" +
                    parentAdapterPosition);
        }
        //同步 parentItem 展开状态
        //注意:这里需要再次通过 parentPosition 转换为 parentAdapterPosition，而不是直接使用参数 parentAdapterPosition!
        //因为{@link #expandParentItem()可能会因为当前模式为单项展开模式导致内部逻辑会自动折叠先前记录的展开位置的 ParentItem}
        syncViewExpansionState(getParentAdapterPosition(parentPos), isSuccessful);

        return isSuccessful;
    }

    /**
     * 根据指定的父列表项在父列表里的位置展开该位置在适配器里对应的父列表项
     * @param parentPosition 该父列表项在父列表里的位置
     */
    public boolean expandParent(int parentPosition) {
        int parentAdapterPosition = getParentAdapterPosition(parentPosition);
        return parentAdapterPosition != RecyclerView.NO_POSITION && expandViews(
                parentAdapterPosition,true);
    }

    /**
     * 展开与{@link ParentItem}相关的父列表项
     * @param parentItem 与父列表项相关的 ParentItem
     */
    public boolean expandParent(ParentItem parentItem) {
        if (parentItem == null) return false;
        int parentPosition = mParentItems.indexOf(parentItem);
        return expandParent(parentPosition);
    }

    /**
     * 展开所有的父列表项
     * <p>
     *     <b>注意:如果当前设置的展开折叠 {@link #setExpandCollapseMode(int)} 为
     *     {@link ExpandCollapseMode MODE_SINGLE_EXPAND} ,调用该方法不会展开所有的 parentItem，只会展开最后一个
     *     parentItem，如果可以展开的话</b>
     * </p>
     */
    public void expandAllParent() {
        for (int i = 0; i < mParentItems.size(); i++) {
            expandParent(i);
        }
    }

    /**
     * 折叠指定适配器位置所对应的父列表项.
     * <p>循环遍历展开所有 attach 到此 adapter 的 {@link RecyclerView} 中指定的{@code parentAdapterPosition}
     * 所对应的 Parent ,并且同步改变对应 View 层(ParentViewHolder) 中的展开折叠状态
     * </p>
     * @param parentAdapterPosition 父列表项在适配器里所对应的位置
     */
    @SuppressWarnings("unchecked")
    private boolean collapseViews(int parentAdapterPosition, boolean force) {
        final int parentPos = getParentPosition(parentAdapterPosition);

        Logger.i(TAG, "collapseViews---->parentPos=" + parentPos + ",parentAdapterPos=" +
                parentAdapterPosition);

        boolean isSuccessful = collapseParentItem(parentAdapterPosition, false, force);

        if (!isSuccessful) {
            Logger.e(TAG, "collapseViews failed---->parentPos=" + parentPos + ",parentAdapterPos=" +
                    parentAdapterPosition);
        }

        //同步 parentItem 折叠状态
        //注意:这里需要再次通过 parentPosition 转换为 parentAdapterPosition，而不是直接使用参数 parentAdapterPosition!
        //因为{@link #collapseParentItem()可能会因为当前模式为单项折叠模式导致内部逻辑会自动展开先前记录的折叠位置的 ParentItem}
        syncViewCollapseState(getParentAdapterPosition(parentPos), isSuccessful);

        return isSuccessful;
    }

    /**
     * 待处理展开折叠逻辑的所有 {@link ParentViewHolder}  的位置集合
     * <p>
     *     在调用 {@link #collapseAllParent()} {@link #expandAllParent()} 循环遍历展开折叠所有已折叠展开的
     *     parent 时，在
     *     {@link #collapseParent(int)} {@link #expandViews(int, boolean)}
     *     方法中处理展开折叠逻辑时，因为某些特殊情况无法获取未布局到
     *     {@link RecyclerView} 中的 {@link ParentViewHolder} 并且没有正确回调
     *     {@link #onBindParentViewHolder(ParentViewHolder, int, int, ParentItem)} 方法进行同步导致设置
     *     {@link ParentViewHolder} 所代表的 parentItem 展开状态彻底失败的 bug
     *     ,所有这里收集待处理的所有
     *     {@link ParentViewHolder} 的位置，以至于能够在 {@link #onViewAttachedToWindow(BaseViewHolder)}
     *     中处理所有待处理的展开折叠逻辑的 {@link ParentViewHolder}
     *
     * </p>
     */
    private List<Integer> mPendingExpandPositions = new ArrayList<>();
    private List<Integer> mPendingCollapsePositions = new ArrayList<>();

    @Override
    @SuppressWarnings("unchecked")
    public void onViewAttachedToWindow(BaseViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        if (!(holder instanceof ParentViewHolder)) return;
        PVH pvh = (PVH) holder;
        final int adapterPos = holder.getAdapterPosition();

        if (mPendingExpandPositions.contains(adapterPos)) {
            Logger.e(TAG, "onViewAttachedToWindow==PendingExpandPosition=>" + adapterPos);
            if (!pvh.isExpanded()) {
                pvh.setExpanded(true);
                notifyParentExpanded(adapterPos, false);
            }
            mPendingExpandPositions.remove((Integer) adapterPos);
        }

        if (mPendingCollapsePositions.contains(adapterPos)) {
            Logger.e(TAG, "onViewAttachedToWindow==PendingCollapsePosition=>" + adapterPos);
            if (pvh.isExpanded()) {
                pvh.setExpanded(false);
                notifyParentCollapsed(adapterPos, false);
            }
            mPendingCollapsePositions.remove((Integer) adapterPos);
        }
    }

    @Override
    public void onViewDetachedFromWindow(BaseViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
    }


    /**
     * 根据指定的父列表项在父列表里的位置折叠该位置在适配器里对应的父列表项
     * @param parentPosition 该父列表项在父列表里的位置
     */
    public boolean collapseParent(int parentPosition) {
        int parentAdapterPosition = getParentAdapterPosition(parentPosition);
        return parentAdapterPosition != RecyclerView.NO_POSITION && collapseViews(
                parentAdapterPosition, true);
    }

    /**
     * 折叠与{@link ParentItem}相关的父列表项
     * @param parentItem 与父列表项相关的 ParentItem
     */
    public boolean collapseParent(ParentItem parentItem) {
        if (parentItem == null) return false;
        int parentPosition = mParentItems.indexOf(parentItem);
        return collapseParent(parentPosition);
    }

    /**
     * 折叠所有的父列表项
     * <p>
     *     <b>注意:如果当前设置的展开折叠 {@link #setExpandCollapseMode(int)} 为
     *     {@link ExpandCollapseMode MODE_SINGLE_COLLAPSE} ,调用该方法不会折叠所有的 parentItem，只会折叠最后一个
     *     parentItem，如果可以折叠的话</b>
     * </p>
     */
    public void collapseAllParent() {
        for (int i = 0; i < mParentItems.size(); i++) {
            collapseParent(i);
        }
    }

    /**
     *  通知任何注册的监视器当前在 {@code parentPosition} 位置有新的父列表项插入，
     * 之前在该位置存在的父列表项将被移动到 {@code parentPosition + 1} 位置。
     * <p>
     * 这是数据结构上的变化，尽管位置变化了，但是之前存在的所有列表项在数据集里的数据都会被认为最新，
     * 因此这些列表项不会被重新绑定数据
     * </p>
     * @param parentPosition
     *                      要插入新的父列表项的位置
     * @see #notifyParentItemRangeInserted(int, int)
     */
    public final void notifyParentItemInserted(int parentPosition) {
        notifyParentItemRangeInserted(parentPosition,1);
    }

    /**
     * 通知任何注册的监视器当前在 {@code parentPosition,childPosition} 位置有新的子列表项插入，
     * 之前在该位置存在的子列表项将被移动到 {@code childPosition+1} 位置。
     * <p>
     * 这是数据结构上的变化，尽管位置变化了，但是之前存在的所有列表项在数据集里的数据都会被认为最新，
     * 因此这些列表项不会被重新绑定数据
     * </p>
     * @param parentPosition 要插入的子列表项所属的父列表项位置
     * @param childPosition 要插入的子列表项的位置
     * @see #notifyChildItemRangeInserted(int, int, int, boolean)
     */
    public final void notifyChildItemInserted(int parentPosition, int childPosition)
    {
        notifyChildItemInserted(parentPosition, childPosition, true);
    }

    /**
     * 通知任何注册的监视器当前在 {@code parentPosition,childPosition} 位置有新的子列表项插入，
     * 之前在该位置存在的子列表项将被移动到 {@code childPosition+1} 位置。
     * <p>
     * 这是数据结构上的变化，尽管位置变化了，但是之前存在的所有列表项在数据集里的数据都会被认为最新，
     * 因此这些列表项不会被重新绑定数据
     * </p>
     * @param parentPosition 要插入的子列表项所属的父列表项位置
     * @param childPosition 要插入的子列表项的位置
     * @param forceExpandParent 是否强制展开已插入的子列表项所属的父列表项，如果当前父列表项没有展开
     * @see #notifyChildItemRangeInserted(int, int, int, boolean)
     */
    public void notifyChildItemInserted(int parentPosition, int childPosition,
            boolean forceExpandParent)
    {
        notifyChildItemRangeInserted(parentPosition, childPosition, 1, forceExpandParent);
    }

    /**
     * 通知任何注册的监视器当前在 {@code parentPositionStart} 位置有 {@code parentItemCount} 个父列表项插入，
     * 之前在该位置存在的父列表项将被移动到 {@code parentPosition + parentItemCount} 位置。
     * <p>
     * 这是数据结构上的变化，尽管位置变化了，但是之前存在的所有列表项在数据集里的数据都会被认为最新，
     * 因此这些列表项不会被重新绑定数据
     * </p>
     * @param parentPositionStart 要插入多个新父列表项的位置
     * @param parentItemCount 要插入新父列表项的个数
     * @see #notifyParentItemInserted(int)
     */
    public final void notifyParentItemRangeInserted(int parentPositionStart, int parentItemCount) {
        final int endInsertPos = mParentItems.size() - parentItemCount;
        int parentAdapterPos = RecyclerView.NO_POSITION;
        if (parentPositionStart >= 0 && parentPositionStart < endInsertPos) {
            parentAdapterPos = getParentAdapterPosition(parentPositionStart);
        } else if (parentPositionStart == endInsertPos) {
            parentAdapterPos = mItems.size();
        }
        if (parentAdapterPos == RecyclerView.NO_POSITION) return;
        int insertedItemCount = 0;
        for (int i = parentPositionStart; i < parentPositionStart + parentItemCount; i++) {
            insertedItemCount += addParentWrapper(parentAdapterPos + insertedItemCount, i);
        }
        notifyItemRangeInserted(parentAdapterPos, insertedItemCount);
        //这里如果有 parentItem 初始化是展开的 是否需要 notifyParentExpanded 通知客户端?
        //注意：通知客户端通知的前提是当前所有的 ParentItem 已经 notifyItemRangeInserted 通知 recyclerView 了
    }

    /**
     * 通知任何注册的监视器当前在 {@code parentPosition,childPositionStart} 位置有 {@code childItemCount} 个子列表项插入，
     * 之前在该位置存在的子列表项将被移动到 {@code childPositionStart + childItemCount} 位置。
     * <p>
     * 这是数据结构上的变化，尽管位置变化了，但是之前存在的所有列表项在数据集里的数据都会被认为最新，
     * 因此这些列表项不会被重新绑定数据
     * </p>
     *
     * @param parentPosition 要插入多个新子列表项所属的父列表项位置
     * @param childPositionStart 要插入多个新子列表项的位置
     * @param childItemCount 要插入的子列表项的个数
     * @see #notifyChildItemInserted(int, int, boolean)
     */
    public final void notifyChildItemRangeInserted(int parentPosition, int childPositionStart,
            int childItemCount)
    {
        notifyChildItemRangeInserted(parentPosition, childPositionStart, childItemCount, true);
    }

    /**
     * 通知任何注册的监视器当前在 {@code parentPosition,childPositionStart} 位置有 {@code childItemCount} 个子列表项插入，
     * 之前在该位置存在的子列表项将被移动到 {@code childPositionStart + childItemCount} 位置。
     * <p>
     * 这是数据结构上的变化，尽管位置变化了，但是之前存在的所有列表项在数据集里的数据都会被认为最新，
     * 因此这些列表项不会被重新绑定数据
     * </p>
     *
     * @param parentPosition 要插入多个新子列表项所属的父列表项位置
     * @param childPositionStart 要插入多个新子列表项的位置
     * @param childItemCount 要插入的子列表项的个数
     * @param forceExpandParent 是否强制展开已插入的子列表项所属的父列表项，如果当前父列表项没有展开
     * @see #notifyChildItemInserted(int, int, boolean)
     */
    public final void notifyChildItemRangeInserted(int parentPosition, int childPositionStart,
            int childItemCount, boolean forceExpandParent)
    {
        int parentAdapterPos = getParentAdapterPosition(parentPosition);
        if (parentAdapterPos == RecyclerView.NO_POSITION) return;
        ParentItemWrapper parentItemWrapper = (ParentItemWrapper) getItem(parentAdapterPos);
        //这里判断如果父列表项已展开才添加数据并通知刷新列表界面
        //注意：这里的数据添加和 {@code expandParentItem } 数据添加有冲突，因为 expandParentItem
        //获取子列表数据然后再添加到模型数据层再刷新界面的，这里如果判断没展开就不能添加数据，否则有重复数据显示
        if (!forceExpandParent && !parentItemWrapper.isExpanded()) return;

        //如果强制展开父列表项并且当前的父列表项没有展开，直接调用{@link #expandViews},通知父列表项已展开
        if (forceExpandParent && !parentItemWrapper.isExpanded()) {
            //直接展开 ParentItem,注意：同时更新对应的 ParentViewHolder 的展开折叠状态
            expandViews(parentAdapterPos, true);
            return;
        }

        List<?> childItems = parentItemWrapper.getChildItems();
        if (childItems==null || childItems.isEmpty()) return;
        List<?> insertedChildItemList = childItems.subList(childPositionStart,
                childItemCount + childPositionStart);

        int childAdapterPos=RecyclerView.NO_POSITION;
        if (childPositionStart >= 0 && childPositionStart < childItems.size() - childItemCount) {
            childAdapterPos = getChildAdapterPosition(parentPosition, childPositionStart);
        } else if(childPositionStart == childItems.size() - childItemCount){
            childAdapterPos = parentAdapterPos + childItems.size() - childItemCount + 1;
        }
        if (childAdapterPos==RecyclerView.NO_POSITION) return;

        mItems.addAll(childAdapterPos, insertedChildItemList);
        notifyItemRangeInserted(childAdapterPos, childItemCount);
    }

    /**
     * 通知任何注册的监视器移除当前在 {@code parentPosition} 的父列表项，
     * 之前在该位置之下并存在的所有父列表项将被移动到 {@code oldPosition - 1} 位置。
     * <p>
     * 这是数据结构上的变化，尽管位置变化了，但是之前存在的所有列表项在数据集里的数据都会被认为最新，
     * 因此这些列表项不会被重新绑定数据
     * </p>
     * @param parentPosition 要移除的父列表项在父列表里的位置
     *  @see #notifyParentItemRangeRemoved(int, int)
     */
    public final void notifyParentItemRemoved(int parentPosition) {
        notifyParentItemRangeRemoved(parentPosition,1);
    }

    /**
     * 通知任何注册的监视器移除当前在 {@code parentPosition,childPosition} 位置的子列表项，
     * 之前在该位置之下并存在的所有子列表项将被移动到 {@code oldPosition - 1} 位置。
     * <p>
     * 这是数据结构上的变化，尽管位置变化了，但是之前存在的所有列表项在数据集里的数据都会被认为最新，
     * 因此这些列表项不会被重新绑定数据
     * </p>
     * @param parentPosition 要移除的子列表项所属的父列表项的位置
     * @param childPosition 要移除的子列表项的位置
     * @see #notifyChildItemRangeRemoved(int, int, int)
     */
    public final void notifyChildItemRemoved(int parentPosition, int childPosition) {
        notifyChildItemRemoved(parentPosition, childPosition, false);
    }

    public final void notifyChildItemRemoved(int parentPosition, int childPosition,
            boolean forceCollapseParent)
    {
        notifyChildItemRangeRemoved(parentPosition, childPosition, 1, forceCollapseParent);
    }

    /**
     * 通知任何注册的监视器移除当前从 {@code parentPositionStart} 位置开始 {@code parentItemCount} 个 的父列表项，
     * 之前在该位置之下并存在的所有父列表项将被移动到 {@code oldPosition - parentItemCount} 位置。
     * <p>
     * 这是数据结构上的变化，尽管位置变化了，但是之前存在的所有列表项在数据集里的数据都会被认为最新，
     * 因此这些列表项不会被重新绑定数据
     * </p>
     * @param parentPositionStart 移除多个父列表项开始的位置
     * @param parentItemCount 移除父列表项的个数
     * @see #notifyParentItemRemoved(int)
     */
    public final void notifyParentItemRangeRemoved(int parentPositionStart, int parentItemCount) {
        int parentAdapterPosStart = getParentAdapterPosition(parentPositionStart);
        if (parentAdapterPosStart == RecyclerView.NO_POSITION) return;
        //计算移除的 ItemView 个数
        int removedItemCount = 0;
        for (int i = parentPositionStart; i < parentPositionStart + parentItemCount; i++) {
            ParentItemWrapper parentItemWrapper = (ParentItemWrapper) getItem(
                    parentAdapterPosStart);
            //判断当前 ParentItem 是否为展开状态
            if (parentItemWrapper.isExpanded()) {
                List<?> childItems = parentItemWrapper.getChildItems();
                if (childItems != null) {
                    int childItemCount = childItems.size();
                    mItems.removeAll(childItems);
                    removedItemCount += childItemCount;
                }
            }
            mItems.remove(parentItemWrapper);
            removedItemCount++;
        }
        notifyItemRangeRemoved(parentAdapterPosStart, removedItemCount);
    }

    public final void notifyChildItemRangeRemoved(int parentPosition, int childPositionStart,
            int childItemCount)
    {
        notifyChildItemRangeRemoved(parentPosition, childPositionStart, childItemCount, false);
    }

    /**
     * 通知任何注册的监视器移除当前从 {@code parentPosition,childPositionStart} 位置 {@code childItemCount} 个子列表项，
     * 之前在该位置之下并存在的所有子列表项将被移动到 {@code oldPosition - childItemCount} 位置。
     * <p>
     * 这是数据结构上的变化，尽管位置变化了，但是之前存在的所有列表项在数据集里的数据都会被认为最新，
     * 因此这些列表项不会被重新绑定数据
     * </p>
     * @param parentPosition 要移除的多个子列表项所属的父列表项的位置
     * @param childPositionStart 移除多个子列表项所开始的位置
     * @param childItemCount 移除的子列表项的个数
     * @see #notifyChildItemRemoved(int, int)
     */
    public final void notifyChildItemRangeRemoved(int parentPosition, int childPositionStart,
            int childItemCount,boolean forceCollapseParent)
    {
        int parentAdapterPos = getParentAdapterPosition(parentPosition);
        if (parentAdapterPos == RecyclerView.NO_POSITION) return;

        ParentItemWrapper parentItemWrapper = (ParentItemWrapper) getItem(parentAdapterPos);
        List<?> childItems = parentItemWrapper.getChildItems();

        //注意：这里判断当前父列表项是否已经打开，只有打开更改本地数据结构并通知刷新，否则会出现数据混乱异常
        if (!parentItemWrapper.isExpanded()) return;

        int childAdapterPosStart = getChildAdapterPosition(parentPosition, childPositionStart);
        if (childAdapterPosStart==RecyclerView.NO_POSITION) return;

        mItems.removeAll(mItems.subList(childAdapterPosStart,childAdapterPosStart+childItemCount));
        notifyItemRangeRemoved(childAdapterPosStart, childItemCount);

        boolean allChildRemoved = childItems == null || childItems.isEmpty();
        //强制在移除 child 时自动折叠 parent
        if (forceCollapseParent && !allChildRemoved) {
            collapseViews(parentAdapterPos, true);
        } else {
            //如果子列表项都删除了，默认通知这些删除的子列表项所属的父列表项已变为折叠状态
            if (allChildRemoved) {
                parentItemWrapper.setExpanded(false);
                //同步 ParentViewHolder 和 ParentItemWrapper 的 ParentItem 展开状态
                syncViewCollapseState(parentAdapterPos, true);
                //通知所有监听 Parent 展开折叠状态监听器当前 Parent 已折叠
                notifyParentCollapsed(parentAdapterPos, false);
            }
        }
    }

    /**
     * 通知任何注册的监视器更新当前在 {@code parentPosition} 位置的父列表项，
     *
     * <p>
     * 这是列表项相关的数据的改变，不是数据结构上的变化，因此，该列表项在适配器数据集里对应位置的数据已经过期，
     * 需要更新该列表项所绑定的数据
     * </p>
     * @param parentPosition 要更新的父列表项在父列表里的位置
     *
     */
    //FIXME Bug
    //TODO 是否需要处理更改后的 parent 的 children 和展开状态
    public final void notifyParentItemChanged(int parentPosition) {
        ParentItem changedParentItem = mParentItems.get(parentPosition);
        if (changedParentItem == null) return;
        int parentAdapterPos = getParentAdapterPosition(parentPosition);
        if (parentAdapterPos == RecyclerView.NO_POSITION) return;
        ParentItemWrapper parentItemWrapper = (ParentItemWrapper) getItem(parentAdapterPos);
        parentItemWrapper.setParentItem(changedParentItem);
        notifyItemChanged(parentAdapterPos);
    }

    /**
     * 通知任何注册的件事情在位置 {@code parentPositionStart} 上有 {@code parentItemCount} 个数据发生改变
     * <p>相当于调用 {@code notifyParentItemRangeChanged(parentPositionStart,parentItemCount,null)}</p>
     * <p>这是列表项相关的数据的改变，不是数据结构上的变化，因此，该列表项在适配器数据集里对应位置的数据已经过期，
     * 需要更新该列表项所绑定的数据,在该范围里的列表保留相同的 Id</p>
     * @param parentPositionStart 父列表项数据更改的起始位置
     * @param parentItemCount 父列表项数据更改的数量
     */
    public final void notifyParentItemRangeChanged(int parentPositionStart, int parentItemCount) {
        //判断当前是否所有的 ParentItem 都是折叠状态
        //1.如果都为折叠状态，直接调用 {@link notifyItemRangeChanged(positionStart,itemCount)}
        //2.只要有一个是打开状态，循环遍历调用 {@link notifyParentItemChanged(parentPosition)}
        boolean allCollapsed = true;
        for (int i = parentPositionStart; i < parentPositionStart + parentItemCount; i++) {
            int parentAdapterPos = getParentAdapterPosition(i);
            if (parentAdapterPos == RecyclerView.NO_POSITION) {
                allCollapsed = false;
                break;
            }
            ParentItemWrapper parentItemWrapper = (ParentItemWrapper) getItem(parentAdapterPos);
            if (parentItemWrapper.isExpanded()) {
                allCollapsed = false;
                break;
            }
        }
        if (allCollapsed) {
            final int parentAdapterPosStart = getParentAdapterPosition(parentPositionStart);
            notifyItemRangeChanged(parentAdapterPosStart, parentItemCount);
        } else {
            for (int i = parentPositionStart; i < parentPositionStart + parentItemCount; i++) {
                notifyParentItemChanged(i);
            }
        }
    }
    
    /**
     * 通知任何注册的监视器更新当前在 {@code parentPosition，childPosition} 位置的子列表项，
     * @param parentPosition 该子列表项所属的父列表项位置
     * @param childPosition 子列表项的位置
     */
    public final void notifyChildItemChanged(int parentPosition, int childPosition) {
        notifyChildItemRangeChanged(parentPosition,childPosition,1);
    }

    /**
     *  通知任何注册的件事情在位置 {@code parentPosition,childPositionStart} 上有 {@code childItemCount} 个数据发生改变
     * <p>相当于调用 {@code notifyParentItemRangeChanged(parentPositionStart,parentItemCount,null)}</p>
     * <p>这是列表项相关的数据的改变，不是数据结构上的变化，因此，该列表项在适配器数据集里对应位置的数据已经过期，
     * 需要更新该列表项所绑定的数据,在该范围里的列表保留相同的 Id</p>
     * @param parentPosition 子列表数据更改从属的父列表项位置
     * @param childPositionStart 子列表数据更改的起始子列表项位置
     * @param childItemCount 子列表数据更改的数量
     */
    public final void notifyChildItemRangeChanged(int parentPosition, int childPositionStart,
            int childItemCount)
    {
        int parentAdapterPos = getParentAdapterPosition(parentPosition);
        if (parentAdapterPos == RecyclerView.NO_POSITION) return;

        ParentItemWrapper parentItemWrapper = (ParentItemWrapper) getItem(parentAdapterPos);
        //父列表项没有展开，不执行更新子列表相关事务
        if (!parentItemWrapper.isExpanded()) return;
        List<?> childItems = parentItemWrapper.getChildItems();
        if (childItems==null || childItems.isEmpty()) return;

        int changedItemCount=0;
        for (int i = childPositionStart; i < childPositionStart + childItemCount; i++) {
            Object changedChildItem = childItems.get(i);
            if (changedChildItem == null) continue;
            int childAdapterPos = getChildAdapterPosition(parentPosition, i);
            if (childAdapterPos == RecyclerView.NO_POSITION) continue;
            mItems.set(childAdapterPos, changedChildItem);
            changedItemCount++;
        }
        final int childAdapterPosStart=getChildAdapterPosition(parentPosition, childPositionStart);
        notifyItemRangeChanged(childAdapterPosStart,changedItemCount);
    }

    /**
     * 通知任何注册的监视器在 {@code fromParentPosition} 位置的父列表项已经移动到 {@code toParentPosition}位置，
     * <p>
     * 这是数据结构上的变化，尽管位置变化了，但是之前存在的所有列表项在数据集里的数据都会被认为最新，
     * 因此这些列表项不会被重新绑定数据
     * </p>
     * @param fromParentPosition 该父列表项项之前的位置
     * @param toParentPosition 移动后的位置
     */
    public final void notifyParentItemMoved(int fromParentPosition, int toParentPosition)
    {
        if (fromParentPosition == toParentPosition) return;

        int fromParentAdapterPos = getParentAdapterPosition(fromParentPosition);
        int toParentAdapterPos = getParentAdapterPosition(toParentPosition);
        if (fromParentAdapterPos == RecyclerView.NO_POSITION ||
                toParentAdapterPos == RecyclerView.NO_POSITION) return;

        ParentItemWrapper fromParentItemWrapper= (ParentItemWrapper) getItem(fromParentAdapterPos);
        ParentItemWrapper toParentItemWrapper= (ParentItemWrapper) getItem(toParentAdapterPos);
        final boolean isFromExpanded=fromParentItemWrapper.isExpanded();
        final boolean isToExpanded=toParentItemWrapper.isExpanded();
        //Parent 或 Child 往下 Move 需要特殊处理
        boolean moveToBottom = fromParentPosition < toParentAdapterPos;

        mItems.remove(fromParentAdapterPos);
        int moveToParentAdapterPos;
        //这里需要判断 toParentItem 展开状态来计算出 fromParentItem 的 moveTo 位置
        if (moveToBottom && isToExpanded && toParentItemWrapper.getChildItemCount() > 0) {
            moveToParentAdapterPos=toParentAdapterPos + toParentItemWrapper.getChildItemCount();
        } else {
            moveToParentAdapterPos=toParentAdapterPos;
        }
        mItems.add(moveToParentAdapterPos, fromParentItemWrapper);
        notifyItemMoved(fromParentAdapterPos,moveToParentAdapterPos);

        //根据 fromParentItem 的展开状态判断是否需要移动其 ChildItem(s)
        if (isFromExpanded) {
            List<?> childItems = fromParentItemWrapper.getChildItems();
            if (childItems==null || childItems.isEmpty()) return;

            final int childCount=childItems.size();
            for (int i = 0; i < childCount; i++) {
                Object fromChildItem=childItems.get(i);
                if (fromChildItem==null) continue;
                int fromChildAdapterPos =
                        moveToBottom ? fromParentAdapterPos  : fromParentAdapterPos + i + 1;
                int toChildAdapterPos = moveToBottom ? moveToParentAdapterPos
                        : moveToParentAdapterPos + i + 1;
                mItems.remove(fromChildAdapterPos);
                mItems.add(toChildAdapterPos, fromChildItem);
                notifyItemMoved(fromChildAdapterPos, toChildAdapterPos);
            }
        }
    }

    /**
     * 通知任何注册的监视器在 {@code fromParentPosition,fromChildPosition} 位置的子列表项已经移动到 {@code
     * toParentPosition,toChildPosition}位置，
     * <p>
     * 这是数据结构上的变化，尽管位置变化了，但是之前存在的所有列表项在数据集里的数据都会被认为最新，
     * 因此这些列表项不会被重新绑定数据
     * </p>
     * @param fromParentPosition 子列表项移动所在的父列表项的起始位置
     * @param fromChildPosition 子列表项移动的起始位置
     * @param toParentPosition 子列表项移动所在的父列表项的目标位置
     * @param toChildPosition 子列表项移动的目标位置
     */
    public final void notifyChildItemMoved(int fromParentPosition, int fromChildPosition,
            int toParentPosition, int toChildPosition)
    {
        if (fromParentPosition == toParentPosition && fromChildPosition == toChildPosition) return;
        int fromParentAdapterPos = getParentAdapterPosition(fromParentPosition);
        int fromChildAdapterPos = getChildAdapterPosition(fromParentPosition, fromChildPosition);
        int toParentAdapterPos = getParentAdapterPosition(toParentPosition);
        int toChildAdapterPos = getChildAdapterPosition(toParentPosition, toChildPosition);
        if (fromParentAdapterPos == RecyclerView.NO_POSITION ||
                fromChildAdapterPos == RecyclerView.NO_POSITION ||
                toParentAdapterPos == RecyclerView.NO_POSITION ||
                toChildAdapterPos == RecyclerView.NO_POSITION)
            return;

        ParentItemWrapper fromParentItemWrapper = (ParentItemWrapper) getItem(fromParentAdapterPos);
        ParentItemWrapper toParentItemWrapper = (ParentItemWrapper) getItem(toParentAdapterPos);
        if (fromParentItemWrapper.isExpanded()) {
            if (toParentItemWrapper.isExpanded()) {
                Object fromChildItem=getItem(fromChildAdapterPos);
                mItems.remove(fromChildAdapterPos);
                mItems.add(toChildAdapterPos,fromChildItem);
                notifyItemMoved(fromChildAdapterPos,toChildAdapterPos);
            } else {
                notifyChildItemRemoved(fromParentPosition, fromChildPosition);
            }
        } else if (toParentItemWrapper.isExpanded()) {
            notifyChildItemInserted(toParentPosition, toChildPosition,false);
        } else {
            // from 和 to parentItem 都没有展开,do nothing
        }
    }

    //———————————————————————————保存 Parent 的展开状态———————————————————————————————————————\\

    /**
     * 保存 Parent 的展开状态
     * <p>在屏幕旋转或者退出应用时保存当前所有 Parent 的展开状态</p>
     * @param outState 存储需要保存的数据的 Bundle
     */
    //TODO 保存并恢复当前 expandCollapseMode 为非 default 时的先前 折叠/展开 的 parentItem 位置信息
    public void onSaveInstanceState(Bundle outState) {
        if (outState==null) return;
        Logger.e(TAG, "onSaveInstanceState");
        outState.putParcelable(SAVED_EXPANSION_STATE,getSavedState());
    }

    private SavedState getSavedState() {
        boolean[] expansionState = new boolean[getParentCount()];
        SavedState savedState = new SavedState(expansionState);
        final int itemCount = mItems.size();
        int index=0;
        for (int i = 0; i < itemCount; i++) {
            Object item = getItem(i);
            if (item instanceof ParentItemWrapper) {
                ParentItemWrapper parentItemWrapper = (ParentItemWrapper) item;
                expansionState[index++] = parentItemWrapper.isExpanded();
            }
        }
        return savedState;
    }

    private int getParentCount() {
        int count = 0;
        for (Object item : mItems) {
            if (item instanceof ParentItemWrapper) {
                count++;
            }
        }
        return count;
    }

    /**
     * 恢复 Parent 的展开状态
     * <p>如果恢复时 parent 数据与之前保存数据的不对应，那么默认按照原先保存的状态去恢复</p>
     * @param savedInstanceState 之前保存过的数据的 Bundle
     */
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState == null) return;
        SavedState savedState = savedInstanceState.getParcelable(SAVED_EXPANSION_STATE);
        if (savedState==null) return;
        boolean[] savedExpansionState=savedState.getExpansionState();
        if (savedExpansionState==null) return;

        Logger.e(TAG, "onRestoreInstanceState");

        List<Object> savedItems = new ArrayList<>();
        final int savedCount=savedExpansionState.length;
        final int parentCount=mParentItems.size();
        for (int i = 0; i < parentCount; i++) {
            ParentItem parentItem=mParentItems.get(i);
            if (parentItem==null) continue;
            ParentItemWrapper parentItemWrapper=new ParentItemWrapper(parentItem);
            savedItems.add(parentItemWrapper);

            if (i < savedCount) {
               final boolean isExpanded=savedExpansionState[i];
                if (isExpanded) {
                    List<?> childItems = parentItemWrapper.getChildItems();
                    final boolean hasChildren = childItems != null && !childItems.isEmpty();
                    if (!hasChildren) continue;
                    parentItemWrapper.setExpanded(true);
                    final int childCount = childItems.size();
                    for (int j = 0; j < childCount; j++) {
                        Object childListItem = childItems.get(j);
                        if (childListItem == null) continue;
                        savedItems.add(childListItem);
                    }
                }
            }
        }
        mItems = savedItems;
        notifyDataSetChanged();
    }

    /**
     * ExpandableRecyclerView 展开折叠模式
     * <p>
     *     支持以下模式:
     *     <ul>
     *         <li>MODE_DEFAULT:默认模式，可以展开折叠任意数量 parentItem,不受约束</li>
     *         <li>MODE_SINGLE_EXPAND:该模式下只能有一个 parentItem 处于展开状态，展开最新的 parentItem 会自动折叠先前展开的
     *         parentItem</li>
     *         <li>MODE_SINGLE_COLLAPSE:该模式下只能有一个 parentItem 处于折叠状态，折叠最新的 parentItem 会自动展开先前折叠的
     *         parentItem</li>
     *     </ul>
     * </p>
     */
    public class ExpandCollapseMode {
        /**
         * 默认模式，可以展开折叠任意数量 parentItem,不受约束
         */
        public static final int MODE_DEFAULT = 0;
        /**
         * 该模式下只能有一个 parentItem 处于展开状态，展开最新的 parentItem 会自动折叠先前展开的 parentItem
         */
        public static final int MODE_SINGLE_EXPAND = 1;
        /**
         * 该模式下只能有一个 parentItem 处于折叠状态，折叠最新的 parentItem 会自动展开先前折叠的 parentItem
         */
        public static final int MODE_SINGLE_COLLAPSE = 2;
        /**
         * 当前设置的模式
         */
        private int mode = MODE_DEFAULT;

        /**
         * 先前展开的 parentItem 在模型数据集里对应的位置
         */
        private int lastExpandedPosition = RecyclerView.NO_POSITION;
        /**
         * 先前展开的 parentItem 在本地适配器里对应的位置
         */
        private int lastExpandedAdapterPosition = RecyclerView.NO_POSITION;
        /**
         * 先前折叠的 parentItem 在模型数据集里对应的位置
         */
        private int lastCollapsedPosition = RecyclerView.NO_POSITION;
        /**
         * 先前折叠的 parentItem 在本地适配器里对应的位置
         */
        private int lastCollapsedAdapterPosition = RecyclerView.NO_POSITION;

    }
}


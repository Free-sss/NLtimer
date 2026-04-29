package com.nltimer.feature.home.model

/**
 * 首页整体 UI 状态的聚合。
 * 控制加载、空闲模式、添加/详情弹窗、网格行数据等全局状态。
 */
data class HomeUiState(
    val rows: List<GridRowUiState> = emptyList(), // 所有网格行
    val currentRowId: String? = null, // 当前活跃行 ID
    val isAddSheetVisible: Boolean = false, // 是否显示添加行为底部弹窗
    val selectedTimeHour: Int = 0, // 选中的小时，用于滚动定位
    val isLoading: Boolean = true, // 是否正在加载数据
    val isIdleMode: Boolean = false, // 是否处于空闲模式
    val hasActiveBehavior: Boolean = false, // 是否有正在进行的活跃行为
    val isDetailSheetVisible: Boolean = false, // 是否显示详情底部弹窗
    val detailBehavior: BehaviorDetailUiState? = null, // 当前查看的详情行为
    val isSaving: Boolean = false, // 是否正在保存中
    val errorMessage: String? = null,
)

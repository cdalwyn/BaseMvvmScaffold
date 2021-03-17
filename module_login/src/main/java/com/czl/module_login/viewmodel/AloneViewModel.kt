package com.czl.module_login.viewmodel

import com.czl.lib_base.base.BaseViewModel
import com.czl.lib_base.base.MyApplication
import com.czl.lib_base.data.DataRepository

/**
 * @author Alwyn
 * @Date 2020/10/21
 * @Description
 */
class AloneViewModel(application: MyApplication, model: DataRepository) :
    BaseViewModel<DataRepository>(application, model) {

    override fun setToolbarRightClick() {
        showNormalToast("标题右侧点击事件")
    }
}
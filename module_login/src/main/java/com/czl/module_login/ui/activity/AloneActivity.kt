package com.czl.module_login.ui.activity

import com.czl.lib_base.base.BaseActivity
import com.czl.module_login.BR
import com.czl.module_login.R
import com.czl.module_login.databinding.LoginActivityAloneBinding
import com.czl.module_login.viewmodel.AloneViewModel

class AloneActivity : BaseActivity<LoginActivityAloneBinding,AloneViewModel>(){
    override fun initContentView(): Int {
        return R.layout.login_activity_alone
    }

    override fun initVariableId(): Int {
        return BR.viewModel
    }

    override fun initData() {
        super.initData()
        viewModel.apply {
            btnBackVisibility.set(false)
            toolbarRightText.set("设置")
            tvTitle.set("测试")
        }
    }

}
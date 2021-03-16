package com.czl.lib_base.data.source.impl

import com.czl.lib_base.base.BaseBean
import com.czl.lib_base.data.api.ApiService
import com.czl.lib_base.data.bean.UserBean
import com.czl.lib_base.data.source.HttpDataSource
import io.reactivex.Observable

/**
 * @author Alwyn
 * @Date 2020/7/22
 * @Description
 */
class HttpDataImpl(private val apiService: ApiService) : HttpDataSource {

    override fun userLogin(account: String, pwd: String): Observable<BaseBean<UserBean>> {
        return apiService.pwdLogin(account, pwd)
    }

}
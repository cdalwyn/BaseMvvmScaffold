package com.czl.lib_base.config

/**
 * @author Alwyn
 * @Date 2020/10/22
 * @Description 常量管理类
 */
interface AppConstants {
    object SpKey {

    }

    /**
     * value规则： /(module后缀)/(所在类名)
     * 路由 A_ : Activity
     *     F_ : Fragment
     */
    interface Router {

        object Login {
            const val F_LOGIN = "/login/LoginFragment"
        }

    }
}
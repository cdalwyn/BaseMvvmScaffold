package com.czl.lib_base.base

import android.app.Activity
import androidx.fragment.app.Fragment
import java.util.*

/**
 * activity堆栈式管理
 */
class AppManager private constructor() {
    /**
     * 添加Activity到堆栈
     */
    fun addActivity(activity: Activity) {
        if (activityStack == null) {
            activityStack = Stack()
        }
        activityStack!!.add(activity)
    }

    /**
     * 移除指定的Activity
     */
    fun removeActivity(activity: Activity?) {
        if (activity != null) {
            activityStack!!.remove(activity)
        }
    }

    /**
     * 是否有activity
     */
    val isActivity: Boolean
        get() = if (activityStack != null) {
            !activityStack!!.isEmpty()
        } else false

    /**
     * 获取当前Activity（堆栈中最后一个压入的）
     */
    fun currentActivity(): Activity {
        return activityStack!!.lastElement()
    }

    /**
     * 结束当前Activity（堆栈中最后一个压入的）
     */
    fun finishActivity() {
        val activity = activityStack!!.lastElement()
        finishActivity(activity)
    }

    /**
     * 结束指定的Activity
     */
    fun finishActivity(activity: Activity?) {
        if (activity != null) {
            if (!activity.isFinishing) {
                activity.finish()
            }
        }
    }

    /**
     * 结束指定类名的Activity
     */
    fun finishActivity(cls: Class<*>) {
        for (activity in activityStack!!) {
            if (activity.javaClass == cls) {
                finishActivity(activity)
                break
            }
        }
    }

    /**
     * 结束所有Activity
     */
    fun finishAllActivity() {
        for (activity in activityStack!!) {
            finishActivity(activity)
        }
    }

    /**
     * 获取指定的Activity
     *
     * @author
     */
    fun getActivity(cls: Class<*>): Activity? {
        if (activityStack != null) for (activity in activityStack!!) {
            if (activity.javaClass == cls) {
                return activity
            }
        }
        return null
    }


    /**
     * 退出应用程序
     */
    fun appExit() {
        try {
            finishAllActivity()
            // 杀死该应用进程
            android.os.Process.killProcess(android.os.Process.myPid())
//            调用 System.exit(n) 实际上等效于调用：
//            Runtime.getRuntime().exit(n)
//            finish()是Activity的类方法，仅仅针对Activity，当调用finish()时，只是将活动推向后台，并没有立即释放内存，活动的资源并没有被清理；当调用System.exit(0)时，退出当前Activity并释放资源（内存），但是该方法不可以结束整个App如有多个Activty或者有其他组件service等不会结束。
//            其实android的机制决定了用户无法完全退出应用，当你的application最长时间没有被用过的时候，android自身会决定将application关闭了。
            //System.exit(0);
        } catch (e: Exception) {
            activityStack!!.clear()
            e.printStackTrace()
        }
    }

    companion object {
        var activityStack: Stack<Activity>? = null
            private set
        /**
         * 单例模式
         *
         * @return AppManager
         */
        val instance: AppManager by lazy { AppManager() }
    }
}
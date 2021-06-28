package com.czl.lib_base.base

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModelProvider
import com.czl.lib_base.R
import com.czl.lib_base.mvvm.ui.ContainerFmActivity
import com.czl.lib_base.route.RouteCenter
import com.czl.lib_base.util.DayModeUtil
import com.czl.lib_base.util.ToastHelper
import com.gyf.immersionbar.ImmersionBar
import me.yokeyword.fragmentation.SupportFragment
import org.koin.android.ext.android.get
import java.lang.reflect.ParameterizedType


/**
 * Created by Alwyn on 2020/10/10.
 */
abstract class BaseFragment<V : ViewDataBinding, VM : BaseViewModel<*>> :
    BaseRxFragment(), IBaseView {
    lateinit var binding: V
    lateinit var viewModel: VM
    private var viewModelId = 0
    private lateinit var rootView: View
    protected var rootBinding: ViewDataBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initParam()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (useBaseLayout()) {
            rootView = inflater.inflate(R.layout.activity_base, null, false)
                .findViewById(R.id.activity_root)
            rootBinding = DataBindingUtil.bind(rootView)
            binding =
                DataBindingUtil.inflate(inflater, initContentView(), rootView as ViewGroup, true)
            return rootView
        } else {
            binding = DataBindingUtil.inflate(inflater, initContentView(), container, false)
            return binding.root
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        ImmersionBar.destroy(this)
        //解除Messenger注册
//        Messenger.getDefault().unregister(viewModel)
        binding.unbind()
        rootBinding?.unbind()
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        //私有的初始化DataBinding和ViewModel方法
        initViewDataBinding()
        //私有的ViewModel与View的契约事件回调逻辑
        registerUIChangeLiveDataCallBack()
    }

    /**
     * 正常创建启动Fragment情况 onViewCreated-onLazyInitView-onEnterAnimationEnd
     * Viewpager创建实例 onViewCreated-onLazyInitView
     */
    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        if (enableLazy()) {
            //页面数据初始化方法
            initData()
            //页面事件监听的方法，一般用于ViewModel层转到View层的事件注册
            initViewObservable()
        }
    }

    /**
     * 入栈动画完毕后执行
     */
    override fun onEnterAnimationEnd(savedInstanceState: Bundle?) {
        super.onEnterAnimationEnd(savedInstanceState)
        if (!enableLazy()) {
            //页面数据初始化方法
            initData()
            //页面事件监听的方法，一般用于ViewModel层转到View层的事件注册
            initViewObservable()
        }
    }

    override fun onSupportVisible() {
        super.onSupportVisible()
        if (isImmersionBarEnabled()) {
            initStatusBar()
        }
    }

    open fun initStatusBar() {
        ImmersionBar.with(this)
            .statusBarDarkFont(!DayModeUtil.isNightMode(requireContext()), 0.2f)
            .statusBarColor(R.color.color_toolbar).init()
    }

    /**
     * 注入绑定
     */
    private fun initViewDataBinding() {
        viewModelId = initVariableId()
        viewModel = initViewModel()
        rootBinding?.setVariable(viewModelId, viewModel)
        rootBinding?.lifecycleOwner = this
        binding.setVariable(viewModelId, viewModel)
        //支持LiveData绑定xml，数据改变，UI自动会更新
        binding.lifecycleOwner = this
        //让ViewModel拥有View的生命周期感应
        lifecycle.addObserver(viewModel)
        //注入RxLifecycle生命周期
        viewModel.injectLifecycleProvider(this)
    }

    /**
     * 注册ViewModel与View的契约UI回调事件
     */
    private fun registerUIChangeLiveDataCallBack() {
        //加载对话框显示
        viewModel.uC.getShowLoadingEvent()
            .observe(this, { title: String? -> showLoading(title) })
        //加载对话框消失
        viewModel.uC.getDismissDialogEvent()
            .observe(this, { dismissLoading() })
        //跳入新页面
        viewModel.uC.getStartActivityEvent().observe(this, { map ->
            val routePath: String = map[BaseViewModel.ParameterField.ROUTE_PATH] as String
            val bundle = map[BaseViewModel.ParameterField.BUNDLE] as Bundle?
            RouteCenter.navigate(routePath, bundle)
        }
        )
        viewModel.uC.getStartFragmentEvent().observe(this, { map ->
            val routePath: String = map[BaseViewModel.ParameterField.ROUTE_PATH] as String
            val bundle: Bundle? = map[BaseViewModel.ParameterField.BUNDLE] as Bundle?
            start(RouteCenter.navigate(routePath, bundle) as SupportFragment)
        })
        //跳入ContainerActivity
        viewModel.uC.getStartContainerActivityEvent().observe(
            this, { params: Map<String?, Any?> ->
                val canonicalName = params[BaseViewModel.ParameterField.ROUTE_PATH] as String?
                val bundle = params[BaseViewModel.ParameterField.BUNDLE] as Bundle?
                val code = params[BaseViewModel.ParameterField.REQUEST_CODE] as Int?
                startContainerActivity(canonicalName, bundle, code)
            }
        )
        //关闭界面
        viewModel.uC.getFinishEvent().observe(this, {
            back()
        })
        //关闭上一层
        viewModel.uC.getOnBackPressedEvent().observe(
            this, { onBackPressedSupport() }
        )
    }

    /**
     * 是否开启懒加载,默认true
     *
     * @return
     */
    protected open fun enableLazy(): Boolean {
        return true
    }

    /**
     * 统一处理回退事件
     */
    open fun back() {
        if (preFragment == null) {
            requireActivity().finish()
        } else {
            pop()
        }
    }

    fun showLoading(title: String?) {
        // todo 加载中
    }

    fun dismissLoading() {
        // todo 关闭加载
    }

    fun showErrorToast(msg: String?) {
        ToastHelper.showErrorToast(msg)
    }

    fun showNormalToast(msg: String?) {
        ToastHelper.showNormalToast(msg)
    }

    fun showSuccessToast(msg: String?) {
        ToastHelper.showSuccessToast(msg)
    }

    /**
     * 跳转页面
     *
     * @param clz 所跳转的目的Activity类
     */
    fun startActivity(clz: Class<*>?) {
        startActivity(Intent(context, clz))
    }

    /**
     * 跳转页面
     *
     * @param clz    所跳转的目的Activity类
     * @param bundle 跳转所携带的信息
     */
    fun startActivity(clz: Class<*>?, bundle: Bundle?) {
        val intent = Intent(context, clz)
        if (bundle != null) {
            intent.putExtras(bundle)
        }
        startActivity(intent)
    }

    /**
     * 跳转容器页面
     * @param routePath Fragment路由地址
     * @param bundle        跳转所携带的信息
     */
    fun startContainerActivity(
        routePath: String?,
        bundle: Bundle? = null, reqCode: Int? = null
    ) {
        val intent = Intent(context, ContainerFmActivity::class.java)
        intent.putExtra(ContainerFmActivity.FRAGMENT, routePath)
        if (bundle != null) intent.putExtra(ContainerFmActivity.BUNDLE, bundle)
        if (reqCode == null)
            startActivity(intent)
        else
            startActivityForResult(intent, reqCode)
    }

    //刷新布局
    fun refreshLayout() {
        binding.setVariable(viewModelId, viewModel)
    }

    /**
     * @return 是否需要标题栏
     */
    protected open fun useBaseLayout(): Boolean {
        return true
    }

    override fun initParam() {}

    /**
     * 初始化根布局
     *
     * @return 布局layout的id
     */
    abstract fun initContentView(): Int


    /**
     * 初始化ViewModel的id
     *
     * @return BR的id
     */
    abstract fun initVariableId(): Int

    /**
     * 动态实例化ViewModel
     *
     * @return 继承BaseViewModel的ViewModel
     */
    private fun initViewModel(): VM {
        val type = javaClass.genericSuperclass
        val modelClass: Class<VM> =
            (type as ParameterizedType).actualTypeArguments[1] as Class<VM>
        return ViewModelProvider(this, get() as AppViewModelFactory).get(modelClass)
    }

    override fun initData() {}

    override fun initViewObservable() {}

    /**
     * 如果使用带有标题栏的布局，则不是带有
     */
    open fun isImmersionBarEnabled(): Boolean {
        return useBaseLayout()
    }
}
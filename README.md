# BaseMvvmScaffold

**集成脚手架的完整项目，功能更多，可玩性更高，请移步： **[玩安卓Mvvm组件化app](https://github.com/cdalwyn/mvvmcomponent)

## 介绍

旨在更方便快速集成地集成到新项目中，在[玩安卓Mvvm组件化app](https://github.com/cdalwyn/mvvmcomponent)中抽出通用组件包，撇去业务资源和非必要第三方库，包装成更通用、更易上手的Mvvm组件化脚手架。

**特点：**

- 遵循Google推荐的Mvvm架构，Model层负责将请求的数据交给ViewModel，ViewModel层负责将请求到的数据做业务逻辑处理，最后交给View层去展示，Model层使用Koin库依赖注入全局内一行代码即可调用数据仓库获取本地缓存以及远程数据，规避数据滥用、维护困难等问题；
- 采用单一容器ContainerActivity+多Fragment配合Fragmentation库、阿里ARouter通信跳转实现单activity多fragment组件化架构。业务开发中，只需编写Fragment，避免每个界面都在AndroidManifest中注册；
- 在基类Fragment初始化界面时添加通用标题栏，样式可自定义，一行代码/一个重写方法即可控制标题栏；
- 结合Mvvm+DataBinding特性，再也不用setOnClickListener和setText了，自定义或第三方控件仅需编写带@BindingAdapter注解方法即可在xml内完成监听绑定；(内置了多种控件绑定方法，如全局点击事件防抖，防止点击过快)
- …………

## 快速上手

###  **准备工作**

将**lib_base**模块导入，把**config.gradle(依赖库信息)、module.build.gradle(组件通用gradle配置)**加入到根目录下，里面主要配置都有注释，具体参考Demo。

在根目录gradle.properties文件中添加：

```
# 是否作为独立App应用运行
isBuildModule=false
```

在根目录build.gradle第一行加入：

```
apply from: "config.gradle"
```

### **第一个Activity**

> 以大家都熟悉的启动页为例：三个文件SplashActivity，SplashViewModel，login_activity_splash

- **关联ViewModel**

![](https://github.com/cdalwyn/BaseMvvmScaffold/blob/master/readme/bindinglayout.png)

开启了dataBinding后，Convert to data binding layout后会自动生成根据文件命名顺序的驼峰命名文件LoginActivitySplashBinding，若找不到则Rebuild Project即可

![](https://github.com/cdalwyn/BaseMvvmScaffold/blob/master/readme/bindViewModel.png)

添加了如下代码进行关联ViewModel:

```
<data>
        <variable
            name="viewModel"
            type="com.czl.module_login.viewmodel.SplashViewModel" />
    </data>
```

> variable - type：类的全路径
> variable - name：变量名

- **继承BaseActivity**

```
class SplashActivity : BaseActivity<LoginActivitySplashBinding, SplashViewModel>() {
    override fun initContentView(): Int {
        return R.layout.login_activity_splash
    }
    
    override fun initVariableId(): Int {
        return BR.viewModel
    }
}
```

BaseActivity是一个抽象类，有两个泛型参数，一个是ViewDataBinding，另一个是BaseViewModel，xml自动生成的LoginActivitySplashBinding则是继承的ViewDataBinding作为第一个泛型约束，SplashViewModel继承BaseViewModel作为第二个泛型约束。

重写的2个方法 :

 initContentView() -> 返回界面layout ; 

initVariableId() -> 对应layout文件中name="viewModel"，这里的BR跟R文件一样，由系统生成，使用BR.xxx找到这个ViewModel的id

> Fragment继承BaseFragment 同理

打开新Fragment只需调用：

```
// AppConstants.Router.Login.F_LOGIN 是ARouter库配置的新Fragment路由地址
startContainerActivity(AppConstants.Router.Login.F_LOGIN)
```

### **第三方控件绑定事件写法**

> 以刷新控件SmartRefreshLayout为例

1. 先编写事件绑定的适配器

   

```
object ViewAdapter {
    //下拉刷新命令 Kotlin需添加@JvmStatic注解 
    // "onRefreshCommand" xml绑定的属性名
    // @BindingAdapter详解自行查询 此处仅介绍用法
    @JvmStatic
    @BindingAdapter("onRefreshCommand")
    fun onRefreshCommand(
        smartRefreshLayout: SmartRefreshLayout,
        onRefreshCommand: BindingCommand<*>?
    ) {
        smartRefreshLayout.setOnRefreshListener { refreshLayout: RefreshLayout? -> onRefreshCommand?.execute() }
    }
}
```

2. xml布局绑定

   ```
   <layout 
   ......
   xmlns:binding="http://schemas.android.com/tools">
   <com.scwang.smart.refresh.layout.SmartRefreshLayout
           ......
           binding:onRefreshCommand="@{viewModel.onRefreshListener}">
   ```

3. ViewModel层调用

   ```
   val onRefreshListener: BindingCommand<Void> = BindingCommand(BindingAction {  
   // 下拉刷新
    })
   ```

   若第三方控件需携带参数，如TabLayout:

   ```
   // 适配器写法
    @JvmStatic
       @BindingAdapter("onTabSelectedCommand")
       fun onTabSelectedCommand(tabLayout: TabLayout, bindingCommand: BindingCommand<Int>?) {
           tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
               override fun onTabSelected(tab: TabLayout.Tab) {
                   bindingCommand?.execute(tab.position)
               }
               override fun onTabUnselected(tab: TabLayout.Tab) {}
               override fun onTabReselected(tab: TabLayout.Tab) {}
           })
       }
   ```

   ViewModel层调用写法：

   ```
   val onTabSelectedCommand: BindingCommand<Int> = BindingCommand(BindingConsumer {
           LogUtil.e("选中:$it")
       })
   ```

### 辅助工具类

- PermissionUtil :  配合郭霖大神出品的PermissionX的权限管理类
- SpHelper：配合腾讯出品的MMKV高性能本地缓存帮助类
- ToastHelper：配合Toasty库的吐司管理类
- ......

## 感谢

- [Koin: 实用的轻量级依赖注入框架](https://github.com/InsertKoinIO/koin)
- [LiveEventBus: 基于LiveData生命周期安全的消息总线](https://github.com/JeremyLiao/LiveEventBus)
- [MMKV：可替代Sharedpreferences高性能缓存库](https://github.com/Tencent/MMKV)
- [Toasty：好看且实用的吐司](https://github.com/GrenderG/Toasty)
- [PermissionX ：郭霖出品必属精品的权限管理](https://github.com/guolindev/PermissionX)
- ······
- **致敬所有为开源贡献的大佬！**

## License

```
   Copyright 2021 cdalwyn(陈志龙)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
```


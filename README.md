# PowerfulEditText
自定义EditText，带一键清除和密码明文切换按钮（可以传入自定义图片资源），可以自定义边框颜色，支持四种边框样式的选择，支持控件抖动动画

## 效果图
![](http://bmob-cdn-15184.b0.upaiyun.com/2017/12/10/0855b611408780c280d5ca436528361e.gif)

## xml配置
``` xml
    <com.wang.powerfuledittext.PowerfulEditText
        android:id="@+id/login_password"
        android:inputType="textPassword"
        app:clearDrawable="@drawable/clear_all"
        app:visibleDrawable="@drawable/visible"
        app:invisibleDrawable="@drawable/invisible"
        app:BtnWidth="@dimen/btn_edittext_width"
        app:BtnSpacing="@dimen/btn_edittext_padding"
        android:hint="@string/hint_password"
        android:layout_marginStart="20dp"
        android:layout_marginEnd="20dp"
        android:layout_marginTop="20dp"
        android:layout_width="match_parent"
        android:layout_height="wrap_content" />
```

## 边框样式的对应规则如下
* 矩形样式：	app:borderStyle="rectangle"
* 半矩形样式：app:borderStyle="halfRect"
* 圆角矩形样式：app:borderStyle="roundRect"
* 动画特效样式：app:borderStyle="animator"

## 摇晃
``` java
mPEditText.startShakeAnimation();
```

## 源码详解
请见博客：http://blog.csdn.net/kuaiguixs/article/details/78745075

# PowerfulEditText
自带一键清除按钮，密码显示与隐藏按钮，也可自定义资源。还有多种输入框样式可供选择

## 效果图
![](http://bmob-cdn-15184.b0.upaiyun.com/2017/12/07/bc6d46a940434dad8023c1567c3ef243.gif)

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

## 摇晃
``` java
mPEditText.startShakeAnimation();
```

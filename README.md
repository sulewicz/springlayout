<div align="center"><img src="./img/site_logo.png" alt="Spring Layout Logo"></div>

[![Android Arsenal](http://img.shields.io/badge/Android%20Arsenal-SpringLayout-blue.svg?style=flat)](http://android-arsenal.com/details/1/707)
### SpringLayout - RelativeLayout on steroids.

The goal of this project is to create more reliable and feature-rich replacement for RelativeLayout.
Apart from new features SpringLayout has better error reporting, so no more guessing what's wrong with your layout :-).

You can find the sample app on Google Play, just click on the logo below.

<div align="center"><a href="https://play.google.com/store/apps/details?id=org.coderoller.springlayoutsample"><img src="./img/play.png" alt="Google Play Logo"></a></div>

#### How to build

Simply update your gradle dependencies.

**build.gradle:**

```
dependencies {
    (...)
    compile('org.coderoller:springlayout:0.9.2')
}
```


#### Improvements compared to RelativeLayout

###### New attributes

**layout_alignCenterVertically:**

Align center of the view vertically to the center of specified view. 

**layout_alignCenterHorizontally:**

Align center of the view horizontally to the center of specified view. 

**layout_alignCenter:**

Align center of the view both vertically and horizontally to the center of specified view. 

**_Example:_**

![readme_example_center_alignment.png](./img/readme_example_center_alignment.png "Center Alignment Example")

```
<org.coderoller.springlayout.SpringLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="50dp"
    android:background="@android:color/white">

    <View
        android:id="@+id/A"
        android:layout_width="40dp"
        android:layout_height="40dp"
        android:background="#ffff0000"
        android:layout_marginLeft="50dp"
        app:layout_centerVertical="true"/>

    <View
        android:id="@+id/B"
        android:layout_width="30dp"
        android:layout_height="30dp"
        android:background="#ff00ff00"
        app:layout_alignCenter="@id/A"/>

    <View
        android:id="@+id/C"
        android:layout_width="20dp"
        android:layout_height="20dp"
        android:background="#ff0000ff"
        app:layout_alignCenter="@id/B"/>

</org.coderoller.springlayout.SpringLayout>
```

**layout_relativeWidth:**

Specify the width of the view in percentage relative to parent width (excluding padding).

**layout_relativeHeight:**

Specify the height of the view in percentage relative to parent height (excluding padding).

**_Example:_**

![readme_example_relative_size.png](./img/readme_example_relative_size.png "Relative Size Example")

```
<org.coderoller.springlayout.SpringLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="50dp"
    android:background="@android:color/white">

    <View
        android:id="@+id/A"
        android:layout_width="40dp"
        android:layout_height="40dp"
        android:background="#ffff0000"
        app:layout_centerInParent="true"
        app:layout_relativeWidth="75%"/>

    <View
        android:id="@+id/B"
        android:layout_width="30dp"
        android:layout_height="30dp"
        android:background="#ff00ff00"
        app:layout_alignCenter="@id/A"
        app:layout_relativeWidth="50%"/>

    <View
        android:id="@+id/C"
        android:layout_width="20dp"
        android:layout_height="20dp"
        android:background="#ff0000ff"
        app:layout_alignCenter="@id/B"
        app:layout_relativeWidth="25%"/>

</org.coderoller.springlayout.SpringLayout>
```
###### Forward referencing

Forward referencing simply works as expected.

Try achieving the same in RelativeLayout (good luck with it :-)).

```
<org.coderoller.springlayout.SpringLayout xmlns:android="http://schemas.android.com/apk/res/android"
        xmlns:app="http://schemas.android.com/apk/res-auto"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:background="@android:color/white" >

        <TextView
            android:id="@+id/X"
            android:layout_width="wrap_content"
            android:layout_height="20dp"
            android:background="#ffff0000"
            app:layout_alignParentLeft="true"
            android:text="X" />

        <TextView android:id="@+id/Y"
            android:layout_width="wrap_content"
            android:layout_height="20dp"
            app:layout_toRightOf="@id/X"
            app:layout_toLeftOf="@+id/Z"
            android:background="#ff00ff00"
            android:text="Y" />
        
        <TextView
            android:id="@id/Z"
            android:layout_width="wrap_content"
            android:layout_height="20dp"
            app:layout_alignParentRight="true"
            android:background="#ff0000ff"
            android:text="Z" />
    </org.coderoller.springlayout.SpringLayout>
```

###### Detailed error messages

SpringLayout will throw an exception when you do something wrong.
For example it will inform you when you duplicate view constraints or introduce a circular dependancy.

###### Adapting width and height parameters

SpringLayout adapts view params a bit before rendering.
For example if you specify the view height to be match_parent, then it implies that layout_alignParentTop and layout_alignParentBottom parameters are true. This works both ways, so if layout_alignParentTop and layout_alignParentBottom parameters are true then view height will be match_parent.

If the view has no vertical anchors defined, then it is set to alignParentTop.
Same goes for horizontal anchors. If the view has no horizontal anchors defined, then it is set to alignParentLeft. Simply, there is no point of having a view without an anchor.

The wrap_content parameter normally tells the SpringLayout to take the child desired size as the final one. However if you define both constraints in one axis for a view which has wrap_content size in that axis, then SpringLayout will expand that view to meet the contraints.

**_Example:_**

![readme_example_wrap_content_size.png](./img/readme_example_wrap_content_size.png "Wrap Content Size Example")

```
<org.coderoller.springlayout.SpringLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:background="@android:color/white">

    <View
        android:id="@+id/A"
        android:layout_width="40dp"
        android:layout_height="40dp"
        android:background="#ffff0000"
        app:layout_alignParentLeft="true"/>

    <View
        android:id="@+id/B"
        android:layout_width="40dp"
        android:layout_height="40dp"
        android:background="#ff00ff00"
        app:layout_alignParentRight="true"/>

    <View
        android:id="@+id/C"
        android:layout_width="wrap_content"
        android:layout_height="match_parent"
        android:background="#ff0000ff"
        app:layout_toRightOf="@id/A"
        app:layout_toLeftOf="@id/B"/>

</org.coderoller.springlayout.SpringLayout>
```


#### Width and height weights (aka Springs)

SpringLayout introduces two new attributes **layout_widthWeight** and **layout_heightWeight**.
Using them will introduce internal chain (horizontal or vertical) of views. The weight attribute tells SpringLayout how much of the empty space in the views chain the view should occupy. Weight attribute works only when corresponding layout size is WRAP_CONTENT.
Apart from convenient way of specyfing view size, you can use the new attributes with empty views to organize other views in the layout. Such empty views will be called Springs from now on.

Springs introduced in SpringLayout slightly resemble Springs from GroupLayout that is available in Swing UI. They allow you to organize views on the screen in more advanced fashion without the need to nest layouts.

Let's say you want to center two Views horizontally on the screen. With RelativeLayout you would do something like this:

```
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="60dp"
    android:background="@android:color/white">

    <LinearLayout
        android:layout_width="wrap_content"
        android:layout_height="match_parent"
        android:orientation="horizontal"
        android:layout_centerHorizontal="true">

        <TextView
            android:id="@+id/A"
            android:layout_width="wrap_content"
            android:layout_height="match_parent"
            android:text="@string/sample_text"
            android:background="#ffff0000"/>

        <View
            android:id="@+id/B"
            android:layout_width="20dp"
            android:layout_height="match_parent"
            android:background="#ff00ff00"/>
    </LinearLayout>

</RelativeLayout>
```

It works, but requires you to nest additional layout (that will only group those two views), which makes things slightly slower.
In SpringLayout you can achieve the same by using Springs:

![readme_example_springs_good.png](./img/readme_example_springs_good.png "Springs Good Example")

```
<org.coderoller.springlayout.SpringLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="60dp"
    android:background="@android:color/white">

    <View
        android:id="@+id/spring_A"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        app:layout_widthWeight="1"/>

    <TextView
        android:id="@+id/A"
        android:layout_width="wrap_content"
        android:layout_height="match_parent"
        android:background="#ffff0000"
        android:text="@string/sample_text"
        android:gravity="center"
        app:layout_toRightOf="@id/spring_A"/>

    <View
        android:id="@+id/B"
        android:layout_width="20dp"
        android:layout_height="match_parent"
        android:background="#ff00ff00"
        app:layout_toRightOf="@id/A"/>

    <View
        android:id="@+id/spring_B"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        app:layout_toRightOf="@id/B"
        app:layout_alignParentRight="true"
        app:layout_widthWeight="1" />

</org.coderoller.springlayout.SpringLayout>

```

Weight attributes work similar to **layout_weight** attribute that can be found in LinearLayout, however **layout_widthWeight** and **layout_heightWeight** apply only to empty space in the view chain. In the example above, the sum of weights in horizontal axis is 2, therefore **spring_A** and **spring_B** will both take 1/2 of empty space in the view chain left by views **A** and **B**.

**Please note:** 

Using **layout_widthWeight** or **layout_heightWeight** will internally introduce a chain of Views. In the example above we will have a horizontal chain consisting of views (in order): **spring_A**, **A**, **B**, **spring_B**. Based on that the SpringLayout knows that empty space available for Springs will be chain width minus **A** width and **B** width.

*Due to this fact, there are two things you have to keep in mind, when using these attributes:*

- View chain head has to have start anchor (left for horizontal, top for vertical) and chain tail has to have end anchor (right for horizontal, bottom for vertical) defined. In other case chain size cannot be calculated and exception will be thrown. That's why **spring_B** has alignParentRight defined (**spring_A** has no anchors defined therefore SpringLayout automatically defines alignParentTop and alignParentLeft for it).

- Views with weights are pointless (and won't work) when used inside a layout with wrap_content width or height (depends if the spring applies to vertical or horizontal chain), unless minWidth or minHeight parameter is specified. In other case empty space available to views with weights will be always 0. 

- Currently the internal chains cannot divert, however this might change in future. This means that if you introduce a new view **C** that will be placed right of view **B**, then **spring_B** won't be attached to the chain since view *B* is succeeded by view **C** in the chain. So for the layout listed below the horizontal chain will consist of **spring_A**, **A**, **B**, **C**.

To illustrate, the snippet below will throw the following exception:

```
java.lang.IllegalStateException: Horizontal weight defined but never used, please review your layout. Remember that the chain of views cannot divert when using springs: Problematic view (please also check other dependant views): android.view.View{a67c0e98 V.ED.... ......I. 0,0-0,0 #7f070009 app:id/C}, problematic layout: org.coderoller.springlayout.SpringLayout{a67ae738 V.E..... ......ID 0,0-0,0}
```

```
<org.coderoller.springlayout.SpringLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="60dp"
    android:background="@android:color/white">

    <View
        android:id="@+id/spring_A"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        app:layout_widthWeight="1"/>

    <TextView
        android:id="@+id/A"
        android:layout_width="wrap_content"
        android:layout_height="match_parent"
        android:background="#ffff0000"
        android:text="@string/sample_text"
        android:gravity="center"
        app:layout_toRightOf="@id/spring_A"/>

    <View
        android:id="@+id/B"
        android:layout_width="20dp"
        android:layout_height="match_parent"
        android:background="#ff00ff00"
        app:layout_toRightOf="@id/A"/>

    <View
        android:id="@+id/spring_B"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        app:layout_toRightOf="@id/B"
        app:layout_alignParentRight="true"
        app:layout_widthWeight="1" />
    
    <View
        android:id="@+id/C"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        app:layout_toRightOf="@id/B"
        app:layout_alignParentRight="true"
        app:layout_widthWeight="1" />

</org.coderoller.springlayout.SpringLayout>

```

#### Attributes reference

##### Spring Layout

**minHeight**

Defines the minimum height of the layout. 

**minWidth**

Defines the minimum width of the layout. 

##### Spring Layout Children

**layout_toLeftOf**

Positions the right edge of this view to the left of the given anchor view ID.

**layout_toRightOf**

Positions the left edge of this view to the right of the given anchor view ID.

**layout_above**

Positions the bottom edge of this view above the given anchor view ID.

**layout_below**

Positions the top edge of this view below the given anchor view ID.

**layout_alignLeft**

Makes the left edge of this view match the left edge of the given anchor view ID.

**layout_alignTop**

Makes the top edge of this view match the top edge of the given anchor view ID.

**layout_alignRight**

Makes the right edge of this view match the right edge of the given anchor view ID.

**layout_alignBottom**

Makes the bottom edge of this view match the bottom edge of the given anchor view ID.

**layout_alignCenter**

Center will be aligned both horizontally and vertically with the given anchor view ID.

**layout_alignCenterHorizontally**

Center will be aligned horizontally with the given anchor view ID.

**layout_alignCenterVertically**

Center will be aligned vertically with the given anchor view ID.

**layout_alignParentLeft**

If true, makes the left edge of this view match the left edge of the parent.

**layout_alignParentTop**

If true, makes the top edge of this view match the top edge of the parent.

**layout_alignParentRight**

If true, makes the right edge of this view match the right edge of the parent.

**layout_alignParentBottom**

If true, makes the bottom edge of this view match the bottom edge of the parent. 

**layout_centerInParent**

If true, centers this child horizontally and vertically within its parent.

**layout_centerHorizontal**

If true, centers this child horizontally within its parent.

**layout_centerVertical**

If true, centers this child vertically within its parent.

**layout_relativeWidth**

Width relative to parent (in percents).

**layout_relativeHeight**

Height relative to parent (in percents).

**layout_widthWeight**

Width weight of the spring. Used to calculate how much of the empty space in the chain the View should take. Works only when layout_width is WRAP_CONTENT.

**layout_heightWeight**

Height weight of the spring. Used to calculate how much of the empty space in the chain the View should take. Works only when layout_height is WRAP_CONTENT.

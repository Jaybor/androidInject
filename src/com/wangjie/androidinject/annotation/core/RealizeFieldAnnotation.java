package com.wangjie.androidinject.annotation.core;

import android.view.View;
import android.widget.AdapterView;
import com.wangjie.androidinject.annotation.annotations.*;
import com.wangjie.androidinject.annotation.listener.OnClickViewListener;
import com.wangjie.androidinject.annotation.listener.OnItemClickViewListener;
import com.wangjie.androidinject.annotation.listener.OnItemLongClickViewListener;
import com.wangjie.androidinject.annotation.listener.OnLongClickViewListener;
import com.wangjie.androidinject.annotation.present.AIPresent;
import com.wangjie.androidinject.annotation.util.SystemServiceUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: wangjie
 * Date: 13-11-30
 * Time: 下午7:23
 * To change this template use File | Settings | File Templates.
 */
public class RealizeFieldAnnotation implements RealizeAnnotation{
    private static final String TAG = RealizeFieldAnnotation.class.getSimpleName();
    private static Map<Class<?>, RealizeFieldAnnotation> map = new HashMap<Class<?>, RealizeFieldAnnotation>();

    public synchronized static RealizeFieldAnnotation getInstance(AIPresent present){
        Class clazz = present.getClazz();
        RealizeFieldAnnotation realize = map.get(clazz);
        if(null == realize){
            realize = new RealizeFieldAnnotation();
            map.put(clazz, realize);
        }
        realize.setPresent(present);
        realize.setClazz(clazz);
        return realize;
    }


    private AIPresent present;
    private Class<?> clazz;

    /**
     * 实现present控件注解功能
     * @throws Exception
     */
    @Override
    public void processAnnotation() throws Exception {
        Field[] fields = clazz.getDeclaredFields();
        for(Field field : fields){
            if(field.isAnnotationPresent(AIView.class)){ // 如果设置了控件注解
                AIView aiView = field.getAnnotation(AIView.class);

                viewFindAnnontation(aiView, field); // 绑定控件注解

                View view = (View)field.get(present);

                viewBindClick(aiView, view); // 绑定控件点击事件注解

                viewBindLongClick(aiView, view); // 绑定控件点击事件注解

                viewBindItemClick(aiView, view); // 绑定控件item点击事件注解

                viewBindItemLongClick(aiView, view);
            }

            if(field.isAnnotationPresent(AIBean.class)){ // 如果设置了bean注解
                beanNewInstance(field);
            }

            if(field.isAnnotationPresent(AISystemService.class)){ // 如果设置了SystemService注解
                systemServiceBind(field);
            }



        }
    }


    /**
     * 绑定控件注解
     * @param aiView
     * @param field
     * @throws Exception
     */
    private void viewFindAnnontation(AIView aiView, Field field) throws Exception{
        int viewId = aiView.id(); // 绑定控件注解
        field.setAccessible(true);
        Method method = clazz.getMethod(AnnotationManager.FIND_VIEW_METHOD_NAME, int.class);
        field.set(present, method.invoke(present, viewId));
    }

    /**
     * 绑定控件点击事件注解
     * @param aiView
     * @param view
     */
    private void viewBindClick(AIView aiView, View view){
        String clickMethodName = aiView.clickMethod();
        if(!"".equals(clickMethodName)){
            view.setOnClickListener(OnClickViewListener.obtainListener(present, clickMethodName));
        }
    }

    /**
     * 绑定控件点击事件注解
     * @param aiView
     * @param view
     */
    private void viewBindLongClick(AIView aiView, View view){
        String longClickMethodName = aiView.longClickMethod();
        if(!"".equals(longClickMethodName)){
            view.setOnLongClickListener(OnLongClickViewListener.obtainListener(present, longClickMethodName));
        }
    }

    /**
     * 绑定控件item点击事件注解
     * @param aiView
     * @param view
     */
    private void viewBindItemClick(AIView aiView, View view) throws Exception{
        // 如果view是AdapterView的子类(ListView, GridView, ExpandableListView...)
        String itemClickMethodName = aiView.itemClickMethod();
        if("".equals(itemClickMethodName)){
            return;
        }

        if(AdapterView.class.isAssignableFrom(view.getClass())){
            AdapterView adapterView = (AdapterView)view;
            adapterView.setOnItemClickListener(OnItemClickViewListener.obtainListener(present, itemClickMethodName));
        }else{
            throw new Exception("view[" + view + "] is not AdapterView's subclass");
        }

    }

    /**
     * 绑定控件item长按事件注解
     * @param aiView
     * @param view
     */
    private void viewBindItemLongClick(AIView aiView, View view) throws Exception{
        // 如果view是AdapterView的子类(ListView, GridView, ExpandableListView...)
        String itemClickMethodName = aiView.itemLongClickMethod();
        if("".equals(itemClickMethodName)){
            return;
        }

        if(AdapterView.class.isAssignableFrom(view.getClass())){
            AdapterView adapterView = (AdapterView)view;
            adapterView.setOnItemLongClickListener(OnItemLongClickViewListener.obtainListener(present, itemClickMethodName));

        }else{
            throw new Exception("view[" + view + "] is not AdapterView's subclass");
        }

    }

    /**
     * 生成一个bean对象
     * @param field
     * @throws Exception
     */
    private void beanNewInstance(Field field) throws Exception{
        try {
            field.getType().getConstructor();
        } catch (NoSuchMethodException e) {
            throw new Exception(field.getType() + " must has a default constructor (constructor with no paramater)! ");
        }
        field.setAccessible(true);
        field.set(present, field.getType().newInstance());

    }

    /**
     * 获得相应SystemService的对象，并初始化属性
     * @param field
     * @throws Exception
     */
    private void systemServiceBind(Field field) throws Exception{
        field.setAccessible(true);
        field.set(present, SystemServiceUtil.getSystemServiceByClazz(present.getContext(), field.getType()));
    }





    public void setClazz(Class<?> clazz) {
        this.clazz = clazz;
    }

    public void setPresent(AIPresent present) {
        this.present = present;
    }

    
}
package com.max.blepro.util;

import android.util.Log;


public class LogUtil {

	public static String TAG = "BlePro";
	
	public final static boolean DEBUG = true;
	
	public static void d(String comment)//输出DEBUG故障日志信息
	{
		if(DEBUG)
			Log.d(TAG, comment);
	}
	
	public static  void d(String tags,String comment)
	{
		if(DEBUG)
			Log.d(TAG,tags+"	"+ comment);
	}
	
	public static  void i(String tags,String comment)//输出INFO程序日志信息
	{
		if(DEBUG)
			Log.i(TAG,tags+"	"+ comment);
	}
	
	public static void e(String comment)//输出ERROR错误信息
	{
		if(DEBUG)
			Log.e(TAG, comment);
	}
	
	public static  void e(String tags,String comment)
	{
		Log.e(TAG,tags+"	"+ comment);
	}
	
	public static  void e(String tags,String comment,Exception e)
	{
		Log.e(TAG,tags+"	"+ comment+"--\r\n--Exception:"+e);
		Log.e(TAG,tags+"	"+comment,e);
	}
	
	public static  void v(String tags,String comment)//输出VERBOSE冗余日志信息
	{		
		Log.v(TAG,tags+"	"+comment);
	}
	public static  void w(String tags,String comment)//输出WARN警告日志信息
	{		
		Log.w(TAG,tags+"	"+comment);
	}
}

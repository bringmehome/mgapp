package cn.cmvideo.helper;

import cn.cmvideo.sdk.common.CmVideoApplication;

public class DemoApplication extends CmVideoApplication
{

	  public void onCreate () 
	  {
		  super.onCreate ();
		  
		  System.loadLibrary ("mg20pbase");
	  }
}

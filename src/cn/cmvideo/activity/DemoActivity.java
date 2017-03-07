package cn.cmvideo.activity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import cn.cmvideo.helper.RSA;
import cn.cmvideo.mgapp.R;
import cn.cmvideo.sdk.common.constants.ResourceType;
import cn.cmvideo.sdk.common.constants.ResultCode;
import cn.cmvideo.sdk.core.CmVideoCoreSdk;
import cn.cmvideo.sdk.core.handler.CmVideoCoreHandler;
import cn.cmvideo.sdk.core.service.CmVideoCoreService;
import cn.cmvideo.sdk.pay.bean.Goods;
import cn.cmvideo.sdk.pay.bean.Order;
import cn.cmvideo.sdk.pay.bean.PayInfo;
import cn.cmvideo.sdk.pay.bean.SubscribeInfo;
import cn.cmvideo.sdk.pay.bean.sales.GoodsService;
import cn.cmvideo.sdk.pay.bean.sales.Payment;
import cn.cmvideo.sdk.pay.bean.sales.QueryPriceInfo;
import cn.cmvideo.sdk.pay.handler.QueryPriceHandler;
import cn.cmvideo.sdk.pay.handler.SubscribeHandler;
import cn.cmvideo.sdk.service.auth.bean.AuthInfo;
import cn.cmvideo.sdk.service.auth.bean.Codec;
import cn.cmvideo.sdk.service.auth.bean.PlayExtResponse;
import cn.cmvideo.sdk.service.auth.handler.ServiceAuthHandler;
import cn.cmvideo.sdk.user.auth.AuthHandler;
import cn.cmvideo.sdk.user.auth.Oauth2AccessToken;

public class DemoActivity extends Activity 
{

	private static final String PREFERENCES_DEMO 	= "demo";

	private static final String TAG 			= "DemoActivity";
	
	private static final String APP_KEY        	= "147935303039910";//需要替换成自己的参数
	private static final String APP_SERRET 		= "fb1dc41c9d544d16a67b6c7e883b7378";//需要替换成自己的参数
	private static final String GOODS_ID        = "1001453";// 产品包id，需要替换成自己的参数
	private static final String GOODS_TYPE      = "MIGU_PACKAGE_PROGRAM";// 产品包枚举，需要替换成自己的参数
	private static final String RESOURCE_ID    	= "623000208";// 节目id，需要替换成自己的参数

	private RadioGroup payTypeGroup;
	private TextView name;
	private TextView price;
	private VideoView videoView;
	private Button subscribeBtn;

	private CmVideoCoreSdk sdk 			= null;
	private SubscribeInfo subscribeInfo;
	private Oauth2AccessToken mAccessToken;
	private List<GoodsService> services;
	private GoodsService currentService;
	private Payment currentPayment;
	private int index 					= 0;

	@Override
	protected void onCreate (Bundle savedInstanceState) 
	{
		super.onCreate (savedInstanceState);
		setContentView (R.layout.activity_demo_new);

		Log.i (TAG, "onCreate");

		videoView 		= (VideoView) findViewById (R.id.videoView);
		payTypeGroup 	= (RadioGroup) findViewById (R.id.payTypeGroup);

		name 			= (TextView) findViewById (R.id.nameValTxt);
		price 			= (TextView) findViewById (R.id.priceValTxt);


		subscribeBtn 	= (Button) findViewById (R.id.subscribeBtn);
		subscribeBtn.setOnClickListener (programOnClick);

		if (sdk == null) 
		{
			CmVideoCoreService.init (this, coreHandler);
		}
		
	}
	
	

	@Override
	protected void onActivityResult (int requestCode, int resultCode, Intent data) 
	{
		super.onActivityResult (requestCode, resultCode, data);

		if (sdk != null) 
		{
			sdk.onActivityCallBack (requestCode, resultCode, data);
		}
	}
	@Override
	protected void onStop () 
	{
		Log.i(TAG, "onStop");
		super.onStop();
	}

	@Override
	protected void onDestroy () 
	{
		Log.i (TAG, "onDestroy");
		sdk.release(this);
		super.onDestroy();
	}
	
	protected void onStart () 
	{
		Log.i (TAG, "onStart");
		super.onStart();
	};

	protected void onRestart () 
	{
		Log.i (TAG, "onRestart");
		super.onRestart ();
	};

	
	
	OnClickListener programOnClick 	= new OnClickListener ()
	{
		public void onClick (View v) 
		{
			switch (v.getId ())
			{
				case R.id.subscribeBtn:
				{
					subscribe ();
				}break;
			
			}
		}
	};


	

	/**
	 * 批价
	 * @param info
	 */
	private void queryPrice (QueryPriceInfo info) 
	{
		sdk.querySalesPrice (DemoActivity.this, info, new QueryPriceHandler () 
		{

			@Override
			public void onResult (String resultCode, String resultDesc, List<GoodsService> services) 
			{
				String str 	= String.format ("resultCode=%s, resultDesc=%s", resultCode, resultDesc);
				Log.d (TAG, str);

				if (ResultCode.ACCEPTED.name ().equals (resultCode)) 
				{
					DemoActivity.this.services 	= services;
					if (services != null && services.size () > 0) 
					{
						int idx 	= 0;
						for (GoodsService service : services) 
						{
							for (Payment payment : service.getPayments ()) 
							{
								RadioButton radio 	= new RadioButton (DemoActivity.this);
								if (idx == 0) 
								{
									radio.setChecked (true);
									currentService 	= service;
									currentPayment 	= payment;
								}
								radio.setId (idx);
								radio.setText (payment.getName ());
								radio.setOnClickListener (radioBtnClick);
								payTypeGroup.addView (radio);
								idx ++;
							}
						}

						name.setText (services.get (0).getServiceInfo ().getName ());
						price.setText(String.format ( "%.2f", Double.parseDouble (services.get (0).getPayments ().get (0).getAmount ()+ "") / 100));
					}
				} 
				else 
				{
					Toast toast 	= Toast.makeText (DemoActivity.this, str, Toast.LENGTH_LONG);
					toast.show ();
				}

			}
		});
	}

	/**
	 * 订购
	 */
	private void subscribe () 
	{
		//现在订购之前是不需要添加登录操作的！在订购返回里面会有登录提示的！如果错误返回ResultCode.DEV_IDENT_ERR的话，那么可以尝试登录操作！
//		if (!isLogin ())
//		{
//			return;
//		}
			

		if (services == null || services.size () < 1) 
		{
			Toast toast 	= Toast.makeText (DemoActivity.this, "批价失败，无法订购！", Toast.LENGTH_LONG);
			toast.show ();
			return;
		}

		if (currentService == null || currentPayment == null) 
		{
			Toast toast 	= Toast.makeText (DemoActivity.this, "currentService或者currentPayment不能为空", Toast.LENGTH_LONG);
			toast.show ();
			return;
		}
		
		Goods goods = new Goods();
		goods.setId(GOODS_ID);
		goods.setType(GOODS_TYPE);
		goods.setResourceId(RESOURCE_ID);
		sdk.subscribe(DemoActivity.this, currentService, currentPayment, mAccessToken, goods, new SubscribeHandler() {

			@Override
			public void onResult(String resultCode, String resultDesc,
					String externalOrderId, Order order, String validId,
					String imageHexStream) {
				String str 	= String.format( "resultCode=%s, resultDesc=%s, validId=%s", resultCode, resultDesc, validId);
				Log.d (TAG, str);
				if (ResultCode.ACCEPTED.name ().equals (resultCode) && order != null) 
				{
					Toast toast	 = Toast.makeText (DemoActivity.this, "订购状态=" + order.getStatus (), Toast.LENGTH_LONG);
					toast.show ();
					
					// 鉴权
					serviceAuth ();
				} 
				else if (ResultCode.DEV_IDENT_ERR.name().equals(resultCode))//自动登录失败，需要调用授权页面登录方式
				{
					sdk.authorize (DemoActivity.this, APP_KEY, APP_SERRET, new DemoAuthHandler ());
				}
				else 
				{
					Toast toast 	= Toast.makeText (DemoActivity.this, str, Toast.LENGTH_LONG);
					toast.show ();
				}
			}
		});
	}
	
	
	boolean isSuccess 	= false;
	/**
	 * 鉴权
	 * @return
	 */
	private boolean serviceAuth () 
	{
		sdk.serviceAuth(DemoActivity.this, mAccessToken.getUserId (), mAccessToken.getToken (), RESOURCE_ID, ResourceType.POMS_PROGRAM_ID, Codec.normal,
				new ServiceAuthHandler () 
		{
			@Override
			public void onResult (String resultCode, String resultDesc, AuthInfo authInfo, PlayExtResponse playExtResponse) 
			{

				isSuccess 		= ResultCode.SUCCESS.name ().equals(resultCode);
				
				String info 	= "";
				String url 		= "";
				if (isSuccess) 
				{
					url 	= playExtResponse.getPlayUrl ();
					info 	= String.format ("鉴权成功！播放地址为: %s", url);
				} 
				else 
				{
					if (playExtResponse != null) 
					{
						url 	= playExtResponse.getPlayUrl4Trial ();
					}
					info 	= String.format ("鉴权失败！试播地址为: %s", url);
				}

				Toast toast  	= Toast.makeText(DemoActivity.this, info, Toast.LENGTH_LONG);
				toast.show ();

				if (!TextUtils.isEmpty (url)) 
				{
					Uri uri 	= Uri.parse(url);
					videoView.setVideoURI (uri);
					videoView.start ();
				}
			}
		});
		return isSuccess;
	}
	
	
	
	
	
	CmVideoCoreHandler coreHandler 	= new CmVideoCoreHandler () 
	{

		public String encryptByRSA	(String source) 
		{
			String result 	= "";
			try 
			{
				result 	= RSA.encrypt (source);
			} 
			catch (Exception e) 
			{
				e.printStackTrace ();
			}
			return result;
		}

		public void initCallback (String resultCode, String resultDesc, CmVideoCoreSdk sdk) 
		{
			DemoActivity.this.sdk 	= sdk;
			if (ResultCode.SUCCESS.name ().equals (resultCode)) 
			{

				// Demo示例代码，开发者可不存用户登录信息
				SharedPreferences pref 	= DemoActivity.this.getSharedPreferences (PREFERENCES_DEMO, Context.MODE_PRIVATE);
				String userid 			= pref.getString ("userid", "");
				String usernum 			= pref.getString ("usernum", "");
				String accesstoken 		= pref.getString ("accesstoken", "");
				
				mAccessToken 			= new Oauth2AccessToken ();
				mAccessToken.setUserId (userid);
				mAccessToken.setToken (accesstoken);
				mAccessToken.setUsernum (usernum);

				QueryPriceInfo info 	= new QueryPriceInfo ();
				info.setGoodsId (GOODS_ID);
				info.setGoodsType (GOODS_TYPE);

				Map<String, String> goodsProperties 	= new HashMap<String, String> ();
				goodsProperties.put ("resourceId", RESOURCE_ID);
				// 按次订购goodsProperties必须传入resourceId
				info.setGoodsProperties (goodsProperties);

				// 批价
				queryPrice (info);

				// 鉴权
				serviceAuth ();
			} 
			else 
			{
				Toast toast = Toast.makeText (DemoActivity.this, resultDesc, Toast.LENGTH_LONG);
				toast.show ();
			}
		}
	};
	
	class DemoAuthHandler implements AuthHandler 
	{
		@Override
		public void onComplete (Bundle values) 
		{
			// 从 Bundle 中解析 Token
			mAccessToken 	= Oauth2AccessToken.parseAccessToken (values);
			if (mAccessToken.isSessionValid ()) 
			{
				Log.i (TAG, "accessToken=" + mAccessToken.getToken ());

				
				// 建议开发者保存用户登录信息，在token失效以前可无需登录！失效之后需要重新调用登录，mAccessToken.getExpirein()来获取过期时间点！
				SharedPreferences pref 	= DemoActivity.this.getSharedPreferences (PREFERENCES_DEMO, Context.MODE_PRIVATE);
				Editor editor 			= pref.edit ();
				editor.putString ("userid", mAccessToken.getUserId ());
				editor.putString ("usernum", mAccessToken.getUsernum ());
				editor.putString ("accesstoken", mAccessToken.getToken ());

				editor.commit ();

				boolean isSuccess 	= serviceAuth ();//这里鉴权结果是直接返回成功的，只在DEMO里面做参考用！
				if (!isSuccess)
				{
					subscribe ();
				}
			} 
			else 
			{
				// 当您注册的应用程序签名不正确时，就会收到 Code，请确保签名正确
				String code 	= values.getString ("code");
				Log.i (TAG, "code=" + code);
			}
		}

	}
	
	

	
//	private boolean isLogin () 
//	{
//		if (mAccessToken == null || !mAccessToken.isSessionValid ()) 
//		{
//			sdk.authorize (DemoActivity.this, APP_KEY, APP_SERRET, new DemoAuthHandler ());
//			return false;
//		}
//		return true;
//	}
	
	public Bitmap bytes2Bimap (byte[] b) 
	{
		if (b.length != 0) 
		{
			return BitmapFactory.decodeByteArray (b, 0, b.length);
		}
		else 
		{
			return null;
		}
	}

	
	OnClickListener radioBtnClick 	= new OnClickListener () 
	{
		public void onClick (View v) 
		{
			onRadioButtonClicked (v);
		}
	};
	
	public void onRadioButtonClicked (View view) 
	{
		boolean isSet 		= false;
		boolean checked 	= ((RadioButton) view).isChecked ();
		if (checked) 
		{
			index 	= view.getId ();
			if (services != null && services.size () > 0) 
			{
				int idx 	= 0;
				for (GoodsService service : services) 
				{
					for (Payment payment : service.getPayments ()) 
					{
						if (idx == index) 
						{
							currentService 	= service;
							currentPayment 	= payment;

							name.setText (service.getServiceInfo ().getName ());
							price.setText (String.format ( "%.2f", Double.parseDouble (payment.getAmount () + "") / 100));
							isSet 	= true;
							break;
						} 
						else 
						{
							idx++;
						}
					}

					if (isSet) 
					{
						break;
					}
				}
			}
		}
	}


}

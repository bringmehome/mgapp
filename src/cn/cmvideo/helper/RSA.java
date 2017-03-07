package cn.cmvideo.helper;

import java.security.Key;
import java.security.KeyFactory;
import java.security.spec.PKCS8EncodedKeySpec;

import javax.crypto.Cipher;

import cn.cmvideo.sdk.common.util.Base64;

public class RSA 
{

	private static final String ALGORITHM 	= "RSA";

	//这里需要替换成自己的私钥，并且进行pkcs8格式转换,参考链接https://help.alipay.com/support/help_detail.htm?help_id=397433
	private static String RSA_PRIVATE_KEY_PKCS8 = "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBALMYPHhS6lABbROY"
		      + "\r" +"VcobIh/T1qErfyH6IIk7wDvCc/b6X1J/dKp3cSD6XjLQmeFVw/faleYbuj/N69sQ" + "\r"
		      + "4PVLWKO0SyA0U0shzSsz8dQRfXhtgXGwk34qYRZN0URZqer5fkN5U+g88/2GEt9s" + "\r"
		      + "N9OgPrkqSrH30d08ZjMyNnYGQwWhAgMBAAECgYBq2jHx5B5yX3dxsni03Z1tedlb" + "\r"
		      + "TXVLk01oM2MNBIZlu5m1wd+ceSjg8R/Ul4rH24DRVvQKV063FKf8isR89VhLTAPS" + "\r"
		      + "3DiH2Izk/qtagNUZaDivdNThnG/757XTf4hlCoeXHpnbDoWoFe8O2cOKL8AqAQgC" + "\r"
		      + "SOJI8mx0x86ibLbxNQJBANwLDczeX4Y1q4LTBf5yxAk7Q6RHzs7ZugrfXKZurBKE" + "\r"
		      + "l3C5MGm5wIskY6M/cItxZt8+DB+d79cXNjuguNIb2f8CQQDQXDSvSGztLx0zDy0B" + "\r"
		      + "vYTOD/VhC9dCo59HtNutXaq28Pdy7iGGvTJiD7Er8OC2aoN2AVG24yFNrWCEdXkk" + "\r"
		      + "0uBfAkAnGTu5hGXa1hyEoXR2MvRMY6BwR2Yi8SMSnX+7/vxKSg8Ss4U7tArXbn7Z" + "\r"
		      + "2gLodB0AW+kRkSG1yWUUkUll5BcHAkBP2CWwTdbABV/xIw2iLxfnRyJG8ByrQrxU" + "\r"
		      + "5C+SfeRfenO4rRxX38Sg41aHeiUCgkqiO9sudFdcxuXHnKSxwcWNAkAZN0GqJseU" + "\r"
		      + "VsEQD0ma3+QW8rHd6mQRifHXjNhiS28rZb3ZepmcFD+m6fzF0iVoMoClWJxaHums" + "\r"
		      + "yYSL11ElosmZ";

	public static String encrypt (String source) throws Exception 
	{
		byte[] data 		= source.getBytes ();
		byte[] keyBytes 	= Base64.decode (RSA_PRIVATE_KEY_PKCS8);

		// 取得私钥
		PKCS8EncodedKeySpec pkcs8KeySpec 	= new PKCS8EncodedKeySpec (keyBytes);
		KeyFactory keyFactory 				= KeyFactory.getInstance (ALGORITHM);
		Key privateKey 						= keyFactory.generatePrivate (pkcs8KeySpec);

		/*
		 * 用私钥进行签名 RSA Cipher负责完成加密或解密工作，基于RSA
		 */
		Cipher cipher 	= Cipher.getInstance ("RSA/ECB/PKCS1Padding");
		// ENCRYPT_MODE表示为加密模式
		cipher.init (Cipher.ENCRYPT_MODE, privateKey);
		// 加密
		byte[] rsaBytes 	= cipher.doFinal (data);
		// Base64编码
		return Base64.encode (rsaBytes);
	}
}

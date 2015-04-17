package com.bd.secure;

import org.apache.commons.codec.digest.DigestUtils;

public class Sha256Generator {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Testing();
		
	}
	
	static void Testing()
	{
		UserCreds usrobj=new UserCreds();
		usrobj.setUserName("xyz");
		usrobj.setUserPassword("myPass$%^");
		usrobj.setUserMail("myname@somemail.com");
		usrobj.generateCrypt("myPass$%^");
		usrobj.generateCrypt("myPass$%");
		System.out.print(usrobj.validateCreds());
	}
}

class UserCreds
{
	private String usrName="";
	private String usrPassword="";
	private String usrMail="";
	private String cryptPassword="";
	
	public void setUserName(String loginName)
	{
		usrName=loginName;//get from login
	}
	
	public void setUserPassword(String loginPass)
	{
		usrPassword=loginPass; //get this from login
	}
	
	public void setUserMail(String signUpMail)
	{
		usrMail=signUpMail;
	}
	
	public void retrieveDBPassword()
	{
			//TODO get sha password stored in DB and set the value to 'cryptPassword' member	
	}
	
	public void setDBPassword(String cPass)
	{
			//TODO add password to db password=cryptPassword
			//password=generateCrypt(cPass);
	}
	
	public String getUsrMail(String key)
	{
		//TODO get mail from db using key as usrid
		return usrMail;
	}
	
	private void setSalt(String usrname)
	{
		usrMail=getUsrMail(usrName);//get mail from db
	}
	
	public String generateCrypt(String myUsrPassword)
	{
		setSalt(this.usrName);
		cryptPassword= DigestUtils.sha256Hex(myUsrPassword);
		cryptPassword= DigestUtils.sha256Hex(cryptPassword+usrMail);
		System.out.println(cryptPassword);
		return cryptPassword;
	}
	
	private String generateCrypt()
	{
		setSalt(this.usrName);
		cryptPassword= DigestUtils.sha256Hex(usrPassword);
		cryptPassword= DigestUtils.sha256Hex(cryptPassword+usrMail);
		System.out.println(cryptPassword);
		return cryptPassword;
	}
	
		
	public Boolean validateCreds()
	{
		return cryptPassword.equals(generateCrypt());
	}
}

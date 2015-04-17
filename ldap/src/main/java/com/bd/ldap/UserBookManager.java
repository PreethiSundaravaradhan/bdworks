package com.bd.ldap;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.net.SocketFactory;

import com.unboundid.ldap.sdk.BindResult;
import com.unboundid.ldap.sdk.DeleteRequest;
import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldap.sdk.Filter;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.Modification;
import com.unboundid.ldap.sdk.ModificationType;
import com.unboundid.ldap.sdk.ModifyRequest;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.ldap.sdk.SearchRequest;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchScope;
import com.unboundid.ldap.sdk.SimpleBindRequest;

/**
 * Created by Work on 2/22/2015.
 */
public class UserBookManager{

	private String rootDn;
	private String password;
	private int ldapPort;
	private String ldapDomain;
	private String adminDn;

	public void loadProperties() throws Exception {
        try{

            Properties prop = new Properties();
            // Load the properties file
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            //InputStream inStream = classLoader.getResourceAsStream("./config.properties");
            FileInputStream inStream=new FileInputStream("S://bd/ldap-java/ldap/target/config.properties");
            prop.load(inStream);
            inStream.close();

            password = prop.getProperty("ldap.password");

            ldapPort = Integer.parseInt(prop.getProperty("ldap.port"));
            ldapDomain = prop.getProperty("ldap.domain");

            rootDn = prop.getProperty("ldap.rootdn");
            adminDn = prop.getProperty("ldap.admindn");
        }
        catch(IOException e1){
             e1.printStackTrace();
             throw new Exception("LDAP CREDENTIALS:Couldn't read ");
        }

	}

	private SocketFactory sfact;



    public String loginUser(String username, String password) throws Exception{
        try {
            LDAPConnection connection = getUserConnection(username, password);
            List<Filter> data = new ArrayList<>();

            data.add(Filter.createEqualityFilter("userid", username));
           // data.add(Filter.createEqualityFilter("uniqueIdentifier", password));

            SearchResult res = connection.search(new SearchRequest(
                    rootDn, SearchScope.SUB, Filter.createANDFilter(data)
            ));

            String userId = null;
            System.out.println("Entries : " + res.getEntryCount());
            for(SearchResultEntry ent : res.getSearchEntries()) {
                System.out.println(ent.toString());
                userId = ent.getAttributeValue("uniqueIdentifier");
            }

            connection.close();

            return userId;
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new Exception("FAILED:BAD PASSWORD");
        }
        finally {
        }
    }

    public void addUserAccount(AuthDTO userAuthData) throws Exception {
    	LDAPConnection connection = null;
    	try {

    		// Add user to ldap directory first
    		connection = getUserConnection("billdesk","billdesk");
    		final String userDN = String.format("userid=%s,"+rootDn, userAuthData.username);
    		Entry ent = new Entry(userDN);

    		ent.addAttribute("objectClass", "extensibleObject");
    		ent.addAttribute("objectClass", "inetOrgPerson");
    		ent.addAttribute("objectClass", "organizationalPerson");
    		ent.addAttribute("objectClass", "person");
    		ent.addAttribute("objectClass", "top");

    		ent.addAttribute("cn", "user");
    		ent.addAttribute("sn", "user");

    		ent.addAttribute("pwdLockout", "TRUE");
    		ent.addAttribute("pwdLockoutDuration", "3600");
    		ent.addAttribute("pwdMaxAge", "86400");
    		ent.addAttribute("pwdMaxFailure", "4");
    		ent.addAttribute("pwdMinLength", "5");
    		ent.addAttribute("pwdInHistory","4");
    		

    		ent.addAttribute("userid", userAuthData.username);
    		ent.addAttribute("userPassword", userAuthData.password);
    		ent.addAttribute("uniqueIdentifier", userAuthData.userId);
    		connection.add(ent);
    		connection.close();
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		connection.close();
    		throw new Exception("FAILED:COULD NOT CREATE USER");
    	}
    }

    public void changePassword(String userId, String newPass) {
        LDAPConnection connection = null;
        try {
            connection = getAdminConnection();
            connection.modify(new ModifyRequest(String.format("userid=%s,"+rootDn, userId), new Modification(ModificationType.REPLACE, "userPassword", newPass)));
            connection.close();
        }
        catch(Exception e) {
            e.printStackTrace();
            connection.close();
        }
    }

    public void removeUser(String userId) throws Exception {
    	LDAPConnection connection = null;
    	try {
    		connection = getAdminConnection();
    		String username = connection.search(new SearchRequest(rootDn, SearchScope.SUB, Filter.createEqualityFilter("uniqueIdentifier", userId))).getSearchEntries().get(0).getAttribute("userid").getValue();
    		final String userDN = String.format("userid=%s,"+rootDn, username);
    		connection.delete(new DeleteRequest(userDN));
    		connection.close();
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		connection.close();
    		throw new Exception("FAILED:COULD NOT DELETE USER, LDAP AND DB ARE OUT OF SYNC");
    	}
    }

    /**
     * @return LDAPConnection: returns a usable connection object
     * */
    private LDAPConnection getUserConnection(String username, String password)  throws Exception {
        if(sfact == null) {
            sfact = SocketFactory.getDefault();
        }

        LDAPConnection connection = new LDAPConnection(sfact, ldapDomain, ldapPort);
        BindResult result = connection.bind(new SimpleBindRequest(String.format("userid=%s,"+rootDn, username), password));
        System.out.println(result.getResultCode());

        if(result.getResultCode() == ResultCode.INVALID_CREDENTIALS)
            throw new Exception("Invalid Credentials");

        else
            return connection;
    }

    private LDAPConnection getAdminConnection() throws Exception {
    	if(sfact == null)
    		sfact = SocketFactory.getDefault();

        LDAPConnection connection = new LDAPConnection(sfact, ldapDomain, ldapPort);
        BindResult result = connection.bind(new SimpleBindRequest(adminDn, password));
        System.out.println(result.getResultCode());

        if(result.getResultCode() == ResultCode.INVALID_CREDENTIALS)
            throw new Exception("Invalid Credentials");

        else
            return connection;
    }
}


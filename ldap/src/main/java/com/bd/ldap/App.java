package com.bd.ldap;

import java.io.PrintStream;

/**
 * Hello world!
 *
 */
public class App
{

    public UserBookManager manager = new UserBookManager();
    private static PrintStream out = System.out;

    public void createUser(String username, String password) {
        AuthDTO auth = new AuthDTO();
        auth.username = username;
        auth.userId = username;
        auth.password = password;
        try {
            manager.addUserAccount(auth);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void initialize() throws Exception {
        manager.loadProperties();
    }
    public static void main( String[] args )
    {
        try {
            String username = "billdesk";
            String password = "billdesk";
            App app = new App();
            app.initialize();
            //app.createUser(username, password);
            String userid = app.manager.loginUser(username, password);
            out.println("Succesfully logged in user: " + userid);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}

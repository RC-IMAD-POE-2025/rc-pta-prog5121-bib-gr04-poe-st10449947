/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package register4;

import java.util.HashMap;

/**
 *
 * @author RC_Student_lab
 */
public class userdatabase {
    
/**
 *
 * @author RC_Student_lab
 */
public class userdatastore {
    public static HashMap<String,String>userCredentials=new HashMap<>();
    public static HashMap<String,String>userphoneno=new HashMap<>();
    public static HashMap<String,String>userFirstname=new HashMap<>();
    public static HashMap<String,String>userLastname=new HashMap<>();
    
    public static String currentLoggedInuser=null;
    static{
        userCredentials.put("john.doe", "Password123");
        userFirstname.put("john.doe", "John");
        userLastname.put("john.doe", "Doe");
        userphoneno.put("john.doe", "+27609876543");
    }
    public static boolean registerUser(String username, String password, String firstName, String lastName, String phoneNo) {
        if (userCredentials.containsKey(username)) {
            return false; // Username already exists
        }
        userCredentials.put(username, password);
        userFirstname.put(username, firstName);
        userLastname.put(username, lastName);
        userphoneno.put(username, phoneNo);
        return true;
    }
        public static boolean doesUserExist(String username) {
        return userCredentials.containsKey(username);
    }


}

}

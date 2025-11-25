/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandUtil {       
    public static String[] tokenizerArgs(String args) {           
        List<String> tokens = new ArrayList<String>();           
        char[] charArray = args.toCharArray();           
        String contact = "";           
        boolean inText = false;           
        
        for (char c : charArray) {               
            if (c == ' ' && !inText) {                   
                if (contact.length() != 0) {   
                    tokens.add(contact);                       
                    contact = "";   
                }               
            } else if (c == '"') {
                if (inText) {   tokens.add(contact);                       
                contact = "";                       
                inText = false;                   
                } else {                       
                    inText = true;   
                }               
            } else {   
                contact += c;   
            }   
        }           
        if (contact.trim().length() != 0) {   
            tokens.add(contact.trim());   
        }           
         
        String[] argsArray = new String[tokens.size()];   
        argsArray = tokens.toArray(argsArray);           
        return argsArray;   
    }
    
    public static void main(String[] args) {           
        String commanda = "file -an c:/dummy/dummy.txt \"Hola mundo tres veces\"";   
        String[] tokens = CommandUtil.tokenizerArgs(commanda);   
        System.out.println(Arrays.toString(tokens));   
    }   
}


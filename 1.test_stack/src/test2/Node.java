/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test2;

/**
 *
 * @author Xu
 */
public class Node {
    //Object info;
    
    Student info;
    Node next;
    
    Node() {}
    Node(Student info, Node next) 
    {this.info=info; 
     this.next=next;}
    
    Node(Student info) 
    {this(info, null);}
}

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
public class MyStack {
    Node head;
    public MyStack() {head = null;}
    public void clear() {head = null;}
    public boolean isEmpty() {return head == null;}
    
    public void push(Student info) {
        Node p = new Node(info);
        if(isEmpty()) {
            head = p;
        }
        else {
            p.next = head; head = p;}
    }
    
    public Student pop() {
        if(isEmpty()){
            return null;
        }
        Student info = head.info;
        head = head.next;
        return info;
    }
    
    public Student top() {
        if(isEmpty()){
            return null;
        }
        Student info = head.info;
        return info;
    }  
    
    public void display() {
        Node current = head;
        System.out.print("Stack: \n");
        while (current != null) {
            System.out.print(current.info + "\n");
            current = current.next;
        }
        System.out.println();
    }
}


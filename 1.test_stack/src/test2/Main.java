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
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        MyStack a = new MyStack();

        // a.push(10);
        // a.push(200);
        a.push(new Student("Nguyen A", 30));
        a.push(new Student("Nguyen B", 200));
        a.push(new Student("Nguyen C", 500));
        a.push(new Student("Nguyen D", 600));
        a.display();
        
        System.out.println("Top: " + a.top());
        System.out.println("Pop: " + a.pop());
        a.display();
        
        
    }

}


/*
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
    
    public Object pop() {
        if(isEmpty()){
            return null;
        }
        Object info = head.info;
        head = head.next;
        return info;
    }
    
    public Object top() {
        if(isEmpty()){
            return null;
        }
        Object info = head.info;
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
public class Student {
    String name;
    int age;

    public Student(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
    
    @Override
    public String toString(){
        return "Name: "+name+", age: "+age;
    }
}*/

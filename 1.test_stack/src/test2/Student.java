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
public class Student {
    String name;
    int age;

    public Student(String name, int age) {
        this.name = name;
        this.age = age;
    }


    @Override
    public String toString(){
        return "Name: "+name+", age: "+age;
    }
}

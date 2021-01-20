package com;

import com.annotations.BuilderPProperty;
import com.annotations.SetterNotNull.SetterNotNull;

@SetterNotNull
public class Person {

    private int age;

    private String name;

    public int getAge() {
        return age;
    }

    @BuilderProperty
    public void setAge(int age) {
        this.age = age;
    }

    public String getName() {
        return name;
    }

    @BuilderProperty
    public void setName(String name) {
        this.name = name;
    }

    public void sudoAptUpdate(String a) {};

    @BuilderPProperty
    public void setSacota(String name) {
        this.name = name;
    }


}

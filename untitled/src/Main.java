public class Main {

    public static void main(String[] args) {

        A a = new A();
        B b = new B();

        B.setJ();

        System.out.print(A.j);
    }
}

class A {
     static int j = 0;
    A a = new A();


    static void setJ () {
        j = 5;
    }
}

class B extends A {
    int j = 1;
}
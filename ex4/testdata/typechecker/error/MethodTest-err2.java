class MethodTest {
	public static void main(String[] a){
            A x;
            x = new A();
            x = x.m(); // TE
	}
}

class A {

}

class B extends A {
    B m()
    {    
        return new B();
    }

}
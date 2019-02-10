class ParamTest {
	public static void main(String[] a){
            A x;
            B y;
            x = new A();
            y = new B();
            x = x.m(x); // TE 
	}
}

class A {
    A m(B x)
    {    
        return new A();
    }

}

class B extends A {}

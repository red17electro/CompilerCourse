class ThisTest {
	public static void main(String[] a){
            int x;
	}
}

class A {
    int m()
    {
        B y;
        y = this; // TE
        return 0;
    }

}

class B {}

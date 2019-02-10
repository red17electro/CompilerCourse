class PrecedenceTest{
    public static void main(String[] a){
	int x;
        x = new C().m().length;
	}
}

class C {
    int[] m() {
        return new int[3];
    }
}

class Main {
    public static void main(String[] a) {
    	int[] ar;
    	ar = new int[20];
    	ar[5] = 30;
    	ar[7] = 25;
    	System.out.println(new A().max(ar));
    }
}

class A {

    int max(int[] ar) {
    	int res;
    	int i;
		res = ar[0];
		i = 1;
		while (i<ar.length) {
			if (res < ar[i]) {
				res = ar[i];
			} else {
				
			}
			i = i + 1;
		}
		return res;
    }
}

class BadAssignmentError{
    public static void main(String[] a){
    	if (true) {
    		
    	} else {
    		while (false) {
    			1 = 5*3;
    		}
    	}
    }
}
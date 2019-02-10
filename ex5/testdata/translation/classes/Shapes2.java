class Shapes {
	public static void main(String[] args) {
		Shape shape;
		Rectangle rect;
		Circle circ;
		boolean b;
		b = false;
		if (b) {
			rect = new Rectangle();
			rect.height = 10;
			rect.width = 20;
			shape = rect;
		} else {
			circ = new Circle();
			circ.radius = 14;
			shape = circ;
		}
		shape.shapeNumber = 77;
		System.out.println(shape.getArea());
	}
}

class Shape {
	int shapeNumber;

	int getArea() {
		return 0;
	}

	int getScaledArea(int scale) {
		return scale * this.getArea();
	}
}


class Rectangle extends Shape {
	int width;
	int height;

	int getArea() {
		return width*height;
	}
}

class Circle extends Shape {
	int radius;

	int getArea() {
		return radius*radius*3;
	}
}

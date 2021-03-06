package io.cubyz.ui.components;

import java.util.ArrayList;

import io.cubyz.ui.Component;
import io.jungle.Window;

public class Container extends Component {

	protected ArrayList<Component> childrens;
	
	public Container() {
		childrens = new ArrayList<>();
	}
	
	public void add(Component comp) {
		if (comp == this) throw new IllegalArgumentException("comp == this");
		childrens.add(comp);
	}
	
	public void remove(Component comp) {
		childrens.remove(comp);
	}
	
	public void remove(int index) {
		childrens.remove(index);
	}

	@Override
	public void render(long nvg, Window src) {
		for (Component child : childrens) {
			child.render(nvg, src);
		}
	}
	
}

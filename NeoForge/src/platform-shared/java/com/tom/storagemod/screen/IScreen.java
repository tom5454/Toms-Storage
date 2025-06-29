package com.tom.storagemod.screen;

import java.util.function.Consumer;

public interface IScreen {
	void getExclusionAreas(Consumer<Box> consumer);

	record Box(int x, int y, int width, int height) {}
}

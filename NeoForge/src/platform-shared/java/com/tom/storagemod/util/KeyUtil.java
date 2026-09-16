package com.tom.storagemod.util;

import java.nio.ByteBuffer;

import org.lwjgl.sdl.SDLKeyboard;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;

public class KeyUtil {

	public static boolean hasControlDown() {
		if (RenderSystem.isOnRenderThread()) {
			ByteBuffer keyboardState = SDLKeyboard.SDL_GetKeyboardState();
			return keyboardState.get(InputConstants.KEY_LCONTROL) != 0;
		}
		return false;
	}

	public static boolean hasShiftDown() {
		if (RenderSystem.isOnRenderThread()) {
			ByteBuffer keyboardState = SDLKeyboard.SDL_GetKeyboardState();
			return keyboardState.get(InputConstants.KEY_LSHIFT) != 0;
		}
		return false;
	}

}

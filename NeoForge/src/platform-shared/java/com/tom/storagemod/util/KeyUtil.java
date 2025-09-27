package com.tom.storagemod.util;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.Minecraft;

import com.mojang.blaze3d.systems.RenderSystem;

public class KeyUtil {

	public static boolean hasControlDown() {
		if (RenderSystem.isOnRenderThread())
			return GLFW.glfwGetKey(Minecraft.getInstance().getWindow().handle(), GLFW.GLFW_KEY_LEFT_CONTROL) != GLFW.GLFW_RELEASE;
		return false;
	}

	public static boolean hasShiftDown() {
		if (RenderSystem.isOnRenderThread())
			return GLFW.glfwGetKey(Minecraft.getInstance().getWindow().handle(), GLFW.GLFW_KEY_LEFT_SHIFT) != GLFW.GLFW_RELEASE;
		return false;
	}

}

package com.tom.storagemod.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import net.minecraft.ChatFormatting;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

import com.tom.storagemod.inventory.StoredItemStack;
import com.tom.storagemod.network.NetworkHandler;

import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class TerminalSyncManager {
	private static final int MAX_PACKET_SIZE = 64000;
	private Object2IntMap<StoredItemStack> idMap = new Object2IntOpenHashMap<>();
	private Int2ObjectMap<StoredItemStack> idMap2 = new Int2ObjectArrayMap<>();
	private Map<StoredItemStack, StoredItemStack> items = new HashMap<>();
	private Map<StoredItemStack, StoredItemStack> itemList = new HashMap<>();
	private int lastId = 1, lastChangeID = -1;
	private RegistryFriendlyByteBuf workBuf;

	public TerminalSyncManager(RegistryAccess reg) {
		workBuf = new RegistryFriendlyByteBuf(Unpooled.buffer(MAX_PACKET_SIZE, MAX_PACKET_SIZE * 2), reg);
	}

	private void write(RegistryFriendlyByteBuf buf, StoredItemStack stack) {
		ItemStack st = stack.getStack();
		Item item = st.getItem();
		DataComponentPatch components = st.getComponentsPatch();
		byte flags = (byte) ((stack.getQuantity() == 0 ? 1 : 0) | (!components.isEmpty() ? 2 : 0));
		boolean wr = true;
		int id = idMap.getInt(stack);
		if(id != 0) {
			flags |= 4;
			wr = false;
		}
		buf.writeByte(flags);
		buf.writeVarInt(idMap.computeIfAbsent(stack, s -> {
			int i = lastId++;
			idMap2.put(i, (StoredItemStack) s);
			return i;
		}));
		if(wr)buf.writeVarInt(BuiltInRegistries.ITEM.getId(item));
		if(stack.getQuantity() != 0)buf.writeVarLong(stack.getQuantity());
		if(wr && !components.isEmpty())DataComponentPatch.STREAM_CODEC.encode(buf, components);
	}

	private void writeMiniStack(RegistryFriendlyByteBuf buf, StoredItemStack stack) {
		int id = idMap.getInt(stack);
		byte flags = (byte) ((stack.getQuantity() == 0 ? 1 : 0) | 2);
		buf.writeByte(flags);
		buf.writeVarInt(id);
		buf.writeVarInt(BuiltInRegistries.ITEM.getId(stack.getStack().getItem()));
		if(stack.getQuantity() != 0)buf.writeVarLong(stack.getQuantity());
		DataComponentPatch tag = DataComponentPatch.builder().set(DataComponents.LORE, new ItemLore(List.of(Component.translatable("tooltip.toms_storage.nbt_overflow").withStyle(ChatFormatting.RED)))).build();
		DataComponentPatch.STREAM_CODEC.encode(buf, tag);
	}

	private StoredItemStack read(RegistryFriendlyByteBuf buf) {
		byte flags = buf.readByte();
		int id = buf.readVarInt();
		boolean rd = (flags & 4) == 0;
		StoredItemStack stack;
		if(rd) {
			stack = new StoredItemStack(new ItemStack(BuiltInRegistries.ITEM.byId(buf.readVarInt())));
		} else {
			stack = new StoredItemStack(idMap2.get(id).getStack());
		}
		long count = (flags & 1) != 0 ? 0 : buf.readVarLong();
		stack.setCount(count);
		if(rd && (flags & 2) != 0) {
			DataComponentPatch dataComponentPatch = DataComponentPatch.STREAM_CODEC.decode(buf);
			stack.getStack().applyComponents(dataComponentPatch);
		}
		idMap.put(stack, id);
		idMap2.put(id, stack);
		return stack;
	}

	public void update(int changeID, Map<StoredItemStack, StoredItemStack> items, ServerPlayer player, Consumer<CompoundTag> extraSync) {
		if (changeID != lastChangeID) {
			lastChangeID = changeID;
			List<StoredItemStack> toWrite = new ArrayList<>();
			Set<StoredItemStack> found = new HashSet<>();
			items.forEach((s, c) -> {
				StoredItemStack pc = this.items.get(s);
				if(pc != null)found.add(s);
				if(pc == null || !c.equalDetails(pc)) {
					toWrite.add(c);
				}
			});
			this.items.forEach((s, c) -> {
				if(!found.contains(s))
					toWrite.add(new StoredItemStack(s.getStack(), 0L));
			});
			this.items.clear();
			this.items.putAll(items);
			if(!toWrite.isEmpty()) {
				workBuf.writerIndex(0);
				int j = 0;
				for (int i = 0; i < toWrite.size(); i++, j++) {
					StoredItemStack stack = toWrite.get(i);
					int li = workBuf.writerIndex();
					try {
						write(workBuf, stack);
					} catch (IndexOutOfBoundsException e) {
						workBuf.writerIndex(li);
						writeMiniStack(workBuf, stack);
					}
					int s = workBuf.writerIndex();
					if((s > MAX_PACKET_SIZE || j > 32000) && j > 1) {
						CompoundTag t = writeBuf("d", workBuf, li);
						t.putShort("l", (short) j);
						NetworkHandler.sendTo(player, t);
						j = 0;
						workBuf.writerIndex(0);
						if(s - li > MAX_PACKET_SIZE) {
							writeMiniStack(workBuf, stack);
						} else {
							workBuf.writeBytes(workBuf, li, s - li);
						}
					}
				}
				if(j > 0 || extraSync != null) {
					CompoundTag t;
					if(j > 0) {
						t = writeBuf("d", workBuf, workBuf.writerIndex());
						t.putShort("l", (short) j);
					} else t = new CompoundTag();
					if(extraSync != null)extraSync.accept(t);
					NetworkHandler.sendTo(player, t);
					return;
				}
			}
		}
		if(extraSync != null) {
			CompoundTag t = new CompoundTag();
			extraSync.accept(t);
			NetworkHandler.sendTo(player, t);
		}
	}

	public boolean receiveUpdate(RegistryAccess registryAccess, CompoundTag tag) {
		if(tag.contains("d")) {
			RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(tag.getByteArray("d")), registryAccess);
			List<StoredItemStack> in = new ArrayList<>();
			short len = tag.getShort("l");
			for (int i = 0; i < len; i++) {
				in.add(read(buf));
			}
			in.forEach(s -> {
				if(s.getQuantity() == 0) {
					this.itemList.remove(s);
				} else {
					this.itemList.put(s, s);
				}
			});
			return true;
		}
		return false;
	}

	public void sendInteract(StoredItemStack intStack, SlotAction action, boolean mod) {
		FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
		int flags = mod ? 1 : 0;
		if(intStack == null) {
			buf.writeByte(flags | 2);
		} else {
			buf.writeByte(flags);
			buf.writeVarInt(idMap.getInt(intStack));
			buf.writeVarLong(intStack.getQuantity());
		}
		buf.writeEnum(action);
		NetworkHandler.sendDataToServer(writeBuf("a", buf, buf.writerIndex()));
	}

	private CompoundTag writeBuf(String id, FriendlyByteBuf buf, int len) {
		byte[] data = new byte[len];
		buf.getBytes(0, data);
		CompoundTag tag = new CompoundTag();
		tag.putByteArray(id, data);
		return tag;
	}

	public void receiveInteract(CompoundTag tag, InteractHandler handler) {
		if(tag.contains("a")) {
			FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(tag.getByteArray("a")));
			byte flags = buf.readByte();
			StoredItemStack stack;
			if((flags & 2) != 0) {
				stack = null;
			} else {
				stack = new StoredItemStack(idMap2.get(buf.readVarInt()).getStack());
				long count = buf.readVarLong();
				stack.setCount(count);
			}
			handler.onInteract(stack, buf.readEnum(SlotAction.class), (flags & 1) != 0);
		}
	}

	public List<StoredItemStack> getAsList() {
		return new ArrayList<>(this.itemList.values());
	}

	public void fillStackedContents(StackedContents stc) {
		items.forEach((s, c) -> stc.accountSimpleStack(c.getActualStack()));
	}

	public long getAmount(StoredItemStack stack) {
		StoredItemStack s = itemList.get(stack);
		return s != null ? s.getQuantity() : 0L;
	}

	public static interface InteractHandler {
		void onInteract(StoredItemStack intStack, SlotAction action, boolean mod);
	}

	public static enum SlotAction {
		PULL_OR_PUSH_STACK, PULL_ONE, SPACE_CLICK, SHIFT_PULL, GET_HALF, GET_QUARTER, CRAFT;
		public static final SlotAction[] VALUES = values();
	}
}

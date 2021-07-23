package com.tom.storagemod.block;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.BlockHitResult;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;

import com.tom.storagemod.TickerUtil;
import com.tom.storagemod.proxy.ClientProxy;
import com.tom.storagemod.tile.TileEntityInventoryProxy;
import com.tom.storagemod.tile.TileEntityPainted;

public class BlockInventoryProxy extends BaseEntityBlock implements IPaintable {
	public static final DirectionProperty FACING = BlockStateProperties.FACING;
	public static final EnumProperty<DirectionWithNull> FILTER_FACING = EnumProperty.create("filter_facing", DirectionWithNull.class);

	public BlockInventoryProxy() {
		super(Block.Properties.of(Material.WOOD).strength(3).harvestTool(ToolType.AXE));
		setRegistryName("ts.inventory_proxy");
		registerDefaultState(defaultBlockState().setValue(FACING, Direction.DOWN).setValue(FILTER_FACING, DirectionWithNull.NULL));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, BlockGetter worldIn, List<Component> tooltip,
			TooltipFlag flagIn) {
		tooltip.add(new TranslatableComponent("tooltip.toms_storage.paintable"));
		ClientProxy.tooltip("inventory_proxy", tooltip);
		if(Screen.hasShiftDown()) {
			tooltip.add(new TranslatableComponent("tooltip.toms_storage.inventory_proxy.key", "ignoreSize", new TranslatableComponent("tooltip.toms_storage.inventory_proxy.ignoreSize")));
			tooltip.add(new TranslatableComponent("tooltip.toms_storage.inventory_proxy.value", "maxCount", new TranslatableComponent("tooltip.toms_storage.inventory_proxy.maxCount.arg"), new TranslatableComponent("tooltip.toms_storage.inventory_proxy.maxCount.desc")));
		}
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TileEntityInventoryProxy(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state,
			BlockEntityType<T> type) {
		return TickerUtil.createTicker(world, false, true);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rot) {
		return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirrorIn) {
		return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState().setValue(FACING, context.getClickedFace().getOpposite());
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, FILTER_FACING);
	}

	@Override
	public RenderShape getRenderShape(BlockState p_149645_1_) {
		return RenderShape.MODEL;
	}

	@Override
	public boolean paint(Level world, BlockPos pos, BlockState to) {
		BlockEntity te = world.getBlockEntity(pos);
		if(te != null && te instanceof TileEntityPainted)
			return ((TileEntityPainted)te).setPaintedBlockState(to);
		return false;
	}

	@Override
	public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
		List<ItemStack> stacks = super.getDrops(state, builder);
		if(state.getValue(FILTER_FACING) != DirectionWithNull.NULL)
			stacks.add(new ItemStack(Items.DIAMOND));
		return stacks;
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos) {
		BlockEntity te = world.getBlockEntity(pos);
		if(state.getValue(FILTER_FACING) != DirectionWithNull.NULL) {
			if(te instanceof TileEntityInventoryProxy) {
				return ((TileEntityInventoryProxy) te).getComparatorOutput();
			}
		}
		return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(te);
	}

	@Override
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand,
			BlockHitResult hit) {
		ItemStack stack = player.getItemInHand(hand);
		if(stack.getItem() == Items.DIAMOND && state.getValue(FACING) != hit.getDirection()) {
			if(state.getValue(FILTER_FACING) == DirectionWithNull.NULL && !player.getAbilities().instabuild) {
				stack.shrink(1);
			}
			world.setBlockAndUpdate(pos, state.setValue(FILTER_FACING, DirectionWithNull.of(hit.getDirection())));
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	public static enum DirectionWithNull implements StringRepresentable {
		NULL("notset"),
		DOWN(Direction.DOWN),
		UP(Direction.UP),
		NORTH(Direction.NORTH),
		SOUTH(Direction.SOUTH),
		WEST(Direction.WEST),
		EAST(Direction.EAST)
		;
		private final String name;
		private final Direction dir;

		private static final Map<Direction, DirectionWithNull> dir2dirwn = new HashMap<>();

		static {
			for (DirectionWithNull dwn : values()) {
				if(dwn.dir != null)
					dir2dirwn.put(dwn.dir, dwn);
			}
		}

		private DirectionWithNull(Direction dir) {
			this.name = dir.getSerializedName();
			this.dir = dir;
		}

		public static DirectionWithNull of(Direction side) {
			return dir2dirwn.get(side);
		}

		private DirectionWithNull(String name) {
			this.name = name;
			dir = null;
		}

		@Override
		public String getSerializedName() {
			return name;
		}

		public Direction getDir() {
			return dir;
		}
	}
}

package com.tom.storagemod.block;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.StorageModClient;
import com.tom.storagemod.TickerUtil;
import com.tom.storagemod.tile.InventoryProxyBlockEntity;
import com.tom.storagemod.tile.PaintedBlockEntity;

public class InventoryProxyBlock extends BlockWithEntity implements IPaintable {
	public static final DirectionProperty FACING = Properties.FACING;
	public static final EnumProperty<DirectionWithNull> FILTER_FACING = EnumProperty.of("filter_facing", DirectionWithNull.class);

	public InventoryProxyBlock() {
		super(Block.Settings.of(Material.WOOD).strength(3));
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void appendTooltip(ItemStack stack, BlockView worldIn, List<Text> tooltip,
			TooltipContext flagIn) {
		tooltip.add(Text.translatable("tooltip.toms_storage.paintable"));
		StorageModClient.tooltip("inventory_proxy", tooltip);
		if(Screen.hasShiftDown()) {
			tooltip.add(Text.translatable("tooltip.toms_storage.inventory_proxy.key", "ignoreSize", Text.translatable("tooltip.toms_storage.inventory_proxy.ignoreSize")));
			tooltip.add(Text.translatable("tooltip.toms_storage.inventory_proxy.value", "maxCount", Text.translatable("tooltip.toms_storage.inventory_proxy.maxCount.arg"), Text.translatable("tooltip.toms_storage.inventory_proxy.maxCount.desc")));
		}
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
			BlockHitResult hit) {
		ItemStack stack = player.getStackInHand(hand);
		if(stack.getItem() == Items.DIAMOND && state.get(FACING) != hit.getSide()) {
			if(state.get(FILTER_FACING) == DirectionWithNull.NULL && !player.getAbilities().creativeMode) {
				stack.decrement(1);
			}
			world.setBlockState(pos, state.with(FILTER_FACING, DirectionWithNull.of(hit.getSide())));
			return ActionResult.SUCCESS;
		}
		return ActionResult.PASS;
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new InventoryProxyBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state,
			BlockEntityType<T> type) {
		return TickerUtil.createTicker(world, false, true);
	}

	@Override
	public BlockState rotate(BlockState state, BlockRotation rot) {
		return state.with(FACING, rot.rotate(state.get(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, BlockMirror mirrorIn) {
		return state.rotate(mirrorIn.getRotation(state.get(FACING)));
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext context) {
		return getDefaultState().with(FACING, context.getSide().getOpposite());
	}

	@Override
	protected void appendProperties(Builder<Block, BlockState> builder) {
		builder.add(FACING, FILTER_FACING);
	}

	@Override
	public BlockRenderType getRenderType(BlockState p_149645_1_) {
		return BlockRenderType.MODEL;
	}

	@Override
	public List<ItemStack> getDroppedStacks(BlockState state, net.minecraft.loot.context.LootContext.Builder builder) {
		List<ItemStack> stacks = super.getDroppedStacks(state, builder);
		if(state.get(FILTER_FACING) != DirectionWithNull.NULL)
			stacks.add(new ItemStack(Items.DIAMOND));
		return stacks;
	}

	@Override
	public boolean hasComparatorOutput(BlockState state) {
		return true;
	}

	@Override
	public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
		BlockEntity te = world.getBlockEntity(pos);
		if(state.get(FILTER_FACING) != DirectionWithNull.NULL) {
			if(te instanceof InventoryProxyBlockEntity) {
				return ((InventoryProxyBlockEntity) te).getComparatorOutput();
			}
		}
		return ScreenHandler.calculateComparatorOutput(te);
	}

	public static enum DirectionWithNull implements StringIdentifiable {
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
			this.name = dir.asString();
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
		public String asString() {
			return name;
		}

		public Direction getDir() {
			return dir;
		}
	}

	@Override
	public boolean paint(World world, BlockPos pos, BlockState to) {
		BlockState old = world.getBlockState(pos);
		world.setBlockState(pos, StorageMod.invProxyPainted.getDefaultState().with(FACING, old.get(FACING)).with(FILTER_FACING, old.get(FILTER_FACING)), 2);
		BlockEntity te = world.getBlockEntity(pos);
		if(te != null && te instanceof PaintedBlockEntity)
			return ((PaintedBlockEntity)te).setPaintedBlockState(to);
		return false;
	}

	@Override
	public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
		return new ItemStack(StorageMod.invProxy);
	}
}

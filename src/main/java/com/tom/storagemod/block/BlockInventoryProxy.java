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
import net.minecraft.text.TranslatableText;
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

import com.tom.fabriclibs.ext.IBlock;
import com.tom.storagemod.proxy.ClientProxy;
import com.tom.storagemod.tile.TileEntityInventoryProxy;
import com.tom.storagemod.tile.TileEntityPainted;

public class BlockInventoryProxy extends BlockWithEntity implements IPaintable, IBlock {
	public static final DirectionProperty FACING = Properties.FACING;
	public static final EnumProperty<DirectionWithNull> FILTER_FACING = EnumProperty.of("filter_facing", DirectionWithNull.class);

	public BlockInventoryProxy() {
		super(Block.Settings.of(Material.WOOD).strength(3));//.harvestTool(ToolType.AXE)
		setRegistryName("ts.inventory_proxy");
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void buildTooltip(ItemStack stack, BlockView worldIn, List<Text> tooltip,
			TooltipContext flagIn) {
		tooltip.add(new TranslatableText("tooltip.toms_storage.paintable"));
		ClientProxy.tooltip("inventory_proxy", tooltip);
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
			BlockHitResult hit) {
		ItemStack stack = player.getStackInHand(hand);
		if(stack.getItem() == Items.DIAMOND && state.get(FACING) != hit.getSide()) {
			if(state.get(FILTER_FACING) == DirectionWithNull.NULL && !player.abilities.creativeMode) {
				stack.decrement(1);
			}
			world.setBlockState(pos, state.with(FILTER_FACING, DirectionWithNull.of(hit.getSide())));
			return ActionResult.SUCCESS;
		}
		return ActionResult.PASS;
	}

	@Override
	public BlockEntity createBlockEntity(BlockView worldIn) {
		return new TileEntityInventoryProxy();
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
	public boolean paint(World world, BlockPos pos, BlockState to) {
		BlockEntity te = world.getBlockEntity(pos);
		if(te != null && te instanceof TileEntityPainted)
			return ((TileEntityPainted)te).setPaintedBlockState(to);
		return false;
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
			if(te instanceof TileEntityInventoryProxy) {
				return ((TileEntityInventoryProxy) te).getComparatorOutput();
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
}

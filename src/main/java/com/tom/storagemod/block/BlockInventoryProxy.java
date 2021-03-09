package com.tom.storagemod.block;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ContainerBlock;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;

import com.tom.storagemod.proxy.ClientProxy;
import com.tom.storagemod.tile.TileEntityInventoryProxy;
import com.tom.storagemod.tile.TileEntityPainted;

public class BlockInventoryProxy extends ContainerBlock implements IPaintable {
	public static final DirectionProperty FACING = BlockStateProperties.FACING;
	public static final EnumProperty<DirectionWithNull> FILTER_FACING = EnumProperty.create("filter_facing", DirectionWithNull.class);

	public BlockInventoryProxy() {
		super(Block.Properties.create(Material.WOOD).hardnessAndResistance(3).harvestTool(ToolType.AXE));
		setRegistryName("ts.inventory_proxy");
		setDefaultState(getDefaultState().with(FACING, Direction.DOWN).with(FILTER_FACING, DirectionWithNull.NULL));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, IBlockReader worldIn, List<ITextComponent> tooltip,
			ITooltipFlag flagIn) {
		tooltip.add(new TranslationTextComponent("tooltip.toms_storage.paintable"));
		ClientProxy.tooltip("inventory_proxy", tooltip);
		if(Screen.hasShiftDown()) {
			tooltip.add(new TranslationTextComponent("tooltip.toms_storage.inventory_proxy.key", "ignoreSize", new TranslationTextComponent("tooltip.toms_storage.inventory_proxy.ignoreSize")));
			tooltip.add(new TranslationTextComponent("tooltip.toms_storage.inventory_proxy.value", "maxCount", new TranslationTextComponent("tooltip.toms_storage.inventory_proxy.maxCount.arg"), new TranslationTextComponent("tooltip.toms_storage.inventory_proxy.maxCount.desc")));
		}
	}

	@Override
	public TileEntity createNewTileEntity(IBlockReader worldIn) {
		return new TileEntityInventoryProxy();
	}
	@Override
	public BlockState rotate(BlockState state, Rotation rot) {
		return state.with(FACING, rot.rotate(state.get(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirrorIn) {
		return state.rotate(mirrorIn.toRotation(state.get(FACING)));
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return getDefaultState().with(FACING, context.getFace().getOpposite());
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(FACING, FILTER_FACING);
	}

	@Override
	public BlockRenderType getRenderType(BlockState p_149645_1_) {
		return BlockRenderType.MODEL;
	}

	@Override
	public boolean paint(World world, BlockPos pos, BlockState to) {
		TileEntity te = world.getTileEntity(pos);
		if(te != null && te instanceof TileEntityPainted)
			return ((TileEntityPainted)te).setPaintedBlockState(to);
		return false;
	}

	@Override
	public List<ItemStack> getDrops(BlockState state, net.minecraft.loot.LootContext.Builder builder) {
		List<ItemStack> stacks = super.getDrops(state, builder);
		if(state.get(FILTER_FACING) != DirectionWithNull.NULL)
			stacks.add(new ItemStack(Items.DIAMOND));
		return stacks;
	}

	@Override
	public boolean hasComparatorInputOverride(BlockState state) {
		return true;
	}

	@Override
	public int getComparatorInputOverride(BlockState state, World world, BlockPos pos) {
		TileEntity te = world.getTileEntity(pos);
		if(state.get(FILTER_FACING) != DirectionWithNull.NULL) {
			if(te instanceof TileEntityInventoryProxy) {
				return ((TileEntityInventoryProxy) te).getComparatorOutput();
			}
		}
		return Container.calcRedstone(te);
	}

	@Override
	public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
			BlockRayTraceResult hit) {
		ItemStack stack = player.getHeldItem(hand);
		if(stack.getItem() == Items.DIAMOND && state.get(FACING) != hit.getFace()) {
			if(state.get(FILTER_FACING) == DirectionWithNull.NULL && !player.abilities.isCreativeMode) {
				stack.shrink(1);
			}
			world.setBlockState(pos, state.with(FILTER_FACING, DirectionWithNull.of(hit.getFace())));
			return ActionResultType.SUCCESS;
		}
		return ActionResultType.PASS;
	}

	public static enum DirectionWithNull implements IStringSerializable {
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
			this.name = dir.getString();
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
		public String getString() {
			return name;
		}

		public Direction getDir() {
			return dir;
		}
	}
}

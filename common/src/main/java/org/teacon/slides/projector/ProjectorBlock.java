package org.teacon.slides.projector;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.teacon.slides.Registry;
import org.teacon.slides.Slideshow;
import org.teacon.slides.mappings.BlockEntityMapper;
import org.teacon.slides.mappings.EntityBlockMapper;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.Locale;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.POWERED;

@SuppressWarnings("deprecation")
@ParametersAreNonnullByDefault
public final class ProjectorBlock extends Block implements EntityBlockMapper {

    public static final EnumProperty<InternalRotation>
            ROTATION = EnumProperty.create("rotation", InternalRotation.class);
    public static final EnumProperty<Direction>
            BASE = EnumProperty.create("base", Direction.class, Direction.Plane.VERTICAL);

    private static final VoxelShape SHAPE_WITH_BASE_UP = Block.box(0.0, 4.0, 0.0, 16.0, 16.0, 16.0);
    private static final VoxelShape SHAPE_WITH_BASE_DOWN = Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0);

    public ProjectorBlock() {
        super(Block.Properties.of(Material.METAL)
                .strength(20F)
                .lightLevel(state -> 15) // TODO Configurable
                .noCollission());
        registerDefaultState(defaultBlockState()
                .setValue(BASE, Direction.DOWN)
                .setValue(FACING, Direction.EAST)
                .setValue(POWERED, Boolean.FALSE)
                .setValue(ROTATION, InternalRotation.NONE));
    }

    @Nonnull
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        switch (state.getValue(BASE)) {
            case DOWN:
                return SHAPE_WITH_BASE_DOWN;
            case UP:
                return SHAPE_WITH_BASE_UP;
            default:
                throw new AssertionError();
        }
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(BASE, FACING, POWERED, ROTATION);
    }

    @Nonnull
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getNearestLookingDirection().getOpposite();
        Direction horizontalFacing = context.getHorizontalDirection().getOpposite();
        Direction base = Arrays.stream(context.getNearestLookingDirections())
                .filter(Direction.Plane.VERTICAL)
                .findFirst()
                .orElse(Direction.DOWN);
        InternalRotation rotation =
                InternalRotation.VALUES[4 + Math.floorMod(facing.getStepY() * horizontalFacing.get2DDataValue(), 4)];
        return defaultBlockState()
                .setValue(BASE, base)
                .setValue(FACING, facing)
                .setValue(POWERED, Boolean.FALSE)
                .setValue(ROTATION, rotation);
    }

    @Override
    public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
                                boolean isMoving) {
        boolean powered = worldIn.hasNeighborSignal(pos);
        if (powered != state.getValue(POWERED)) {
            worldIn.setBlockAndUpdate(pos, state.setValue(POWERED, powered));
        }
    }

    @Override
    public void onPlace(BlockState state, Level worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!oldState.is(state.getBlock())) {
            boolean powered = worldIn.hasNeighborSignal(pos);
            if (powered != state.getValue(POWERED)) {
                worldIn.setBlockAndUpdate(pos, state.setValue(POWERED, powered));
            }
        }
    }

    @Nonnull
    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        Direction direction = state.getValue(FACING);
        switch (direction) {
            case DOWN:
            case UP:
                return state.setValue(ROTATION, state.getValue(ROTATION).compose(Rotation.CLOCKWISE_180));
            default:
                return state.setValue(FACING, mirror.getRotation(direction).rotate(direction));
        }
    }

    @Nonnull
    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        Direction direction = state.getValue(FACING);
        switch (direction) {
            case DOWN:
            case UP:
                return state.setValue(ROTATION, state.getValue(ROTATION).compose(rotation));
            default:
                return state.setValue(FACING, rotation.rotate(direction));
        }
    }

    @Override
    public BlockEntityMapper createBlockEntity(BlockPos pos, BlockState state) {
        return new ProjectorBlockEntity(pos, state);
    }

    @Nonnull
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        } else if (player instanceof ServerPlayer && hasPermission((ServerPlayer) player)) {
            final BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof ProjectorBlockEntity) {
                ((ProjectorBlockEntity) blockEntity).syncData();
                final FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
                packet.writeBlockPos(pos);
                Registry.sendToPlayer((ServerPlayer) player, Slideshow.PACKET_OPEN_GUI, packet);
            }
            return InteractionResult.CONSUME;
        } else {
            return InteractionResult.FAIL;
        }
    }

    public static boolean hasPermission(ServerPlayer serverPlayer) {
        return hasPermission(serverPlayer.gameMode.getGameModeForPlayer());
    }

    private static boolean hasPermission(GameType gameType) {
        return gameType == GameType.CREATIVE || gameType == GameType.SURVIVAL;
    }

    public enum InternalRotation implements StringRepresentable {
        NONE(new float[]{1F, 0F, 0F, 0F, 0F, 1F, 0F, 0F, 0F, 0F, 1F, 0F, 0F, 0F, 0F, 1F}),
        CLOCKWISE_90(new float[]{0F, 0F, -1F, 0F, 0F, 1F, 0F, 0F, 1F, 0F, 0F, 0F, 0F, 0F, 0F, 1F}),
        CLOCKWISE_180(new float[]{-1F, 0F, 0F, 0F, 0F, 1F, 0F, 0F, 0F, 0F, -1F, 0F, 0F, 0F, 0F, 1F}),
        COUNTERCLOCKWISE_90(new float[]{0F, 0F, 1F, 0F, 0F, 1F, 0F, 0F, -1F, 0F, 0F, 0F, 0F, 0F, 0F, 1F}),
        HORIZONTAL_FLIPPED(new float[]{-1F, 0F, 0F, 0F, 0F, -1F, 0F, 0F, 0F, 0F, 1F, 0F, 0F, 0F, 0F, 1F}),
        DIAGONAL_FLIPPED(new float[]{0F, 0F, -1F, 0F, 0F, -1F, 0F, 0F, -1F, 0F, 0F, 0F, 0F, 0F, 0F, 1F}),
        VERTICAL_FLIPPED(new float[]{1F, 0F, 0F, 0F, 0F, -1F, 0F, 0F, 0F, 0F, -1F, 0F, 0F, 0F, 0F, 1F}),
        ANTI_DIAGONAL_FLIPPED(new float[]{0F, 0F, 1F, 0F, 0F, -1F, 0F, 0F, 1F, 0F, 0F, 0F, 0F, 0F, 0F, 1F});

        public static final InternalRotation[] VALUES = values();

        private static final int[]
                INV_INDICES = {0, 3, 2, 1, 4, 5, 6, 7},
                FLIP_INDICES = {4, 7, 6, 5, 0, 3, 2, 1};
        private static final int[][] ROTATION_INDICES = {
                {0, 1, 2, 3, 4, 5, 6, 7},
                {1, 2, 3, 0, 5, 6, 7, 4},
                {2, 3, 0, 1, 6, 7, 4, 5},
                {3, 0, 1, 2, 7, 4, 5, 6}
        };

        private final String mSerializedName;
        private final Matrix4f mMatrix;
        private final Matrix3f mNormal;

        InternalRotation(float[] matrix) {
            mSerializedName = name().toLowerCase(Locale.ROOT);
            mMatrix = new Matrix4f();
            load(mMatrix, FloatBuffer.wrap(matrix));
            mNormal = new Matrix3f(mMatrix);
        }

        public InternalRotation compose(Rotation rotation) {
            return VALUES[ROTATION_INDICES[rotation.ordinal()][ordinal()]];
        }

        public InternalRotation flip() {
            return VALUES[FLIP_INDICES[ordinal()]];
        }

        public InternalRotation invert() {
            return VALUES[INV_INDICES[ordinal()]];
        }

        public boolean isFlipped() {
            return ordinal() >= 4;
        }

        public Vector4f transform(Vector4f vector) {
            return vector.mul(mMatrix);
        }

        public Matrix4f transform(Matrix4f poseMatrix) {
            return poseMatrix.mul(mMatrix);
        }

        public Matrix3f transform(Matrix3f normalMatrix) {
            return normalMatrix.mul(mNormal);
        }

        @Override
        public final String getSerializedName() {
            return mSerializedName;
        }

        private static void load(Matrix4f matrix4f, FloatBuffer floatBuffer) {
            matrix4f.set(new Matrix4f(
                    floatBuffer.get(bufferIndex(0, 0)),
                    floatBuffer.get(bufferIndex(0, 1)),
                    floatBuffer.get(bufferIndex(0, 2)),
                    floatBuffer.get(bufferIndex(0, 3)),
                    floatBuffer.get(bufferIndex(1, 0)),
                    floatBuffer.get(bufferIndex(1, 1)),
                    floatBuffer.get(bufferIndex(1, 2)),
                    floatBuffer.get(bufferIndex(1, 3)),
                    floatBuffer.get(bufferIndex(2, 0)),
                    floatBuffer.get(bufferIndex(2, 1)),
                    floatBuffer.get(bufferIndex(2, 2)),
                    floatBuffer.get(bufferIndex(2, 3)),
                    floatBuffer.get(bufferIndex(3, 0)),
                    floatBuffer.get(bufferIndex(3, 1)),
                    floatBuffer.get(bufferIndex(3, 2)),
                    floatBuffer.get(bufferIndex(3, 3))
            ));
        }

        private static int bufferIndex(int i, int j) {
            return j * 4 + i;
        }
    }
}

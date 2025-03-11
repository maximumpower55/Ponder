package net.createmod.catnip.render;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;

import dev.engine_room.flywheel.lib.model.baked.EmptyVirtualBlockGetter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

public class SuperBufferFactory {

	private static final ThreadLocal<ThreadLocalObjects> THREAD_LOCAL_OBJECTS = ThreadLocal.withInitial(ThreadLocalObjects::new);

	public static final SuperBufferFactory INSTANCE = new SuperBufferFactory();

	public SuperByteBuffer create(MeshData data) {
		return new DefaultSuperByteBuffer(data);
	}

	public SuperByteBuffer createForBlock(BlockState renderedState) {
		return createForBlock(Minecraft.getInstance().getBlockRenderer().getBlockModel(renderedState), renderedState);
	}

	public SuperByteBuffer createForBlock(BakedModel model, BlockState referenceState) {
		return createForBlock(model, referenceState, new PoseStack());
	}

	public SuperByteBuffer createForBlock(BakedModel model, BlockState state, @Nullable PoseStack poseStack) {
		ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();

		if (poseStack == null) {
			poseStack = objects.identityPoseStack;
		}
		RandomSource random = objects.random;

		ShadedBlockSbbBuilder sbbBuilder = objects.sbbBuilder;
		sbbBuilder.begin();
		sbbBuilder.bufferBlock(EmptyVirtualBlockGetter.FULL_DARK, model, state, BlockPos.ZERO, poseStack, random);
		return sbbBuilder.end();
	}

	private static class ThreadLocalObjects {
		public final PoseStack identityPoseStack = new PoseStack();
		public final RandomSource random = RandomSource.createNewThreadLocalInstance();
		public final ShadedBlockSbbBuilder sbbBuilder = ShadedBlockSbbBuilder.createForPonder();
	}
}

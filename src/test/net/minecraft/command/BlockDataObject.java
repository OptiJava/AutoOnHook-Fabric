/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Locale;
import java.util.function.Function;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.command.DataCommandObject;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.NbtPathArgumentType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.DataCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;

public class BlockDataObject
implements DataCommandObject {
    static final SimpleCommandExceptionType INVALID_BLOCK_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.data.block.invalid"));
    public static final Function<String, DataCommand.ObjectType> TYPE_FACTORY = string -> new DataCommand.ObjectType((String)string){
        final /* synthetic */ String field_13787;
        {
            this.field_13787 = string;
        }

        @Override
        public DataCommandObject getObject(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
            BlockPos blockPos = BlockPosArgumentType.getLoadedBlockPos(context, this.field_13787 + "Pos");
            BlockEntity blockEntity = context.getSource().getWorld().getBlockEntity(blockPos);
            if (blockEntity == null) {
                throw INVALID_BLOCK_EXCEPTION.create();
            }
            return new BlockDataObject(blockEntity, blockPos);
        }

        @Override
        public ArgumentBuilder<ServerCommandSource, ?> addArgumentsToBuilder(ArgumentBuilder<ServerCommandSource, ?> argument, Function<ArgumentBuilder<ServerCommandSource, ?>, ArgumentBuilder<ServerCommandSource, ?>> argumentAdder) {
            return argument.then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.literal("block").then(argumentAdder.apply(CommandManager.argument(this.field_13787 + "Pos", BlockPosArgumentType.blockPos()))));
        }
    };
    private final BlockEntity blockEntity;
    private final BlockPos pos;

    public BlockDataObject(BlockEntity blockEntity, BlockPos pos) {
        this.blockEntity = blockEntity;
        this.pos = pos;
    }

    @Override
    public void setNbt(NbtCompound nbt) {
        nbt.putInt("x", this.pos.getX());
        nbt.putInt("y", this.pos.getY());
        nbt.putInt("z", this.pos.getZ());
        BlockState blockState = this.blockEntity.getWorld().getBlockState(this.pos);
        this.blockEntity.readNbt(nbt);
        this.blockEntity.markDirty();
        this.blockEntity.getWorld().updateListeners(this.pos, blockState, blockState, Block.NOTIFY_ALL);
    }

    @Override
    public NbtCompound getNbt() {
        return this.blockEntity.writeNbt(new NbtCompound());
    }

    @Override
    public Text feedbackModify() {
        return new TranslatableText("commands.data.block.modified", this.pos.getX(), this.pos.getY(), this.pos.getZ());
    }

    @Override
    public Text feedbackQuery(NbtElement element) {
        return new TranslatableText("commands.data.block.query", this.pos.getX(), this.pos.getY(), this.pos.getZ(), NbtHelper.toPrettyPrintedText(element));
    }

    @Override
    public Text feedbackGet(NbtPathArgumentType.NbtPath path, double scale, int result) {
        return new TranslatableText("commands.data.block.get", path, this.pos.getX(), this.pos.getY(), this.pos.getZ(), String.format(Locale.ROOT, "%.2f", scale), result);
    }
}


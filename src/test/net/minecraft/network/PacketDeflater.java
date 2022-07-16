/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import java.util.zip.Deflater;
import net.minecraft.network.PacketByteBuf;

public class PacketDeflater
extends MessageToByteEncoder<ByteBuf> {
    private final byte[] deflateBuffer = new byte[8192];
    private final Deflater deflater;
    private int compressionThreshold;

    public PacketDeflater(int compressionThreshold) {
        this.compressionThreshold = compressionThreshold;
        this.deflater = new Deflater();
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, ByteBuf byteBuf2) {
        int i = byteBuf.readableBytes();
        PacketByteBuf packetByteBuf = new PacketByteBuf(byteBuf2);
        if (i < this.compressionThreshold) {
            packetByteBuf.writeVarInt(0);
            packetByteBuf.writeBytes(byteBuf);
        } else {
            byte[] bs = new byte[i];
            byteBuf.readBytes(bs);
            packetByteBuf.writeVarInt(bs.length);
            this.deflater.setInput(bs, 0, i);
            this.deflater.finish();
            while (!this.deflater.finished()) {
                int j = this.deflater.deflate(this.deflateBuffer);
                packetByteBuf.writeBytes(this.deflateBuffer, 0, j);
            }
            this.deflater.reset();
        }
    }

    public int getCompressionThreshold() {
        return this.compressionThreshold;
    }

    public void setCompressionThreshold(int compressionThreshold) {
        this.compressionThreshold = compressionThreshold;
    }

    @Override
    protected /* synthetic */ void encode(ChannelHandlerContext channelHandlerContext, Object object, ByteBuf byteBuf) throws Exception {
        this.encode(channelHandlerContext, (ByteBuf)object, byteBuf);
    }
}


package craft.meow.meowsync.wdpe.packets;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.codec.BedrockPacketSerializer;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacketHandler;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacketType;
import org.cloudburstmc.protocol.common.PacketSignal;

/**
 * 代理端表示的服务端自定义数据包
 * {@code craft.meow.meowsync.nukkit.packets.ForwardPacket}。
 *
 * <p>这是一个 p2s2p 的通用转发包：代理端 -&gt; 下游服务端 -&gt; 代理端。
 * {@code msgId} 用来关联一次请求和它的回包，{@code data} 承载原始字节。
 * 该数据包属于代理端与服务端的内部协议，绝不允许下发到玩家客户端。
 */
@ToString(exclude = "data")
public class ForwardPacket implements BedrockPacket, BedrockPacketSerializer<ForwardPacket> {

    public static final int NETWORK_ID = 501;

    private static final byte[] EMPTY = new byte[0];

    @Getter
    @Setter
    private int msgId;

    @Getter
    @Setter
    private byte[] data = EMPTY;

    public ForwardPacket() {
    }

    public ForwardPacket(int msgId, byte[] data) {
        this.msgId = msgId;
        this.data = data == null ? EMPTY : data;
    }

    @Override
    public PacketSignal handle(BedrockPacketHandler handler) {
        return PacketSignal.UNHANDLED;
    }

    @Override
    public BedrockPacketType getPacketType() {
        return BedrockPacketType.UNKNOWN;
    }

    @Override
    public ForwardPacket clone() {
        return new ForwardPacket(this.msgId, this.data == null ? EMPTY : this.data.clone());
    }

    @Override
    public void serialize(ByteBuf buffer, BedrockCodecHelper helper, ForwardPacket packet) {
        // msgId 采用大端 4 字节，与服务端 BinaryStream#putInt / #getInt 对应
        buffer.writeInt(packet.msgId);
        helper.writeByteArray(buffer, packet.data == null ? EMPTY : packet.data);
    }

    @Override
    public void deserialize(ByteBuf buffer, BedrockCodecHelper helper, ForwardPacket packet) {
        packet.msgId = buffer.readInt();
        packet.data = helper.readByteArray(buffer);
    }
}

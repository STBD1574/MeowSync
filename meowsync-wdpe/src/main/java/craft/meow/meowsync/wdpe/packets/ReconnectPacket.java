package craft.meow.meowsync.wdpe.packets;

import io.netty.buffer.ByteBuf;
import lombok.ToString;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.codec.BedrockPacketSerializer;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacketHandler;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacketType;
import org.cloudburstmc.protocol.common.PacketSignal;

/**
 * 代理端表示的服务端自定义数据包
 * {@code craft.meow.meowsync.nukkit.packets.ReconnectPacket}。
 *
 * <p>下游服务端通过该数据包 (s2p) 请求代理端让当前玩家重新连接
 * （例如服务端重载、数据刷新后需要客户端重新进入）。该数据包没有负载。
 */
@ToString
public class ReconnectPacket implements BedrockPacket, BedrockPacketSerializer<ReconnectPacket> {

    public static final int NETWORK_ID = 503;

    @Override
    public PacketSignal handle(BedrockPacketHandler handler) {
        return PacketSignal.UNHANDLED;
    }

    @Override
    public BedrockPacketType getPacketType() {
        return BedrockPacketType.UNKNOWN;
    }

    @Override
    public ReconnectPacket clone() {
        return new ReconnectPacket();
    }

    @Override
    public void serialize(ByteBuf buffer, BedrockCodecHelper helper, ReconnectPacket packet) {
        // 无负载
    }

    @Override
    public void deserialize(ByteBuf buffer, BedrockCodecHelper helper, ReconnectPacket packet) {
        // 无负载
    }
}

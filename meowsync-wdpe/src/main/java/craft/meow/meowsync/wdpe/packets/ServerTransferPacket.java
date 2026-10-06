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
 * {@code craft.meow.meowsync.nukkit.packets.ServerTransferPacket}。
 *
 * <p>下游服务端通过该数据包 (s2p) 请求代理端把玩家转移到
 * {@link #serverName} 对应的服务器。
 */
@ToString
public class ServerTransferPacket implements BedrockPacket, BedrockPacketSerializer<ServerTransferPacket> {

    public static final int NETWORK_ID = 502;

    @Getter
    @Setter
    private String serverName = "";

    public ServerTransferPacket() {
    }

    public ServerTransferPacket(String serverName) {
        this.serverName = serverName == null ? "" : serverName;
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
    public ServerTransferPacket clone() {
        return new ServerTransferPacket(this.serverName);
    }

    @Override
    public void serialize(ByteBuf buffer, BedrockCodecHelper helper, ServerTransferPacket packet) {
        helper.writeString(buffer, packet.serverName == null ? "" : packet.serverName);
    }

    @Override
    public void deserialize(ByteBuf buffer, BedrockCodecHelper helper, ServerTransferPacket packet) {
        packet.serverName = helper.readString(buffer);
    }
}

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
 * {@code craft.meow.meowsync.nukkit.packets.ServerInfo}。
 *
 * <p>用于 s2p2s 的查询/应答流程：
 * <ul>
 *     <li>s2p：下游服务端发送 {@link #serverName}，表示想查询该服务器的信息；</li>
 *     <li>p2s：代理端把 {@link #info}（服务器信息，自由格式，建议 JSON）回传给下游服务端。</li>
 * </ul>
 *
 * <p><b>注意</b>：WaterdogPE 自身在 {@code dev.waterdog.waterdogpe.network.serverinfo}
 * 包下也有一个同名的 {@code ServerInfo} 类，二者含义不同，同时引用时需要使用全限定名区分。
 */
@ToString
public class ServerInfo implements BedrockPacket, BedrockPacketSerializer<ServerInfo> {

    public static final int NETWORK_ID = 504;

    @Getter
    @Setter
    private String serverName = "";

    @Getter
    @Setter
    private String info = "";

    public ServerInfo() {
    }

    public ServerInfo(String serverName, String info) {
        this.serverName = serverName == null ? "" : serverName;
        this.info = info == null ? "" : info;
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
    public ServerInfo clone() {
        return new ServerInfo(this.serverName, this.info);
    }

    @Override
    public void serialize(ByteBuf buffer, BedrockCodecHelper helper, ServerInfo packet) {
        helper.writeString(buffer, packet.serverName == null ? "" : packet.serverName);
        helper.writeString(buffer, packet.info == null ? "" : packet.info);
    }

    @Override
    public void deserialize(ByteBuf buffer, BedrockCodecHelper helper, ServerInfo packet) {
        packet.serverName = helper.readString(buffer);
        packet.info = helper.readString(buffer);
    }
}

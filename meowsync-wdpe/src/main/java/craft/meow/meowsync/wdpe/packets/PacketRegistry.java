package craft.meow.meowsync.wdpe.packets;

import dev.waterdog.waterdogpe.network.protocol.ProtocolCodecs;
import org.cloudburstmc.protocol.bedrock.data.PacketRecipient;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 把 MeowSync 的自定义数据包绑定到网络 id 上。
 *
 * <p>代理端只会保留自己认识的数据包，所以必须在 codec 构建之前
 * （也就是 {@code Plugin#onEnable()} 中）调用 {@link #register()}，
 * 否则代理端既无法解析、也无法发送这些数据包。
 */
public final class PacketRegistry {

    private static final AtomicBoolean REGISTERED = new AtomicBoolean(false);

    private PacketRegistry() {
    }

    public static void register() {
        if (!REGISTERED.compareAndSet(false, true)) {
            return;
        }

        ProtocolCodecs.addUpdater((builder, baseCodec) -> {
            builder.registerPacket(ForwardPacket::new, new ForwardPacket(),
                    ForwardPacket.NETWORK_ID, PacketRecipient.BOTH);
            builder.registerPacket(ServerTransferPacket::new, new ServerTransferPacket(),
                    ServerTransferPacket.NETWORK_ID, PacketRecipient.BOTH);
            builder.registerPacket(ReconnectPacket::new, new ReconnectPacket(),
                    ReconnectPacket.NETWORK_ID, PacketRecipient.BOTH);
            builder.registerPacket(ServerInfo::new, new ServerInfo(),
                    ServerInfo.NETWORK_ID, PacketRecipient.BOTH);
            return builder;
        });
    }
}

package cn.dancingsnow.neoecoprototype.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.InputStream;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * What our computation-cable split is built on, read off upstream's own class files.
 *
 * <p>{@code NeoECOPrototypeClient} stops drawing the mixin for this: the glowing pair is registered per host
 * through eco 21.2.10's {@code ECOComputationModels.registerCableModel(IECOTier, Block, ...)}, and eco's drive
 * renderer asks the host-keyed lookup. Two things have to stay true for that to reach the screen, and neither
 * is visible to the game-test server, which never loads a renderer:
 *
 * <ol>
 *   <li>eco's {@code renderFixed} must keep asking the <i>host-keyed</i> lookups - if it goes back to the
 *       tier-only ones, our per-host entries are never consulted and the cable silently reverts;</li>
 *   <li>the tier-only lookups must keep passing no host, because that is what keeps our own L1 frame out of
 *       the per-host table - it asks the tier-only pair and must keep getting the plain cable.</li>
 * </ol>
 */
final class ComputationCableModelContractTest {
    private static final String RENDERER_CLASS =
            "cn/dancingsnow/neoecoae/client/renderer/blockentity/ECOComputationDriveRenderer";
    private static final String MODELS_CLASS = "cn/dancingsnow/neoecoae/api/ECOComputationModels";
    private static final String TIER_ONLY_DESCRIPTOR =
            "(Lcn/dancingsnow/neoecoae/api/IECOTier;)Lnet/minecraft/resources/ResourceLocation;";
    private static final String HOST_KEYED_DESCRIPTOR =
            "(Lcn/dancingsnow/neoecoae/api/IECOTier;Lnet/minecraft/world/level/block/Block;)"
                    + "Lnet/minecraft/resources/ResourceLocation;";

    @Test
    void driveRendererStillAsksTheHostKeyedCableLookups() throws Exception {
        var renderFixed = ecoClass(RENDERER_CLASS).methods.stream()
                .filter(method -> method.name.equals("renderFixed")
                        && method.desc.startsWith("(Lcn/dancingsnow/neoecoae/blocks/entity/computation/"
                                + "ECOComputationDriveBlockEntity;"))
                .findFirst()
                .orElse(null);
        assertNotNull(renderFixed, "eco's drive renderer no longer declares renderFixed(...). Read the class "
                + "name and the registration in NeoECOPrototypeClient before believing anything in this file.");

        int hostConnected = 0;
        int hostDisconnected = 0;
        int tierOnly = 0;
        for (AbstractInsnNode node : renderFixed.instructions) {
            if (!(node instanceof MethodInsnNode call) || !call.owner.equals(MODELS_CLASS)) {
                continue;
            }
            boolean connected = call.name.equals("getCableConnectedModel");
            boolean disconnected = call.name.equals("getCableDisconnectedModel");
            if (!connected && !disconnected) {
                continue;
            }
            if (HOST_KEYED_DESCRIPTOR.equals(call.desc)) {
                if (connected) {
                    hostConnected++;
                } else {
                    hostDisconnected++;
                }
            } else if (TIER_ONLY_DESCRIPTOR.equals(call.desc)) {
                tierOnly++;
            }
        }

        // One connected and two disconnected, exactly as the retired redirect counted them at offsets
        // 257 / 309 / 358 of the 21.2.10 jar.
        assertEquals(1, hostConnected, "eco's renderFixed now asks the host-keyed connected lookup "
                + hostConnected + " times. The per-host registration in NeoECOPrototypeClient answers every"
                + " call, so a changed count means the cable is drawn from somewhere this file does not know.");
        assertEquals(2, hostDisconnected, "eco's renderFixed now asks the host-keyed disconnected lookup "
                + hostDisconnected + " times. See the note above.");
        assertEquals(0, tierOnly, "eco's renderFixed calls the tier-only cable lookup again (" + tierOnly
                + " times). Our per-host entries are never consulted on that path, so the glowing pair would"
                + " stop showing with nothing failing - re-add the redirect or ask eco before shipping.");
    }

    @Test
    void tierOnlyLookupPassesNoHostSoTheAddonFrameStaysOutOfThePerHostTable() throws Exception {
        var models = ecoClass(MODELS_CLASS);
        for (String name : new String[]{"getCableConnectedModel", "getCableDisconnectedModel"}) {
            var tierOnly = models.methods.stream()
                    .filter(method -> method.name.equals(name) && TIER_ONLY_DESCRIPTOR.equals(method.desc))
                    .findFirst()
                    .orElse(null);
            assertNotNull(tierOnly, "eco no longer ships " + name + "(IECOTier). Our own drive renderer calls"
                    + " that overload for the plain pair, so both sides need re-reading.");

            int hostKeyedCalls = 0;
            int withoutNullHost = 0;
            for (AbstractInsnNode node : tierOnly.instructions) {
                if (!(node instanceof MethodInsnNode call) || !call.owner.equals(MODELS_CLASS)
                        || !call.name.equals(name) || !HOST_KEYED_DESCRIPTOR.equals(call.desc)) {
                    continue;
                }
                hostKeyedCalls++;
                AbstractInsnNode previous = node.getPrevious();
                while (previous != null && previous.getType() == AbstractInsnNode.LINE
                        || previous != null && previous.getType() == AbstractInsnNode.LABEL
                        || previous != null && previous.getType() == AbstractInsnNode.FRAME) {
                    previous = previous.getPrevious();
                }
                if (previous == null || previous.getOpcode() != Opcodes.ACONST_NULL) {
                    withoutNullHost++;
                }
            }
            assertEquals(1, hostKeyedCalls, name + "(IECOTier) does not delegate to the host-keyed overload,"
                    + " so the two tables this split relies on are no longer one lookup apart.");
            assertEquals(0, withoutNullHost, name + "(IECOTier) hands a real block to the host-keyed lookup."
                    + " Our L1 frame asks this overload, so it would start reading the per-host table and"
                    + " wear the glowing pair inside its own machine.");
        }
    }

    @Test
    void ecoShipsThePublicPerHostRegistrationWeRegisterThrough() throws Exception {
        var models = ecoClass(MODELS_CLASS);
        var register = models.methods.stream()
                .filter(method -> method.name.equals("registerCableModel")
                        && method.desc.equals("(Lcn/dancingsnow/neoecoae/api/IECOTier;"
                                + "Lnet/minecraft/world/level/block/Block;"
                                + "Lnet/minecraft/resources/ResourceLocation;"
                                + "Lnet/minecraft/resources/ResourceLocation;)V"))
                .findFirst()
                .orElse(null);
        assertNotNull(register, "eco dropped registerCableModel(IECOTier, Block, ResourceLocation,"
                + " ResourceLocation) - the four-tier loop in NeoECOPrototypeClient has nothing to call.");
        assertTrue((register.access & Opcodes.ACC_STATIC) != 0,
                "registerCableModel(IECOTier, Block, ...) is no longer static, so the client cannot call it"
                        + " the way it does now.");
    }

    /**
     * Read from the classpath by default, because build.gradle puts exactly one eco jar there and that is the
     * file the mod compiles against. Pass {@code -Dneoecoprototype.test.ecoJar=<jar>} to read a different
     * upstream build - the control that shows these three checks are readings, not assertions about ourselves.
     */
    private static ClassNode ecoClass(String internalName) throws Exception {
        var override = System.getProperty("neoecoprototype.test.ecoJar");
        if (override != null && !override.isBlank()) {
            try (var jar = new java.util.jar.JarFile(new java.io.File(override))) {
                var entry = jar.getJarEntry(internalName + ".class");
                assertNotNull(entry, "no " + internalName + " inside " + override);
                try (InputStream input = jar.getInputStream(entry)) {
                    return read(input);
                }
            }
        }
        try (InputStream input = ComputationCableModelContractTest.class.getClassLoader()
                .getResourceAsStream(internalName + ".class")) {
            assertNotNull(input, "no " + internalName + " on the test classpath - the eco jar is not being"
                    + " resolved, so this test is not reading upstream at all");
            return read(input);
        }
    }

    private static ClassNode read(InputStream input) throws Exception {
        var node = new ClassNode();
        new org.objectweb.asm.ClassReader(input).accept(node, 0);
        return node;
    }
}

package cn.dancingsnow.neoecoprototype.mixin;

import cn.dancingsnow.neoecoae.api.IECOTier;
import cn.dancingsnow.neoecoprototype.api.SimplifyTier;
import cn.dancingsnow.neoecoprototype.client.ComputationCableModels;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import static org.junit.jupiter.api.Assertions.*;

class ComputationDriveRendererMixinTest {
    private static final String RENDERER = "cn/dancingsnow/neoecoae/client/renderer/blockentity/ECOComputationDriveRenderer.class";
    private static final String MODELS = "cn/dancingsnow/neoecoae/api/ECOComputationModels";
    private static final String WRAP = "Lcom/llamalad7/mixinextras/injector/wrapoperation/WrapOperation;";
    private static final String GROUP = "Lorg/spongepowered/asm/mixin/injection/Group;";

    @Test void rendererCableLookupsHaveExactlyOneCompatibleWrapper() throws Exception {
        ClassNode renderer = renderer();
        var mixin = read(ComputationDriveRendererMixinTest.class.getResourceAsStream("ECOComputationDriveRendererMixin.class"));
        var renderFixed = renderer.methods.stream().filter(method -> method.name.equals("renderFixed")
            && method.desc.startsWith("(Lcn/dancingsnow/neoecoae/blocks/entity/computation/ECOComputationDriveBlockEntity;"))
            .findFirst().orElseThrow();
        var totals = new HashMap<String, Integer>();
        int cableCalls = 0;
        for (var instruction : renderFixed.instructions) {
            if (!(instruction instanceof MethodInsnNode call) || !call.owner.equals(MODELS)
                || !(call.name.equals("getCableConnectedModel") || call.name.equals("getCableDisconnectedModel"))) continue;
            cableCalls++;
            String target = "L" + call.owner + ";" + call.name + call.desc;
            var matches = mixin.methods.stream().filter(method -> {
                var wrap = annotation(method, WRAP);
                if (wrap == null) return false;
                return ((List<?>) value(wrap, "at")).stream()
                    .anyMatch(at -> target.equals(value((AnnotationNode) at, "target")));
            }).toList();
            assertEquals(1, matches.size(), "Uncovered or ambiguous invocation: " + target);
            var wrapper = matches.getFirst();
            var wrap = annotation(wrapper, WRAP);
            assertTrue(((List<?>) value(wrap, "method")).contains(renderFixed.name + renderFixed.desc));
            assertEquals(0, value(wrap, "require"));
            int close = call.desc.indexOf(')');
            assertEquals(call.desc.substring(0, close) + Type.getDescriptor(Operation.class)
                + call.desc.substring(close), wrapper.desc, "Wrapper arguments must match the invoked signature");
            var group = annotation(wrapper, GROUP);
            assertNotNull(group, "Alternative signatures must retain a combined injection requirement");
            totals.merge((String) value(group, "name"), 1, Integer::sum);
        }
        assertEquals(3, cableCalls, "Expected one connected and two disconnected cable lookups");
        assertEquals(2, totals.size());
        for (var wrapper : mixin.methods) {
            var group = annotation(wrapper, GROUP);
            if (group == null) continue;
            String name = (String) value(group, "name");
            assertEquals(value(group, "min"), totals.get(name), name);
            assertEquals(value(group, "max"), totals.get(name), name);
        }
    }

    @Test void otherTiersKeepTheOriginalLookupAndItsHostArgument() throws Exception {
        var tier = (IECOTier) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{IECOTier.class},
            (proxy, method, arguments) -> { throw new AssertionError("Unexpected tier call: " + method); });
        var expected = ResourceLocation.parse("test:original_cable");
        for (var wrapper : wrappers()) {
            int[] calls = {0};
            Operation<ResourceLocation> original = arguments -> {
                calls[0]++;
                assertEquals(wrapper.getParameterCount() - 1, arguments.length);
                assertSame(tier, arguments[0]);
                if (arguments.length == 2) assertNull(arguments[1]);
                return expected;
            };
            assertSame(expected, invoke(wrapper, tier, original));
            assertEquals(1, calls[0]);
        }
    }

    @Test void addonTiersKeepTheUpperHostCableModelsForBothSignatures() throws Exception {
        for (var tier : SimplifyTier.values()) {
            for (var wrapper : wrappers()) {
                Operation<ResourceLocation> original = arguments -> {
                    throw new AssertionError("An addon tier must use its upper-host cable model");
                };
                var expected = wrapper.getName().contains("$disconnected")
                    ? ComputationCableModels.UPPER_DISCONNECTED : ComputationCableModels.UPPER_CONNECTED;
                assertEquals(expected, invoke(wrapper, tier, original));
            }
        }
    }

    private static List<Method> wrappers() {
        return Stream.of(ECOComputationDriveRendererMixin.class.getDeclaredMethods())
            .filter(method -> method.getParameterCount() > 0
                && method.getParameterTypes()[method.getParameterCount() - 1] == Operation.class).toList();
    }

    private static Object invoke(Method wrapper, IECOTier tier, Operation<ResourceLocation> original) throws Exception {
        var arguments = new ArrayList<Object>();
        arguments.add(tier);
        if (wrapper.getParameterCount() == 3) arguments.add(null);
        arguments.add(original);
        wrapper.setAccessible(true);
        return wrapper.invoke(new ECOComputationDriveRendererMixin(), arguments.toArray());
    }

    private static ClassNode renderer() throws Exception {
        String candidate = System.getProperty("neoecoprototype.test.ecoJar");
        if (candidate == null) return read(ComputationDriveRendererMixinTest.class.getClassLoader().getResourceAsStream(RENDERER));
        try (var jar = new JarFile(Path.of(candidate).toFile())) {
            return read(jar.getInputStream(jar.getJarEntry(RENDERER)));
        }
    }

    private static ClassNode read(InputStream input) throws Exception {
        assertNotNull(input);
        try (input) {
            var result = new ClassNode();
            new ClassReader(input).accept(result, 0);
            return result;
        }
    }

    private static AnnotationNode annotation(MethodNode method, String descriptor) {
        return Stream.concat(method.visibleAnnotations == null ? Stream.empty() : method.visibleAnnotations.stream(),
            method.invisibleAnnotations == null ? Stream.empty() : method.invisibleAnnotations.stream())
            .filter(annotation -> annotation.desc.equals(descriptor)).findFirst().orElse(null);
    }

    private static Object value(AnnotationNode annotation, String key) {
        for (int index = 0; index < annotation.values.size(); index += 2) {
            if (annotation.values.get(index).equals(key)) return annotation.values.get(index + 1);
        }
        return null;
    }
}

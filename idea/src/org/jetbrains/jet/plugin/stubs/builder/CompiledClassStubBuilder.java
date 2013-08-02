package org.jetbrains.jet.plugin.stubs.builder;

import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jet.descriptors.serialization.ClassData;
import org.jetbrains.jet.descriptors.serialization.Flags;
import org.jetbrains.jet.descriptors.serialization.NameResolver;
import org.jetbrains.jet.descriptors.serialization.ProtoBuf;
import org.jetbrains.jet.lang.psi.stubs.PsiJetFileStub;
import org.jetbrains.jet.lang.psi.stubs.elements.JetStubElementTypes;
import org.jetbrains.jet.lang.psi.stubs.impl.PsiJetClassStubImpl;
import org.jetbrains.jet.lang.psi.stubs.impl.PsiJetFileStubImpl;
import org.jetbrains.jet.lang.resolve.java.resolver.KotlinClassFileHeader;
import org.jetbrains.jet.lang.resolve.name.FqName;
import org.jetbrains.jet.lang.resolve.name.Name;

import java.util.List;

public class CompiledClassStubBuilder extends CompiledStubBuilderBase {

    @NotNull
    private final NameResolver nameResolver;
    @NotNull
    private final ProtoBuf.Class classProto;
    private final FqName classFqName;

    public CompiledClassStubBuilder(@NotNull KotlinClassFileHeader header) {
        super(header);
        ClassData classData = header.readClassData();
        assert classData != null;
        this.nameResolver = classData.getNameResolver();
        this.classProto = classData.getClassProto();
        this.classFqName = header.getJvmClassName().getFqName();
    }

    @NotNull
    public PsiJetFileStub createStub() {
        PsiJetFileStubImpl fileStub = createFileStub();
        Name name = nameResolver.getName(classProto.getName());
        int flags = classProto.getFlags();
        ProtoBuf.Class.Kind kind = Flags.CLASS_KIND.get(flags);
        //TODO: inner classes?
        PsiJetClassStubImpl classStub =
                new PsiJetClassStubImpl(JetStubElementTypes.CLASS, fileStub, classFqName.asString(), name.asString(), getSuperList(),
                                        kind == ProtoBuf.Class.Kind.TRAIT, kind == ProtoBuf.Class.Kind.ENUM_CLASS,
                                        kind == ProtoBuf.Class.Kind.ENUM_ENTRY, kind == ProtoBuf.Class.Kind.ANNOTATION_CLASS, false);
        for (ProtoBuf.Callable callableProto : classProto.getMemberList()) {
            createCallableStub(classStub, callableProto);
        }
        return fileStub;
    }

    @NotNull
    private List<String> getSuperList() {
        return ContainerUtil.map(classProto.getSupertypeList(), new Function<ProtoBuf.Type, String>() {
            @Override
            public String fun(ProtoBuf.Type type) {
                assert type.getConstructor().getKind() == ProtoBuf.Type.Constructor.Kind.CLASS;
                FqName superFqName = getNameResolver().getFqName(type.getConstructor().getId());
                return superFqName.asString();
            }
        });
    }

    @NotNull
    @Override
    protected NameResolver getNameResolver() {
        return nameResolver;
    }

    @Nullable
    @Override
    protected FqName getInternalFqName(@NotNull String name) {
        return null;
    }
}

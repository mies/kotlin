package org.jetbrains.jet.plugin.stubs.builder;

import com.intellij.psi.stubs.StubElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jet.descriptors.serialization.Flags;
import org.jetbrains.jet.descriptors.serialization.NameResolver;
import org.jetbrains.jet.descriptors.serialization.ProtoBuf;
import org.jetbrains.jet.lang.psi.stubs.elements.JetStubElementTypes;
import org.jetbrains.jet.lang.psi.stubs.impl.PsiJetFileStubImpl;
import org.jetbrains.jet.lang.psi.stubs.impl.PsiJetFunctionStubImpl;
import org.jetbrains.jet.lang.psi.stubs.impl.PsiJetObjectStubImpl;
import org.jetbrains.jet.lang.psi.stubs.impl.PsiJetPropertyStubImpl;
import org.jetbrains.jet.lang.resolve.java.resolver.KotlinClassFileHeader;
import org.jetbrains.jet.lang.resolve.name.FqName;
import org.jetbrains.jet.lang.resolve.name.Name;

public abstract class CompiledStubBuilderBase {

    @NotNull
    protected final FqName packageFqName;

    public CompiledStubBuilderBase(@NotNull KotlinClassFileHeader header) {
        this.packageFqName = header.getJvmClassName().getFqName().parent();
    }

    protected void createCallableStub(@NotNull StubElement parentStub, @NotNull ProtoBuf.Callable callableProto) {
        ProtoBuf.Callable.CallableKind callableKind = Flags.CALLABLE_KIND.get(callableProto.getFlags());
        String callableName = getNameResolver().getName(callableProto.getName()).asString();
        FqName callableFqName = getInternalFqName(callableName);
        switch (callableKind) {
            case FUN:
                new PsiJetFunctionStubImpl(JetStubElementTypes.FUNCTION, parentStub, callableName,
                                           callableFqName != null, callableFqName, callableProto.hasReceiverType());
                break;
            case VAL:
                //TODO: type text, inference body text
                new PsiJetPropertyStubImpl(JetStubElementTypes.PROPERTY, parentStub, callableName, false, callableFqName != null, callableFqName, null, null);
                break;
            case VAR:
                new PsiJetPropertyStubImpl(JetStubElementTypes.PROPERTY, parentStub, callableName, true, callableFqName != null, callableFqName, null, null);
                break;
            case CONSTRUCTOR:
                throw new IllegalStateException("");
            case OBJECT_PROPERTY:
                new PsiJetObjectStubImpl(JetStubElementTypes.OBJECT_DECLARATION, parentStub, callableName, callableFqName, true, false);
                break;
        }
    }

    protected PsiJetFileStubImpl createFileStub() {
        return new PsiJetFileStubImpl(null, packageFqName.asString(), packageFqName.isRoot());
    }

    @NotNull
    protected abstract NameResolver getNameResolver();

    @Nullable
    protected abstract FqName getInternalFqName(@NotNull String name);
}

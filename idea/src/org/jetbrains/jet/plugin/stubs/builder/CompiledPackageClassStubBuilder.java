/*
 * Copyright 2010-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.jet.plugin.stubs.builder;

import com.intellij.psi.stubs.StubElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jet.descriptors.serialization.Flags;
import org.jetbrains.jet.descriptors.serialization.NameResolver;
import org.jetbrains.jet.descriptors.serialization.PackageData;
import org.jetbrains.jet.descriptors.serialization.ProtoBuf;
import org.jetbrains.jet.lang.psi.stubs.PsiJetFileStub;
import org.jetbrains.jet.lang.psi.stubs.elements.JetStubElementTypes;
import org.jetbrains.jet.lang.psi.stubs.impl.PsiJetFileStubImpl;
import org.jetbrains.jet.lang.psi.stubs.impl.PsiJetFunctionStubImpl;
import org.jetbrains.jet.lang.resolve.java.resolver.KotlinClassFileHeader;
import org.jetbrains.jet.lang.resolve.name.FqName;
import org.jetbrains.jet.lang.resolve.name.Name;

public final class CompiledPackageClassStubBuilder {

    @NotNull
    private final ProtoBuf.Package packageProto;
    @NotNull
    private final NameResolver nameResolver;
    @NotNull
    private final FqName packageFqName;

    public CompiledPackageClassStubBuilder(@NotNull KotlinClassFileHeader header) {
        PackageData packageData = header.readPackageData();
        assert packageData != null;
        this.packageProto = packageData.getPackageProto();
        this.nameResolver = packageData.getNameResolver();
        this.packageFqName = header.getJvmClassName().getFqName().parent();
    }

    public PsiJetFileStub createStub() {
        PsiJetFileStubImpl fileStub = new PsiJetFileStubImpl(null, packageFqName.asString(), packageFqName.isRoot());
        for (ProtoBuf.Callable callableProto : packageProto.getMemberList()) {
            createCallableStub(fileStub, callableProto);
        }
        return fileStub;
    }

    private void createCallableStub(@NotNull StubElement parentStub, @NotNull ProtoBuf.Callable callableProto) {
        ProtoBuf.Callable.CallableKind callableKind = Flags.CALLABLE_KIND.get(callableProto.getFlags());
        String callableName = nameResolver.getName(callableProto.getName()).asString();
        FqName callableFqName = packageFqName.child(
                Name.identifier(callableName));
        switch (callableKind) {
            case FUN:
                new PsiJetFunctionStubImpl(JetStubElementTypes.FUNCTION, parentStub, callableName,
                                           true, callableFqName, callableProto.hasReceiverType());
                break;
            case VAL:
                break;
            case VAR:
                break;
            case CONSTRUCTOR:
                //TODO:
                throw new IllegalStateException("");
            case OBJECT_PROPERTY:
                break;
        }
    }
}
